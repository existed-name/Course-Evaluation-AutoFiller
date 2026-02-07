package com.github.existed_name.courseevaluationautofiller;

import com.github.existed_name.courseevaluationautofiller.config.SystemConfig;
import com.github.existed_name.courseevaluationautofiller.core.BrowserManager;
import com.github.existed_name.courseevaluationautofiller.core.DataExtractor;
import com.github.existed_name.courseevaluationautofiller.core.FormFiller;
import com.github.existed_name.courseevaluationautofiller.core.NavigationService;
import com.github.existed_name.courseevaluationautofiller.model.CourseEvaluation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 教学质量评价自动化 - 主程序入口
 *
 * <pre>
 * 功能流程：
 * 1. 启动浏览器并复用登录态
 * 2. 导航到评价列表页
 * 3. 提取所有待评价课程
 * 4. 循环填写每门课程的问卷
 * 5. 全部完成后关闭浏览器
 * </pre>
 *
 * v1.0 - 优化代码结构，增加空列表处理
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/2 11:59
 */
public class AutoEvaluationMain {

    private static BrowserManager browserManager;
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static NavigationService navigationService;
    private static DataExtractor dataExtractor;
    private static FormFiller formFiller;

    public static void main(String[] args) {
        try {
            setEncoding();
            printBanner();

            // 阶段1：环境初始化
            initializeEnvironment();

            // 阶段2：导航到评价页面
            navigateToEvaluationPage();

            // 阶段3：批量评价
            int[] results = batchEvaluate();

            // 阶段4：任务总结
            printSummary(results[0], results[1]);

        } catch (Exception e) {
            System.err.println("\n❌ 程序执行异常: " + e.getMessage());
            e.printStackTrace();

        } finally {
            cleanup();
        }
    }

    /**
     * 设置命令行字符编码为 UTF-8
     */
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
     * 打印程序标题
     */
    private static void printBanner() {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║                 课程评价收割机                ║");
        System.out.println("║         https://github.com/existed-name       ║");
        System.out.println("╚═══════════════════════════════════════════════╝\n");
    }

    /**
     * 阶段1：环境初始化
     */
    private static void initializeEnvironment() throws Exception {
        browserManager = new BrowserManager();
        browserManager.initEnvironment();
        browserManager.navigateToTarget();

        if (!browserManager.verifyLoginStatus()) {
            throw new RuntimeException("登录验证失败，请手动登录后重新运行");
        }

        driver = browserManager.getDriver();
        wait = browserManager.getWait();
        navigationService = new NavigationService(driver, wait);
        dataExtractor = new DataExtractor(driver, wait);
        formFiller = new FormFiller(driver, wait);
    }

    /**
     * 阶段2：导航到评价列表页
     */
    private static void navigateToEvaluationPage() {
        navigationService.navigateToEvaluationPage();
    }

    /**
     * 阶段3：批量评价所有课程
     *
     * @return int数组 [成功数量, 失败数量]
     */
    private static int[] batchEvaluate() {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║                   开始批量评价                ║");
        System.out.println("╚═══════════════════════════════════════════════╝\n");

        int successCount = 0;
        int failedCount = 0;
        int totalAttempts = 0;
        final int MAX_ATTEMPTS = 100;

        while (totalAttempts < MAX_ATTEMPTS) {
            totalAttempts++;

            // 重新检查待评价列表
            System.out.println(">>> 重新检查待评价列表...");
            List<CourseEvaluation> courses = dataExtractor.extractPendingCourses();

            // 🔑 如果列表为空，说明全部完成
            if (courses.isEmpty()) {
                System.out.println("✅ 所有课程已评价完成！\n");
                break;
            }

            // 只处理第一门课程
            CourseEvaluation course = courses.get(0);

            System.out.printf("【进度: 已完成 %d 门】正在处理: %s%n",
                    successCount, course.getFullInfo());

            try {
                // 重新获取按钮元素（避免 stale element）
                WebElement freshButton = refindEvalButton(course);

                if (freshButton == null) {
                    System.err.println("   ✗ 无法定位评价按钮，跳过此课程");
                    failedCount++;
                    continue;
                }

                // 填写问卷
                evaluateSingleCourse(freshButton, course);

                successCount++;
                System.out.println("✅ 完成\n");

                // 短暂等待，让列表刷新完成
                Thread.sleep(1000);

            } catch (Exception e) {
                failedCount++;
                System.err.printf("❌ 失败: %s%n", e.getMessage());
                System.err.println("   继续处理下一门课程...\n");

                // 尝试恢复
                recoverFromError();
            }
        }

        if (totalAttempts >= MAX_ATTEMPTS) {
            System.err.println("⚠️ 达到最大尝试次数，终止任务");
        }

        return new int[]{successCount, failedCount};
    }

    /**
     * 评价单门课程
     */
    private static void evaluateSingleCourse(WebElement evalButton, CourseEvaluation course)
            throws Exception {
        // 点击"网上评价"按钮，进入问卷
        navigationService.navigateToQuestionnaire(evalButton, course.getFullInfo());

        // 填写并提交问卷
        formFiller.fillAndSubmitQuestionnaire(course.getFullInfo());
    }

    /**
     * 重新查找评价按钮（避免 stale element 问题）
     */
    private static WebElement refindEvalButton( CourseEvaluation course) {
        try {
            // 根据课程名称和教师名称定位所在行
            String rowXPath = String.format(
                    "//tr[contains(., '%s') and contains(., '%s')]",
                    course.courseName(),
                    course.teacherName()
            );

            WebElement row = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath(rowXPath))
            );

            // 在行内查找评价按钮
            for (String buttonXPath : SystemConfig.XPATH_EVAL_BUTTON) {
                try {
                    return row.findElement(By.xpath(buttonXPath));
                } catch (Exception e) {
                    continue;
                }
            }

            return null;

        } catch (Exception e) {
            System.err.println("   ⚠ 重新定位按钮失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 错误恢复机制
     */
    private static void recoverFromError() {
        try {
            driver.navigate().back();
            Thread.sleep(2000);
        } catch (Exception backError) {
            System.err.println("   ⚠ 无法返回列表页，尝试重新导航...");
            try {
                navigationService.navigateToEvaluationPage();
            } catch (Exception navError) {
                System.err.println("   ✗ 重新导航失败");
            }
        }
    }

    /**
     * 阶段4：打印任务总结
     */
    private static void printSummary(int successCount, int failedCount) {
        System.out.println("╔═══════════════════════════════════════════════╗");
        System.out.println("║   任务执行完成                                ║");
        System.out.println("╠═══════════════════════════════════════════════╣");
        System.out.printf ("║   成功: %d 门                                  ║%n", successCount);
        System.out.printf ("║   失败: %d 门                                  ║%n", failedCount);
        System.out.printf ("║   总计: %d 门                                  ║%n", successCount + failedCount);
        System.out.println("╚═══════════════════════════════════════════════╝\n");

        if (failedCount > 0) {
            System.out.println("⚠️ 部分课程评价失败，请手动检查");
        } else {
            System.out.println("🎉 所有课程评价已完成！");
        }
    }

    /**
     * 资源清理
     */
    private static void cleanup() {
        if (browserManager != null) {
            System.out.println("\n按回车键关闭程序...");
            try {
                System.in.read();
            } catch (Exception ignored) {}

            browserManager.cleanup();
        }
    }
}