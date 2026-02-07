package com.github.existed_name.courseevaluationautofiller.core;

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
import java.util.Arrays;

import static com.github.existed_name.courseevaluationautofiller.config.SystemConfig.*;

/**
 * 浏览器管理器
 * 负责浏览器的启动、配置、登录验证和资源清理
 * 整合自原 EvaluationAutomator 类
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/2 11:59
 */
public class BrowserManager {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    /**
     * 初始化环境
     * 包含：进程清理 → 驱动配置 → 浏览器启动
     */
    public void initEnvironment() throws IOException, InterruptedException {
        System.out.println("🚀 开始初始化自动化环境...");
        
        if (FORCE_KILL_EDGE_PROCESS) {
            killEdgeProcesses();
            Thread.sleep(2000); // 等待进程完全释放
        }
        
        setupDriver();
        configureBrowser();
        
        System.out.println("✅ 环境初始化完成\n");
    }
    
    /**
     * 强制结束 Edge 相关进程
     */
    private void killEdgeProcesses() throws IOException {
        System.out.println("   [系统] 正在清理 Edge 进程...");
        try {
            Runtime.getRuntime().exec("taskkill /F /IM msedge.exe");
            Runtime.getRuntime().exec("taskkill /F /IM msedgedriver.exe");
            System.out.println("   [系统] 进程清理完成");
        } catch (IOException e) {
            System.err.println("   [警告] 进程清理失败: " + e.getMessage());
        }
    }

    /**
     * 配置 WebDriver 系统属性
     */
    private void setupDriver() throws IOException{
        String path = loadEdgeDriverPath();
        System.out.println( "   [配置] 驱动路径: " + path );
        System.setProperty( "webdriver.edge.driver", path );
    }

    /**
     * 从 resources 读取 msedgedriver.exe 临时拿出来
     *
     * @return 临时创建的 EdgeDriver 的绝对路径
     */
    private String loadEdgeDriverPath() throws IOException{
        // 1. 获取 JAR 内部的资源流
        InputStream inputStream = BrowserManager.class.getResourceAsStream( "/msedgedriver.exe" );

        // 2. 在系统临时目录下创建一个临时文件
        File tempDriver = File.createTempFile("msedgedriver", ".exe");
        tempDriver.deleteOnExit(); // 程序退出时自动删除

        // 3. 将资源流拷贝到临时文件
        Files.copy(inputStream, tempDriver.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return tempDriver.getAbsolutePath();
    }
    
    /**
     * 配置并启动浏览器
     */
    private void configureBrowser() {
        EdgeOptions options = new EdgeOptions();
        
        // 反自动化检测
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--disable-infobars");
        
        // 窗口设置
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        
        // 复用用户配置（Cookie、缓存、登录状态）
        options.addArguments("user-data-dir=" + loadUserDataDir());
        options.addArguments("profile-directory=" + EDGE_PROFILE);
        
        // 稳定性配置
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        
        this.driver = new EdgeDriver(options);
        
        // 设置等待策略
        driver.manage().timeouts().implicitlyWait(IMPLICIT_WAIT_TIMEOUT);
        this.wait = new WebDriverWait(driver, EXPLICIT_WAIT_TIMEOUT);

        System.out.println("   [配置] 浏览器已启动(配置: " + EDGE_PROFILE + ")");
    }

    /**
     * 获取系统用户名，拼接 Edge 数据储存目录(保留登录 Cookie 的关键)
     *
     * @return Edge 储存用户数据的绝对路径
     */
    private String loadUserDataDir(){
        String username = System.getProperty("user.name");
        System.out.println("👲 当前系统用户名: " + username);
//        String username2 = System.getenv("USERNAME");
//        System.out.println("环境变量用户名: " + username2);

        // Windows路径，注意双反斜杠
        return String.format( "C:\\Users\\%s\\AppData\\Local\\Microsoft\\Edge\\User Data", username );
    }

    /**
     * 导航至目标页面
     */
    public void navigateToTarget() throws InterruptedException {
        System.out.println("🌐 正在访问目标页面...");
        driver.get(TARGET_URL);
        
        Thread.sleep(INITIAL_LOAD_DELAY); // 仅在启动时使用一次
        
        System.out.println("   [信息] 当前页面: " + driver.getTitle());
        System.out.println("   [信息] URL: " + driver.getCurrentUrl());
        System.out.println();
    }
    
    /**
     * 验证登录状态
     */
    public boolean verifyLoginStatus() {
        System.out.println("🔍 正在验证登录状态...");
        
        // 策略1：检查登录成功标识
        for (String xpath : LOGIN_SUCCESS_INDICATORS) {
            try {
                WebElement element = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.xpath(xpath))
                );
                if (element.isDisplayed()) {
                    System.out.println("   [成功] 检测到登录标识: " + xpath);
                    System.out.println("✅ 登录状态验证通过\n");
                    return true;
                }
            } catch (Exception e) {
                continue;
            }
        }
        
        // 策略2：检查是否在登录页
        boolean isOnLoginPage = Arrays.stream(LOGIN_PAGE_INDICATORS)
                .anyMatch(xpath -> {
                    try {
                        return driver.findElement(By.xpath(xpath)).isDisplayed();
                    } catch (Exception e) {
                        return false;
                    }
                });
        
        if (isOnLoginPage) {
            System.err.println("   [失败] 检测到登录页面，Cookie可能已过期");
        } else {
            System.err.println("   [警告] 未检测到明确的登录标识");
        }
        
        printDebugInfo();
        return false;
    }
    
    /**
     * 打印调试信息
     */
    private void printDebugInfo() {
        System.err.println("═══════════════════════════════════════");
        System.err.println("🐛 调试信息:");
        System.err.println("URL: " + driver.getCurrentUrl());
        System.err.println("标题: " + driver.getTitle());
        System.err.println("═══════════════════════════════════════\n");
    }
    
    /**
     * 资源清理
     */
    public void cleanup() {
        if (driver == null) return;
        
        if (AUTO_CLOSE_BROWSER) {
            System.out.println("🔒 正在关闭浏览器...");
            try {
                driver.quit();
                System.out.println("✅ 浏览器已关闭");
            } catch (Exception e) {
                System.err.println("⚠️ 关闭失败: " + e.getMessage());
            }
        } else {
            System.out.println("🔓 浏览器保持开启（手动关闭或设置 AUTO_CLOSE_BROWSER=true）");
        }
    }
    
    /**
     * 获取 WebDriver 实例
     */
    public WebDriver getDriver() {
        if (driver == null) {
            throw new IllegalStateException("浏览器未初始化，请先调用 initEnvironment()");
        }
        return driver;
    }
    
    /**
     * 获取 WebDriverWait 实例
     */
    public WebDriverWait getWait() {
        if (wait == null) {
            throw new IllegalStateException("等待器未初始化");
        }
        return wait;
    }
    
    /**
     * 手动关闭浏览器
     */
    public void closeBrowser() {
        AUTO_CLOSE_BROWSER = true;
        cleanup();
    }
}