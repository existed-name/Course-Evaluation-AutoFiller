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
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Scanner;

/**
 * 教学评价自动化 - 模块1：浏览器接管与登录确认
 * 适配：Java 21 + Selenium 4.40.0 + EdgeDriver 144.0
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @author <a href="https://chat.deepseek.com/"> DeepSeekV3.2 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @CreateTime 2026/1/31
 */

public class OldEvaluationAutomator {

    // ==================== 常量配置区（已按你的环境修改） ====================

    /** EdgeDriver 驱动路径 - 已更新为你的实际路径 */
    private static final String EDGE_DRIVER_PATH =
            "src/main/resources/msedgedriver.exe";
//            "D:\\Users\\Programming\\WebDriver\\EdgeDriver\\EdgeDriver144.0\\msedgedriver.exe";

    /** 教务系统登录页 URL（请替换为实际地址） */
    private static final String LOGIN_URL =
            // 从 aTrust 复制过来的链接
            "https://matrix.dean.swust.edu.cn/acadmicManager/index.cfm?event=studentPortal:DEFAULT_EVENT";

    /** 登录成功后主页特征元素 XPath */
    private static final String[] HOME_PAGE_INDICATORS = {
            // 去登录界面找各种词语摘下来
            "//*[@id=\"navAccountLink\"]", // 个人账户
            "//a[contains(text(),'个人账户')]",
            "//a[contains(text(),'档案')]",
            "//a[contains(text(),'考试')]",
            "//a[contains(text(),'选课')]",
            "//a[contains(text(),'评价')]",
            "//a[contains(text(),'注销')]",
            "//a[contains(text(),'成绩')]",
            "//a[contains(text(),'注销')]",
    };

    /** 手动登录等待时间（10秒） */
    private static final long MANUAL_LOGIN_TIMEOUT = 10 * 1000;//20000;

    /** 显式等待默认超时时间 */
    private static final Duration EXPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(10);

    /** 隐式等待全局超时时间 */
    private static final Duration IMPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(5);

    // ==================== 成员变量 ====================

    private WebDriver driver;
    private WebDriverWait wait;

    // ==================== 主程序入口 ====================

    public static void main(String[] args) {
        OldEvaluationAutomator automator = new OldEvaluationAutomator();
        try {
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
            automator.initBrowser();
            automator.takeoverBrowser();

            Scanner scanner = new Scanner( System.in );
            System.out.println( "--- 按下Enter结束 ---" );
            scanner.nextLine();
        } catch (Exception e) {
            System.err.println("❌ 自动化流程异常终止: " + e.getMessage());
            e.printStackTrace();
        }
        // 注意：模块1不关闭浏览器，方便查看状态；后续模块会调用 closeBrowser()
    }

    // ==================== 核心方法 ====================

    /**
     * 步骤 1：初始化浏览器配置（Selenium 4.40.0 语法）
     */
    public void initBrowser() throws IOException {
        System.out.println("🚀 正在初始化 Edge 浏览器...");

        // 强制结束Edge进程，避免之前的实例产生干扰
        Runtime.getRuntime().exec("taskkill /F /IM msedge.exe");
        Runtime.getRuntime().exec("taskkill /F /IM msedgedriver.exe");

        // 设置驱动路径
//        System.setProperty("webdriver.edge.driver", EDGE_DRIVER_PATH);
        System.setProperty("webdriver.edge.driver", loadEdgeDriver());

        // 配置 Edge 选项
        EdgeOptions options = new EdgeOptions();
        // 禁用自动化特征检测
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        // 使用用户数据（Cookie），这样浏览器会有你的登录状态
//        options.addArguments("user-data-dir=C:\\Users\\你的用户名\\AppData\\Local\\Microsoft\\Edge\\User Data");
        options.addArguments("user-data-dir=" + getUserDataDir() );
        options.addArguments("profile-directory=Default"); // 使用默认配置文件
        options.addArguments("--start-maximized");
        // 不显示"Chrome正在受自动化软件控制"
        options.addArguments("--disable-infobars");
        options.addArguments("--remote-allow-origins=*");

        // 实例化浏览器（Selenium 4 语法）
        this.driver = new EdgeDriver(options);

        // ✅ Selenium 4.40.0 修正：使用 Duration 单参数形式
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIMEOUT);

