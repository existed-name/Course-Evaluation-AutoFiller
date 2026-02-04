package com.github.existed_name.courseevaluationautofiller;

import com.github.existed_name.courseevaluationautofiller.model.EvaluationSubject;
import com.github.existed_name.courseevaluationautofiller.util.EvaluationNavigator;
import com.github.existed_name.courseevaluationautofiller.util.SubjectExtractor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 第二阶段测试主程序
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 */
public class Phase2Test {
    
    public static void main(String[] args) {
        WebDriver driver = null;
        
        try {
            setEncoding();

            // 1. 启动浏览器（假设模块一已完成登录）
            driver = initializeDriver();
            System.out.println("浏览器启动成功，已登录教务系统\n");
            
            // 模拟：假设当前在教务系统首页
            // driver.get("https://your-university.edu.cn/jwxt/index.cfm");
            // Thread.sleep(2000); // 实际项目中不要用，这里仅为演示
            
            // 2. 导航到评价列表页
            EvaluationNavigator navigator = new EvaluationNavigator(driver);
            navigator.navigateToEvaluationPage();
            
            // 3. 提取待评价科目
            SubjectExtractor extractor = new SubjectExtractor(driver);
            List< EvaluationSubject > pendingSubjects = extractor.extractPendingSubjects();
            
            // 4. 验证结果
            if (pendingSubjects.isEmpty()) {
                System.out.println("✓ 没有待评价的科目，任务完成！");
            } else {
                System.out.printf("✓ 共发现 %d 门待评价科目，准备进入下一阶段...%n", 
                    pendingSubjects.size());
            }
            
            // 保持浏览器打开，方便观察
            System.out.println("\n按回车键关闭浏览器...");
            System.in.read();
            
        } catch (Exception e) {
            System.err.println("程序执行失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
                System.out.println("浏览器已关闭");
            }
        }
    }

    private static void setEncoding(){
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                // 1、强制将当前控制台输出代码页改为 65001 (UTF-8)
                new ProcessBuilder("cmd", "/c" , "chcp 65001" // " > nul" 不展示 "Active code page: 65001"
                ).inheritIO().start().waitFor();

                // 2. 关键：强制重置 Java 的标准输出/错误流为 UTF-8
                // 这样即便 JVM 启动时抓取的是 GBK，我们也会在运行中把它改掉
                System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
                System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
            } catch (Exception ignored) {}
        }
    }

    /**
     * 初始化WebDriver
     * （这里简化处理，实际应该复用模块一的配置）
     */
    private static WebDriver initializeDriver() throws IOException, InterruptedException{
        // 获取已初始化的 WebDriver 实例（已登录，在首页）
        EvaluationAutomator automator = new EvaluationAutomator();
        automator.initEnvironment();  // 这会启动浏览器并复用登录态
        automator.navigateToTarget();
        WebDriver driver = automator.getDriver();  // 关键：模块二通过此方法操作浏览器

        /*
        EdgeOptions options = new EdgeOptions();
        
        // 使用用户数据目录（绕过登录）
        options.addArguments("--user-data-dir=C:/EdgeUserData");
        options.addArguments("--profile-directory=Default");
        
        // 其他配置
        options.addArguments("--start-maximized");
        options.addArguments("--disable-blink-features=AutomationControlled");
        
        WebDriver driver = new EdgeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
         */
        
        return driver;
    }
}