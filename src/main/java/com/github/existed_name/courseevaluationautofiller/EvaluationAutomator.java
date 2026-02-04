package com.github.existed_name.courseevaluationautofiller;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * 教学评价自动化 - 模块1：浏览器接管与登录确认
 * <br>
 * 特性：
 * 1. 复用 Edge 用户配置（保留登录状态）
 * 2. 多元素智能检测登录状态
 * 3. 可选自动/手动关闭浏览器
 * <br>
 * 环境：Java 21 + Selenium 4.40.0 + EdgeDriver 144.0
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @author <a href="https://chat.deepseek.com/"> DeepSeekV3.2 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @CreateTime 2026/2/1
 */
public class EvaluationAutomator {
    
    // ==================== 1. 系统路径常量 ====================
    
    /** EdgeDriver 驱动路径（请根据实际安装位置修改） */
    private static final String EDGE_DRIVER_PATH = 
            "D:\\Users\\Programming\\WebDriver\\EdgeDriver\\EdgeDriver144.0\\msedgedriver.exe";
    
    /** Edge 用户数据目录（保留登录 Cookie 的关键） */
    private static final String EDGE_USER_DATA_DIR = 
            "C:\\Users\\用户名\\AppData\\Local\\Microsoft\\Edge\\User Data";
    
    /** Edge 配置文件名（Default 为默认，也可改为 "Profile 1" 等） */
    private static final String EDGE_PROFILE = "Default";
    
    // ==================== 2. 业务 URL 常量 ====================
    
    /** 
     * 教务系统目标地址
     * 注意：使用 user-data-dir 后，如果已登录应直接进入此页面，
     * 如果仍跳转到登录页，说明 Cookie 已过期
     */
    private static final String TARGET_URL = 
            "https://matrix.dean.swust.edu.cn/acadmicManager/index.cfm?event=studentPortal:DEFAULT_EVENT";
    
    // ==================== 3. 等待时间配置 ====================
    
    /** 页面初始加载等待（毫秒）：给浏览器足够时间读取用户配置 */
    private static final long INITIAL_LOAD_DELAY = 3000;
    
    /** 显式等待超时（秒）：查找元素的最大等待时间 */
    private static final Duration EXPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(10);
    
    /** 隐式等待全局超时（秒）：元素轮询间隔 */
    private static final Duration IMPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(5);
    
    // ==================== 4. 登录状态检测配置 ====================
    
    /** 
     * 登录成功标识元素列表（任一匹配即认为成功）
     * 建议按"出现概率高 → 低"排序，提高检测效率
     */
    private static final List<String> LOGIN_SUCCESS_INDICATORS = Arrays.asList(
            "//*[@id='navAccountLink']",           // 个人账户链接（最精确）
            "//a[contains(text(),'评价')]",         // 教学评价入口
            "//a[contains(text(),'选课')]",         // 选课入口
            "//a[contains(text(),'成绩')]",         // 成绩查询
            "//a[contains(text(),'考试')]",         // 考试安排
            "//a[contains(text(),'注销')]",         // 注销按钮
            "//a[contains(text(),'档案')]"          // 档案管理
    );
    
    /** 登录页特征元素（用于检测是否已掉线/未登录） */
    private static final List<String> LOGIN_PAGE_INDICATORS = Arrays.asList(
            "//input[@id='username']",              // 用户名输入框
            "//input[@type='password']",            // 密码输入框
            "//a[contains(text(),'登录')]",         // 登录按钮
            "//button[contains(text(),'登录')]"
    );
    
    // ==================== 5. 行为控制开关 ====================
    
    /** 
     * 是否自动关闭浏览器
     * true  - 程序结束时自动关闭（适合无人值守）
     * false - 保持浏览器开启，供手动检查或继续操作（推荐调试时使用）
     */
    private static boolean AUTO_CLOSE_BROWSER = false;
    
    /** 强制清理 Edge 进程（解决用户数据占用问题） */
    private static final boolean FORCE_KILL_EDGE_PROCESS = true;
    
    // ==================== 成员变量 ====================
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    // ==================== 主程序入口 ====================
    
    public static void main(String[] args) {
        EvaluationAutomator automator = new EvaluationAutomator();
        try {
            automator.initEnvironment();
            automator.navigateToTarget();
            
            if (automator.verifyLoginStatus()) {
                System.out.println("🎉 环境准备就绪，可以进入模块 2（评价自动化）");
                // TODO: 在这里调用模块 2 的入口
                // new EvaluationProcessor(driver).start();
            } else {
                System.err.println("⚠️ 检测到未登录状态，请检查 Cookie 是否过期");
            }
        } catch (Exception e) {
            System.err.println("❌ 自动化流程异常终止: " + e.getMessage());
            e.printStackTrace();
        } finally {
            automator.cleanup();
        }
    }
    