        // 初始化显式等待
        this.wait = new WebDriverWait(driver, EXPLICIT_WAIT_TIMEOUT);

        System.out.println("✅ 浏览器初始化成功（Selenium 4.40.0 兼容模式）");
    }


    /**
     * 临时补上
     *
     * @return EdgeDriver的绝对路径
     */
    public static String loadEdgeDriver() throws IOException{
        // 1. 获取 JAR 内部的资源流
        InputStream inputStream = OldEvaluationAutomator.class.getResourceAsStream("/msedgedriver.exe");

        // 2. 在系统临时目录下创建一个临时文件
        File tempDriver = File.createTempFile("msedgedriver", ".exe");
        tempDriver.deleteOnExit(); // 程序退出时自动删除

        // 3. 将资源流拷贝到临时文件
        Files.copy(inputStream, tempDriver.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // 4. 设置驱动路径
//        System.setProperty("webdriver.edge.driver", tempDriver.getAbsolutePath());

        return tempDriver.getAbsolutePath();
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
     * 步骤 2：浏览器接管核心逻辑
     */
    public void takeoverBrowser() throws InterruptedException {
        System.out.println("🌐 正在打开教务系统登录页面: " + LOGIN_URL);
        driver.get(LOGIN_URL);

        System.out.println("⏳ 请手动完成登录操作（输入账号、密码、验证码）...");
        System.out.println("   您有 " + (MANUAL_LOGIN_TIMEOUT / 1000) + " 秒时间完成登录");

        // 等待人工操作
        Thread.sleep(MANUAL_LOGIN_TIMEOUT);

        System.out.println("🔍 正在检测登录状态...");

        if (isLoginSuccessful()) {
            System.out.println("✅ 浏览器接管成功，当前处于个人主页。");
        } else {
            handleLoginFailure();
        }
    }

    /**
     * 检测登录状态 - 多元素检测
     */
    private boolean isLoginSuccessful() {
        for (String xpath : HOME_PAGE_INDICATORS) {
            try {
                WebElement element = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath(xpath))
                );
                if (element.isDisplayed()) {
                    System.out.println( "Xpath匹配成功:" + xpath );
                    return true;  // 任意一个匹配即认为登录成功
                }
            } catch (Exception e) {
                // 当前 XPath 未找到，继续检测下一个
                System.out.println( "Xpath匹配失败:" + xpath );
                continue;
            }
        }
        System.out.println( "所有 XPath 都不匹配" );
        return false;
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure() {
        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();

        System.err.println("═══════════════════════════════════════");
        System.err.println("❌ 登录状态检测失败");
        System.err.println("当前页面 URL: " + currentUrl);
        System.err.println("当前页面标题: " + pageTitle);
        System.err.println("═══════════════════════════════════════");

        // 调试辅助：打印页面源码前 1000 字符帮助定位问题
        String pageSource = driver.getPageSource();
//        System.err.println("页面源码片段: " + pageSource.substring(0, Math.min(1000, pageSource.length())));
        System.out.println( "页面源码: " );
        System.out.println( pageSource );
        System.err.println("═══════════════════════════════════════");

        throw new RuntimeException("未能在规定时间内检测到登录成功标识");
    }

    /**
     * 获取 WebDriver 实例（供模块 2/3/4 调用）
     * ⚠️ 当前模块 1 未使用，后续模块必需
     */
    public WebDriver getDriver() {
        return this.driver;
    }

    /**
     * 关闭浏览器资源（全部任务完成后调用）
     * ⚠️ 当前模块 1 未使用，最终模块必需
     */
    public void closeBrowser() {
        if (driver != null) {
            System.out.println("🔒 正在关闭浏览器...");
            driver.quit();
            System.out.println("✅ 浏览器已安全关闭");
        }
    }
}