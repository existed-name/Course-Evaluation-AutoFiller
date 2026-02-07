# 项目技术要点

## 核心技术实现

### 1. 登录状态保持

**问题**：避免每次运行都重新登录，绕过验证码等复杂认证流程。

**解决方案**：复用浏览器用户数据目录

```java
EdgeOptions options = new EdgeOptions();
options.addArguments("user-data-dir=" + EDGE_USER_DATA_DIR);
options.addArguments("profile-directory=" + EDGE_PROFILE);
```

**工作原理**：
- Edge浏览器将Cookie、Session等数据存储在用户数据目录
- 通过指定 `user-data-dir` 参数，Selenium复用现有的登录状态
- 首次手动登录后，后续运行自动保持登录

**注意事项**：
- 需先手动登录一次并保存Cookie
- 运行脚本前需关闭所有Edge浏览器窗口（避免数据目录占用）

---

### 2. 登录验证机制

**问题**：如何判断用户是否已成功登录？

**解决方案**：多元素并行检测

```java
for (String xpath : LOGIN_SUCCESS_INDICATORS) {
    try {
        WebElement element = wait.until(
            ExpectedConditions.presenceOfElementLocated(By.xpath(xpath))
        );
        if (element.isDisplayed()) {
            return true; // 检测到任一登录标识即认为成功
        }
    } catch (Exception e) {
        continue; // 当前XPath失败，尝试下一个
    }
}
```

**检测策略**：
1. **账户链接**：`//*[@id='navAccountLink']`
2. **功能菜单**：评价、选课、成绩等链接
3. **退出按钮**：注销按钮的存在

**优势**：
- 多重保险，提高检测成功率
- 适应不同页面结构变化
- 支持不同的教务系统版本

---

### 3. XPath动态定位策略

**问题**：老旧教务系统元素ID动态生成（如 `id="btn_12345"`），每次刷新都会变化。

**解决方案**：多备用方案数组 + 文本/属性组合定位

```java
// 配置多种XPath方案
public static final String[] XPATH_EVALUATION_MENU = {
    "//*[@id='navItem_app_evaluateOnline']/a/div[2]/div",  // 精确ID路径
    "//*[@id='navItem_app_evaluateOnline']//a",            // ID通配
    "//a[contains(normalize-space(.), '教学质量评价')]",     // 文本匹配
    "//a[contains(@href, 'evaluateOnline')]"              // href特征
};

// 逐个尝试
for (String xpath : XPATH_EVALUATION_MENU) {
    try {
        return driver.findElement(By.xpath(xpath));
    } catch (NoSuchElementException e) {
        continue;
    }
}
```

**关键技术**：
- `normalize-space(.)` - 处理文本中的换行、空格、缩进
- `contains()` - 模糊匹配，适应文本变化
- 优先级排序 - 精确定位 → 模糊匹配 → 属性特征

---

### 4. 显式等待机制

**问题**：页面加载时间不确定，`Thread.sleep()` 浪费时间或等待不足。

**解决方案**：WebDriverWait + ExpectedConditions

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

// 等待元素可点击
WebElement element = wait.until(
    ExpectedConditions.elementToBeClickable(By.xpath(xpath))
);

// 等待URL变化
wait.until(driver -> driver.getCurrentUrl().contains("evaluate"));
```

**工作原理**：
- 每500ms轮询一次条件
- 条件满足立即返回（最快可能只需100ms）
- 超时后抛出 `TimeoutException`

**优势**：
- 智能等待，最小化时间浪费
- 明确超时时间，便于调试
- 可自定义等待条件

---

### 5. Stale Element处理

**问题**：DOM刷新后，之前获取的 `WebElement` 引用失效。

**场景**：
```
1. 提取列表 → 获取 WebElement A
2. 评价课程 → 提交后列表刷新
3. 点击下一个 → WebElement A 已失效 → StaleElementReferenceException
```

**解决方案1**：每次循环重新提取列表

```java
while (true) {
    List<CourseEvaluation> courses = extractor.extractPendingCourses();
    if (courses.isEmpty()) break;
    
    CourseEvaluation course = courses.get(0); // 只处理第一门
    // 评价完成后，下次循环会重新提取最新列表
}
```

**解决方案2**：动态重定位元素

```java
private static WebElement refindEvalButton(CourseEvaluation course) {
    // 根据课程名称和教师名称重新定位行
    String rowXPath = String.format(
        "//tr[contains(., '%s') and contains(., '%s')]",
        course.courseName(),
        course.teacherName()
    );
    
    WebElement row = wait.until(
        ExpectedConditions.presenceOfElementLocated(By.xpath(rowXPath))
    );
    
    // 在最新的行中查找按钮
    return row.findElement(By.xpath(buttonXPath));
}
```

**核心思路**：基于业务信息（课程名、教师名）而非技术引用（WebElement对象）

---

### 6. 空列表检测

**问题**：所有课程评价完成后，表格消失，显示完成提示，脚本报错。

**解决方案**：优先检测完成状态

```java
public List<CourseEvaluation> extractPendingCourses() {
    // 先检查是否已完成所有评价
    if (isAllCompleted()) {
        return new ArrayList<>(); // 返回空列表，而非抛异常
    }
    
    // 正常提取逻辑...
}