    // ==================== 核心方法（按执行顺序）====================
    
    /**
     * 步骤 1：环境初始化
     * 包含：进程清理 → 驱动配置 → 浏览器启动
     */
    public void initEnvironment() throws IOException, InterruptedException {
        System.out.println("🚀 开始初始化自动化环境...");
        
        if (FORCE_KILL_EDGE_PROCESS) {
            killEdgeProcesses();
            // 等待进程完全释放（特别是用户数据文件锁）
            Thread.sleep(2000);
        }
        
        setupDriver();
        configureBrowser();
        
        System.out.println("✅ 环境初始化完成");
    }
    
    /**
     * 强制结束 Edge 相关进程
     * 注意：这会关闭你正在使用的 Edge，请提前保存工作！
     */
    private void killEdgeProcesses() throws IOException {
        System.out.println("   [系统] 正在清理 Edge 进程...");
        try {
            // /F 表示强制结束，/IM 表示按镜像名
            Runtime.getRuntime().exec("taskkill /F /IM msedge.exe");
            Runtime.getRuntime().exec("taskkill /F /IM msedgedriver.exe");
            System.out.println("   [系统] 进程清理完成（如有报错可忽略）");
        } catch (IOException e) {
            System.err.println("   [警告] 进程清理失败，可能无权限或进程不存在: " + e.getMessage());
        }
    }
    
    /**
     * 配置 WebDriver 系统属性
     */
    private void setupDriver() throws IOException{
//        System.setProperty("webdriver.edge.driver", EDGE_DRIVER_PATH);
        System.setProperty("webdriver.edge.driver", loadEdgeDriver());
//        System.out.println("   [配置] 驱动路径: " + EDGE_DRIVER_PATH);
    }

    /**
     * 临时补上
     *
     * @return EdgeDriver的绝对路径
     */
    public static String loadEdgeDriver() throws IOException{
        // 1. 获取 JAR 内部的资源流
        InputStream inputStream = EvaluationAutomator.class.getResourceAsStream( "/msedgedriver.exe" );

        // 2. 在系统临时目录下创建一个临时文件
        File tempDriver = File.createTempFile("msedgedriver", ".exe");
        tempDriver.deleteOnExit(); // 程序退出时自动删除

        // 3. 将资源流拷贝到临时文件
        Files.copy(inputStream, tempDriver.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempDriver.getAbsolutePath();
    }
    
    /**
     * 配置并启动浏览器
     * 关键：复用用户数据目录以保留登录状态
     */
    private void configureBrowser() {
        EdgeOptions options = new EdgeOptions();
        
        // 1. 反自动化检测（降低被识别为机器人的概率）
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--disable-infobars");
        
        // 2. 窗口设置
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        
        // 3. 【核心】复用用户配置（Cookie、缓存、登录状态）
//        options.addArguments("user-data-dir=" + EDGE_USER_DATA_DIR);
        options.addArguments("user-data-dir=" + getUserDataDir());
        options.addArguments("profile-directory=" + EDGE_PROFILE);
        
        // 4. 其他稳定性配置
        options.addArguments("--no-sandbox");           // 沙箱模式（某些环境需要）
        options.addArguments("--disable-dev-shm-usage"); // 共享内存问题修复
        
        this.driver = new EdgeDriver(options);
        
        // 设置等待策略
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIMEOUT);
        this.wait = new WebDriverWait(driver, EXPLICIT_WAIT_TIMEOUT);
        
        System.out.println("   [配置] 浏览器已启动（使用用户配置: " + EDGE_PROFILE + "）");
    }

    /**
     * 临时补充
     *
     * @return 拼接Edge储存用户数据的地址
     */
    private String getUserDataDir(){
        String username = System.getProperty("user.name");
        System.out.println("👲 当前系统用户名: " + username);
//        String username2 = System.getenv("USERNAME");
//        System.out.println("环境变量用户名: " + username2);

        return String.format( "C:\\Users\\%s\\AppData\\Local\\Microsoft\\Edge\\User Data", username );
    }
    
    /**
     * 步骤 2：导航至目标页面
     * 如果已登录应直接进入，否则会停留在登录页
     */
    public void navigateToTarget() throws InterruptedException {
        System.out.println("🌐 正在访问目标页面...");
        driver.get(TARGET_URL);
        
        // 给页面充分时间加载（特别是复用用户数据时可能有较多缓存读取）
        Thread.sleep(INITIAL_LOAD_DELAY);
        
        System.out.println("   [信息] 当前页面标题: " + driver.getTitle());
        System.out.println("   [信息] 当前页面 URL: " + driver.getCurrentUrl());
    }
    
