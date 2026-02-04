package com.github.existed_name.courseevaluationautofiller.util;

import com.github.existed_name.courseevaluationautofiller.config.XPathConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.github.existed_name.courseevaluationautofiller.config.XPathConstants.XPATH_EVALUATION_MENU_BY_HREF;

/**
 * 页面导航器
 * 负责从教务系统首页导航到评价列表页
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/1
 */
public class EvaluationNavigator {
    
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    private static final String EXPECTED_URL_KEYWORD = "evaluate";//"evaluation";
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    public EvaluationNavigator(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }
    
    /**
     * 导航到评价列表页
     * 
     * @throws TimeoutException 如果10秒内未找到菜单或页面未跳转
     */
    public void navigateToEvaluationPage() {
        System.out.println("=== 开始导航到教学质量评价页面 ===");
        printCurrentPageInfo();
        
        try {
            // 步骤1：等待并点击菜单
            WebElement menuLink = waitForEvaluationMenu();
            System.out.println("✓ 找到评价菜单，准备点击...");
            
            menuLink.click();
            System.out.println("✓ 菜单已点击");
            
            // 步骤2：等待页面跳转
            waitForPageLoad();
            
            // 步骤3：验证跳转成功
            printCurrentPageInfo();
            System.out.println("=== 导航成功完成 ===\n");
            
        } catch (TimeoutException e) {
            System.err.println("✗ 导航失败：超时未找到目标元素");
            printCurrentPageInfo();
            throw new RuntimeException("无法导航到评价页面", e);
        } catch (Exception e) {
            System.err.println("✗ 导航过程中发生异常：" + e.getMessage());
            printCurrentPageInfo();
            throw new RuntimeException("导航异常", e);
        }
    }
    
    /**
     * 等待教学质量评价菜单出现并返回
     */
    private WebElement waitForEvaluationMenu() {
        System.out.println("正在等待菜单元素加载...");
        
        try {
            // 优先使用文本定位
            return wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath( XPathConstants.XPATH_EVALUATION_MENU)
            ));
        } catch (TimeoutException e1) {
            System.out.println("尝试备用定位方式（通过href）...");
            
            try {
                // 备用方案：通过href定位
                return wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath(XPATH_EVALUATION_MENU_BY_HREF)
                ));
            } catch (TimeoutException e2) {
                System.err.println("两种定位方式均失败");
                throw new TimeoutException("未找到教学质量评价菜单");
            }
        }
    }
    
    /**
     * 等待页面加载完成
     * 通过URL变化判断是否跳转成功
     */
    private void waitForPageLoad() {
        System.out.println("正在等待页面跳转...");
        
        try {
            // 方式1：等待URL包含关键字
            wait.until(ExpectedConditions.urlContains(EXPECTED_URL_KEYWORD));
            System.out.println("✓ 检测到URL变化");
            
        } catch (TimeoutException e) {
            System.out.println("⚠ URL未包含预期关键字，尝试检测DOM变化...");
            
            // 方式2：等待表格出现
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath( XPathConstants.XPATH_EVALUATION_TABLE)
                ));
                System.out.println("✓ 检测到评价表格已加载");
                
            } catch (TimeoutException e2) {
                // 尝试备用表格定位
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath( XPathConstants.XPATH_EVALUATION_TABLE_BY_HEADER)
                ));
                System.out.println("✓ 通过表头定位到评价表格");
            }
        }
    }
    
    /**
     * 打印当前页面信息（用于调试）
     */
    private void printCurrentPageInfo() {
        System.out.println("┌─────────────────────────────────");
        System.out.println("│ 当前页面标题: " + driver.getTitle());
        System.out.println("│ 当前页面URL:  " + driver.getCurrentUrl());
        System.out.println("└─────────────────────────────────");
    }
}