private boolean isAllCompleted() {
    for (String xpath : XPATH_ALL_COMPLETED_INDICATORS) {
        try {
            WebElement notice = driver.findElement(By.xpath(xpath));
            if (notice.isDisplayed()) {
                return true; // 检测到"太棒了"提示
            }
        } catch (NoSuchElementException e) {
            continue;
        }
    }
    return false;
}
```

**检测特征**：
- 提示框元素：`//*[@id='Questionnaire']/div[@class='systemNotice']`
- 标题文本：`//h3[contains(text(), '太棒了')]`
- 内容文本：`//p[contains(text(), '完成了目前所有的问卷')]`

---

### 7. 表单自动填写

**问题**：如何批量选择单选题并填写文本框？

**解决方案**：定位所有选项单元格，遍历点击

```java
// 1. 定位所有选项单元格
List<WebElement> optionCells = driver.findElements(
    By.xpath("//td[@class='quota ltr']")
);

// 2. 在每个单元格内点击"非常满意"选项
for (WebElement cell : optionCells) {
    WebElement option = cell.findElement(
        By.xpath(".//a[@data-opt='1']") // data-opt="1" 表示第一个选项
    );
    option.click();
    Thread.sleep(500); // 短暂延迟，让页面响应
}

// 3. 填写评语
driver.findElement(By.xpath("//*[@id='CourseComment']"))
      .sendKeys("无");
```

**关键点**：
- 所有题目都在 `class="quota ltr"` 的单元格内
- 使用 `data-opt` 属性区分选项（1=非常满意，2=满意...）
- `.//` 表示在当前元素内部查找（相对路径）

---

### 8. 提交按钮禁用处理

**问题**：提交按钮初始状态为 `disabled="disabled"`，需所有题目填写完才启用。

**解决方案**：JavaScript强制移除禁用属性

```java
WebElement submitButton = driver.findElement(By.xpath("//*[@id='postTrigger']"));

// 检查并移除disabled属性
String disabled = submitButton.getAttribute("disabled");
if (disabled != null && !disabled.isEmpty()) {
    ((JavascriptExecutor) driver).executeScript(
        "arguments[0].removeAttribute('disabled');", 
        submitButton
    );
}

// 点击提交
submitButton.click();
```

**备用点击方案**：
```java
try {
    element.click(); // 常规点击
} catch (Exception e) {
    // 如果被遮挡或不可点击，使用JavaScript点击
    ((JavascriptExecutor) driver).executeScript(
        "arguments[0].click();", 
        element
    );
}
```

---

### 9. 页面跳转验证

**问题**：如何确认页面已成功跳转？

**解决方案**：URL + DOM双重验证

```java
// 方式1：检查URL特征
wait.until(driver -> {
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    return Arrays.stream(EVALUATION_URL_KEYWORDS)
                 .anyMatch(currentUrl::contains);
});

// 方式2：检查特征元素
wait.until(ExpectedConditions.presenceOfElementLocated(
    By.xpath("//h2[contains(text(), '教学质量评价')]")
));
```

**双重保险**：
- URL检查失败时，自动尝试DOM检查
- 适应不同系统的跳转行为

---

### 10. 异常恢复机制

**问题**：某门课程评价失败后，如何继续处理其他课程？

**解决方案**：Try-Catch + 错误恢复

```java
try {
    evaluateSingleCourse(freshButton, course);
    successCount++;
} catch (Exception e) {
    failedCount++;
    System.err.printf("❌ 失败: %s%n", e.getMessage());
    
    // 尝试恢复
    recoverFromError();
}

private static void recoverFromError() {
    try {
        driver.navigate().back(); // 优先后退
        Thread.sleep(2000);
    } catch (Exception backError) {
        // 后退失败，重新导航到列表页
        navigationService.navigateToEvaluationPage();
    }
}
```

**恢复策略**：
1. 尝试后退到列表页
2. 后退失败则重新导航
3. 继续处理下一门课程

**防护措施**：
```java
final int MAX_ATTEMPTS = 100; // 防止死循环
int totalAttempts = 0;

while (totalAttempts < MAX_ATTEMPTS) {
    totalAttempts++;
    // 主逻辑...
}
```

---

### 11. 反自动化检测

**问题**：教务系统可能检测Selenium并拒绝访问。

**解决方案**：隐藏自动化特征

```java
EdgeOptions options = new EdgeOptions();

// 禁用"Chrome正受到自动化测试软件的控制"提示
options.addArguments("--disable-blink-features=AutomationControlled");
options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
options.addArguments("--disable-infobars");

// 设置User-Agent（可选）
options.addArguments("user-agent=Mozilla/5.0 ...");
```

**检测点**：
- `navigator.webdriver` 属性（已通过配置隐藏）
- 窗口大小异常（已设置 `--start-maximized`）
- 特征HTTP头（已设置真实User-Agent）

---

### 12. 进程管理

**问题**：用户数据目录被占用，无法启动新浏览器。