    /**
     * 步骤 3：验证登录状态
     * 检测策略：先检查登录成功标识，如失败再检查是否停留在登录页
     * 
     * @return true 如果确认处于登录状态
     */
    public boolean verifyLoginStatus() {
        System.out.println("🔍 正在验证登录状态...");
        
        // 策略 1：检查登录成功标识（任一匹配即可）
        for (String xpath : LOGIN_SUCCESS_INDICATORS) {
            try {
                WebElement element = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath(xpath))
                );
                if (element.isDisplayed()) {
                    System.out.println("   [成功] 检测到登录标识: " + xpath);
                    System.out.println("✅ 登录状态验证通过，当前已处于教务系统");
                    return true;
                }
            } catch (Exception e) {
                // 当前 XPath 未找到，继续尝试下一个
                continue;
            }
        }
        
        // 策略 2：检查是否明显处于登录页（用户名/密码输入框存在）
        boolean isOnLoginPage = LOGIN_PAGE_INDICATORS.stream().anyMatch(xpath -> {
            try {
                return driver.findElement(By.xpath(xpath)).isDisplayed();
            } catch (Exception e) {
                return false;
            }
        });
        
        if (isOnLoginPage) {
            System.err.println("   [失败] 检测到登录页面元素，Cookie 可能已过期");
            printDebugInfo();
        } else {
            System.err.println("   [警告] 既未检测到登录标识，也未检测到登录页特征");
            System.err.println("          可能是网络延迟或页面结构变更");
            printDebugInfo();
        }
        
        return false;
    }
    
    /**
     * 打印调试信息（用于排查页面结构问题）
     */
    private void printDebugInfo() {
        System.err.println("═══════════════════════════════════════");
        System.err.println("🐛 调试信息:");
        System.err.println("当前 URL: " + driver.getCurrentUrl());
        System.err.println("当前标题: " + driver.getTitle());
        
        // 尝试提取页面所有按钮/链接文本，帮助用户定位正确 XPath
        System.err.println("页面交互元素快照:");
        try {
            List<WebElement> elements = driver.findElements(By.xpath("//a | //button"));
            int count = 0;
            for (WebElement elem : elements) {
                String text = elem.getText().trim();
                if (!text.isEmpty() && text.length() < 20) { // 过滤空文本和超长文本
                    System.err.println("  - [" + elem.getTagName() + "] " + text);
                    if (++count >= 10) break; // 只显示前 10 个
                }
            }
        } catch (Exception e) {
            System.err.println("  (无法提取页面元素)");
        }
        System.err.println("══════════════════════════════════════=");
    }
    
    /**
     * 步骤 4：资源清理
     * 根据 AUTO_CLOSE_BROWSER 配置决定是否关闭浏览器
     */
    public void cleanup() {
        if (driver == null) return;
        
        if (AUTO_CLOSE_BROWSER) {
            System.out.println("🔒 正在关闭浏览器...");
            try {
                driver.quit();
                System.out.println("✅ 浏览器已关闭");
            } catch (Exception e) {
                System.err.println("⚠️ 关闭浏览器时出错: " + e.getMessage());
            }
        } else {
            System.out.println("🔓 浏览器保持开启状态（请手动关闭）");
            System.out.println("   提示：如需自动关闭，请修改 AUTO_CLOSE_BROWSER = true");
            
            // 断开 Selenium 与浏览器的连接，但不关闭浏览器
            // 这样你可以手动操作浏览器，同时 Selenium 进程可以安全退出
            try {
                // Selenium 4 没有直接的 detach 方法，但 quit() 会关浏览器，
                // 所以我们这里只是打印提示，实际的 driver 对象会随 JVM 结束而断开
                System.out.println("   [信息] Selenium 会话已分离，浏览器保留");
            } catch (Exception ignored) {}
        }
    }
    
    // ==================== 公共接口（供后续模块调用）====================
    
    /**
     * 获取 WebDriver 实例（供模块 2/3/4 使用）
     * 注意：如果 AUTO_CLOSE_BROWSER 为 false，你需要在后续模块中手动管理生命周期
     * 
     * @return 当前 WebDriver 实例
     */
    public WebDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException("浏览器尚未初始化，请先调用 initEnvironment()");
        }
        return driver;
    }
    
    /**
     * 手动关闭浏览器（供外部模块在任务完成后调用）
     */
    public void closeBrowser() {
        AUTO_CLOSE_BROWSER = true;  // 临时开启自动关闭
        cleanup();
    }
}