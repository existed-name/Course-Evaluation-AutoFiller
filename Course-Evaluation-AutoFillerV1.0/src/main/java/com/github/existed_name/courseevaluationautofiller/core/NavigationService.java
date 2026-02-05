package com.github.existed_name.courseevaluationautofiller.core;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;

import static com.github.existed_name.courseevaluationautofiller.config.SystemConfig.*;

/**
 * 页面导航服务
 * 负责在教务系统各页面间导航
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/2 11:59
 */
public class NavigationService {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    public NavigationService(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }
    
    /**
     * 导航到教学质量评价列表页
     */
    public void navigateToEvaluationPage() {
        System.out.println("=== 开始导航到教学质量评价页面 ===");
        printCurrentPageInfo();
        
        try {
            // 步骤1：点击菜单
            WebElement menuLink = findEvaluationMenu();
            System.out.println("✓ 找到评价菜单，准备点击...");
            
            menuLink.click();
            System.out.println("✓ 菜单已点击");
            
            // 步骤2：等待页面跳转
            waitForEvaluationPageLoad();
            
            printCurrentPageInfo();
            System.out.println("=== 导航成功完成 ===\n");
            
        } catch (Exception e) {
            System.err.println("✗ 导航失败: " + e.getMessage());
            printCurrentPageInfo();
            throw new RuntimeException("无法导航到评价页面", e);
        }
    }
    
    /**
     * 查找教学质量评价菜单
     * 使用数组逐个尝试不同的XPath
     */
    private WebElement findEvaluationMenu() {
        System.out.println("正在定位评价菜单...");
        
        for (String xpath : XPATH_EVALUATION_MENU) {
            try {
                WebElement element = wait.until(
                    ExpectedConditions.elementToBeClickable(By.xpath(xpath))
                );
                System.out.println("   [成功] 使用XPath: " + xpath);
                return element;
            } catch (TimeoutException e) {
                System.out.println("   [跳过] XPath无效: " + xpath);
            }
        }
        
        throw new TimeoutException("所有XPath方案均失败，未找到评价菜单");
    }
    
    /**
     * 等待评价页面加载完成
     */
    private void waitForEvaluationPageLoad() {
        System.out.println("正在等待页面跳转...");
        
        // 方式1：检查URL特征
        boolean urlMatched = tryWaitForUrl();
        
        // 方式2：检查页面特征元素
        if (!urlMatched) {
            System.out.println("⚠ URL未包含预期关键字，尝试检测DOM元素...");
            tryWaitForPageElements();
        }
    }
    
    /**
     * 尝试通过URL判断页面跳转
     */
    private boolean tryWaitForUrl() {
        try {
            wait.until(driver -> {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                return Arrays.stream(EVALUATION_URL_KEYWORDS)
                        .anyMatch(currentUrl::contains);
            });
            System.out.println("✓ 检测到URL变化");
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
    
    /**
     * 尝试通过页面元素判断页面跳转
     */
    private void tryWaitForPageElements() {
        for (String xpath : EVALUATION_PAGE_INDICATORS) {
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath(xpath)
                ));
                System.out.println("✓ 检测到页面特征元素");
                return;
            } catch (TimeoutException e) {
                continue;
            }
        }
        throw new TimeoutException("页面加载超时，未检测到预期元素");
    }
    
    /**
     * 导航到问卷页面（点击"网上评价"按钮）
     * 
     * @param evalButton 网上评价按钮
     * @param courseInfo 课程信息（用于日志）
     */
    public void navigateToQuestionnaire(WebElement evalButton, String courseInfo) {
        System.out.println(">>> 准备进入问卷: " + courseInfo);
        
        try {
            evalButton.click();
            System.out.println("   ✓ 已点击\"网上评价\"按钮");
            
            // 等待问卷页面加载
            waitForQuestionnairePageLoad();
            
            System.out.println("   ✓ 问卷页面加载完成\n");
            
        } catch (Exception e) {
            System.err.println("   ✗ 进入问卷失败: " + e.getMessage());
            throw new RuntimeException("无法打开问卷页面", e);
        }
    }
    
    /**
     * 等待问卷页面加载完成
     */
    private void waitForQuestionnairePageLoad() {
        // 方式1：检查URL特征
        boolean urlMatched = false;
        try {
            wait.until(driver -> {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                return Arrays.stream(QUESTIONNAIRE_URL_KEYWORDS)
                        .anyMatch(currentUrl::contains);
            });
            urlMatched = true;
        } catch (TimeoutException e) {
            // URL检查失败，继续尝试元素检查
        }
        
        // 方式2：检查页面特征元素
        if (!urlMatched) {
            for (String xpath : QUESTIONNAIRE_PAGE_INDICATORS) {
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath(xpath)
                    ));
                    return;
                } catch (TimeoutException e) {
                    continue;
                }
            }
            throw new TimeoutException("问卷页面加载超时");
        }
    }
    
    /**
     * 打印当前页面信息
     */
    private void printCurrentPageInfo() {
        System.out.println("┌─────────────────────────────────");
        System.out.println("│ 标题: " + driver.getTitle());
        System.out.println("│ URL:  " + driver.getCurrentUrl());
        System.out.println("└─────────────────────────────────");
    }
}