**解决方案**：启动前强制清理进程

```java
if (FORCE_KILL_EDGE_PROCESS) {
    Runtime.getRuntime().exec("taskkill /F /IM msedge.exe");
    Runtime.getRuntime().exec("taskkill /F /IM msedgedriver.exe");
    Thread.sleep(2000); // 等待进程完全释放
}
```

**注意**：
- `/F` 表示强制结束
- `/IM` 表示按镜像名（进程名）
- 会关闭所有Edge窗口，使用前提醒用户保存工作

---

## 架构设计

### 模块化分层

```
AutoEvaluationMain (应用层)
    ├─ BrowserManager (浏览器管理层)
    ├─ NavigationService (导航服务层)
    ├─ DataExtractor (数据提取层)
    └─ FormFiller (表单填写层)
```

**职责划分**：
- **BrowserManager**：浏览器生命周期管理
- **NavigationService**：页面间导航跳转
- **DataExtractor**：HTML解析和数据提取
- **FormFiller**：表单填写和提交
- **AutoEvaluationMain**：流程编排和异常处理

### 配置集中管理

```java
public final class SystemConfig {
    // 系统路径
    public static final String EDGE_DRIVER_PATH = "...";
    
    // XPath表达式（数组形式，支持备用方案）
    public static final String[] XPATH_EVALUATION_MENU = {...};
    
    // 等待时间
    public static final Duration EXPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(10);
}
```

**优势**：
- 所有配置集中在一个文件
- 常量命名规范统一
- 便于维护和扩展

---

## 性能优化

### 1. 智能等待

```java
// ❌ 固定等待（浪费时间）
Thread.sleep(5000);

// ✅ 智能等待（条件满足立即返回）
wait.until(ExpectedConditions.elementToBeClickable(element));
```

### 2. 元素缓存

```java
// 表格行一次性提取，避免重复查找
List<WebElement> rows = table.findElements(By.xpath(XPATH_TABLE_ROWS));
```

### 3. 短路求值

```java
// 任一XPath成功即返回，无需尝试所有方案
for (String xpath : xpaths) {
    try {
        return driver.findElement(By.xpath(xpath));
    } catch (Exception e) {
        continue;
    }
}
```

---

## 调试技巧

### 1. 浏览器保持开启

```java
public static boolean AUTO_CLOSE_BROWSER = false; // 调试时设为false
```

### 2. 详细日志输出

```java
System.out.println("┌─────────────────────────────────");
System.out.println("│ 标题: " + driver.getTitle());
System.out.println("│ URL:  " + driver.getCurrentUrl());
System.out.println("└─────────────────────────────────");
```

### 3. Console验证XPath

```javascript
// 在浏览器F12 Console中测试
$x("//a[contains(normalize-space(.), '教学质量评价')]")
```

---

## 最佳实践

### 1. XPath编写原则

✅ **推荐**：
- 优先使用ID（最稳定）
- 使用 `normalize-space()` 处理空格
- 提供多个备用方案

❌ **避免**：
- 使用绝对路径（如 `/html/body/div[1]/...`）
- 依赖动态ID
- 过度嵌套的层级定位

### 2. 异常处理原则

```java
// ✅ 具体异常 + 友好提示
try {
    element.click();
} catch (StaleElementReferenceException e) {
    element = refindElement(); // 重新定位
} catch (ElementNotInteractableException e) {
    jsClick(element); // JavaScript备用方案
}

// ❌ 捕获所有异常 + 沉默失败
try {
    element.click();
} catch (Exception e) {
    // 什么都不做
}
```

### 3. 代码可读性原则

```java
// ✅ 方法名自解释
private static void navigateToEvaluationPage() { }
private static boolean isAllCompleted() { }

// ❌ 含糊的命名
private static void doStuff() { }
private static boolean check() { }
```

---

## 常见坑点

### 1. 用户数据目录路径错误

```java
// ❌ 错误：单反斜杠（Java转义问题）
"C:\Users\...\Edge\User Data"

// ✅ 正确：双反斜杠
"C:\\Users\\...\\Edge\\User Data"
```

### 2. 隐式等待与显式等待冲突

```java
// ❌ 两种等待同时使用，导致超时时间累加
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
// 实际超时：5秒（隐式）+ 10秒（显式）= 15秒

// ✅ 只使用显式等待
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
```

### 3. 忘记切换iframe

```java
// 如果元素在iframe内
driver.switchTo().frame("iframeName");
driver.findElement(By.id("element"));
driver.switchTo().defaultContent(); // 切回主文档
```

---

## 总结

本项目通过以下技术方案实现了稳定、高效的自动化评教：

1. **登录保持**：复用用户数据目录，免登录
2. **元素定位**：多XPath备用 + 文本匹配，适应动态ID
3. **等待策略**：显式等待，智能响应页面加载
4. **异常处理**：Stale Element重定位 + 空列表检测
5. **错误恢复**：Try-Catch + 自动返回列表页
6. **反检测**：隐藏自动化特征，模拟真实用户

核心思想：**防御式编程 + 多重保险 + 友好降级**
