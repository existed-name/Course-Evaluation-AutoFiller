package com.github.existed_name.courseevaluationautofiller.core;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static com.github.existed_name.courseevaluationautofiller.config.SystemConfig.*;

/**
 * 表单填写服务
 * 负责自动填写教学质量评价问卷
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/2 11:59
 */
public class FormFiller {
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    public FormFiller(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }
    
    /**
     * 填写问卷并提交
     * 
     * @param courseInfo 课程信息（用于日志）
     */
    public void fillAndSubmitQuestionnaire(String courseInfo) {
        System.out.println(">>> 开始填写问卷: " + courseInfo);
        
        try {
            // 步骤1：等待问卷表格加载
            waitForQuestionnaireTable();
            
            // 步骤2：填写所有单选题（选择"非常满意"）
            fillAllQuestions();
            
            // 步骤3：填写评语
            fillComment();
            
            // 步骤4：提交问卷
            submitQuestionnaire();
            
            // 步骤5：等待返回列表页
            waitForReturnToList();
            
            System.out.println("   ✓ 问卷提交成功，已返回列表页\n");
            
        } catch (Exception e) {
            System.err.println("   ✗ 问卷填写失败: " + e.getMessage());
            throw new RuntimeException("无法完成问卷填写", e);
        }
    }
    
    /**
     * 等待问卷表格加载
     */
    private void waitForQuestionnaireTable() {
        System.out.println("   [1/4] 等待问卷表格加载...");
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath(XPATH_QUESTIONNAIRE_TABLE)
            ));
            System.out.println("   ✓ 问卷表格已加载");
        } catch (Exception e) {
            throw new RuntimeException("问卷表格加载超时", e);
        }
    }
    
    /**
     * 填写所有单选题
     * 策略：找到所有 class="quota ltr" 的单元格，点击其中的 data-opt="1"
     */
    private void fillAllQuestions() {
        System.out.println("   [2/4] 开始填写选择题...");
        
        try {
            // 查找所有选项单元格
            List<WebElement> optionCells = driver.findElements(
                By.xpath(XPATH_ALL_OPTION_CELLS)
            );
            
            System.out.printf("   发现 %d 道题目%n", optionCells.size());
            
            int successCount = 0;
            for (int i = 0; i < optionCells.size(); i++) {
                WebElement cell = optionCells.get(i);
                try {
                    // 在单元格内查找 data-opt="1" 的选项
                    WebElement option = cell.findElement(
                        By.xpath(XPATH_OPTION_VERY_SATISFIED)
                    );
                    
                    // 点击选项
                    clickElement(option);
                    successCount++;
                    
                    // 短暂延迟，让页面响应
                    Thread.sleep(SHORT_DELAY);
                    
                } catch (Exception e) {
                    System.err.printf("   ⚠ 第 %d 题填写失败: %s%n", i + 1, e.getMessage());
                }
            }
            
            System.out.printf("   ✓ 已填写 %d/%d 道题目%n", successCount, optionCells.size());
            
        } catch (Exception e) {
            throw new RuntimeException("选择题填写失败", e);
        }
    }
    
    /**
     * 填写评语文本框
     */
    private void fillComment() {
        System.out.println("   [3/4] 填写评语...");
        
        try {
            WebElement commentBox = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath(XPATH_COMMENT_TEXTAREA)
                )
            );
            
            commentBox.clear();
            commentBox.sendKeys(DEFAULT_COMMENT);
            
            System.out.println("   ✓ 评语已填写: \"" + DEFAULT_COMMENT + "\"");
            
        } catch (Exception e) {
            throw new RuntimeException("评语填写失败", e);
        }
    }
    
    /**
     * 提交问卷
     */
    private void submitQuestionnaire() {
        System.out.println("   [4/4] 提交问卷...");
        
        try {
            WebElement submitButton = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.xpath(XPATH_SUBMIT_BUTTON)
                )
            );
            
            // 检查按钮是否可用（可能有disabled属性）
            String disabled = submitButton.getAttribute("disabled");
            if (disabled != null && !disabled.isEmpty()) {
                System.out.println("   [提示] 按钮初始为禁用状态，尝试移除disabled属性...");
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].removeAttribute('disabled');", submitButton
                );
            }
            
            // 点击提交按钮
            clickElement(submitButton);
            
            System.out.println("   ✓ 已点击提交按钮");
            
        } catch (Exception e) {
            throw new RuntimeException("提交按钮点击失败", e);
        }
    }
    
    /**
     * 等待返回到列表页
     */
    private void waitForReturnToList() {
        System.out.println("   等待返回列表页...");
        
        try {
            // 等待URL变化（从 evaluateResponse 返回到 evaluate）
            wait.until(driver -> {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                return currentUrl.contains("evaluate") && 
                       !currentUrl.contains("evaluateresponse");
            });
            
            // 等待列表表格出现
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath(XPATH_EVALUATION_TABLE[0])
            ));
            
        } catch (Exception e) {
            System.err.println("   ⚠ 等待返回列表页超时，继续执行");
        }
    }
    
    /**
     * 点击元素（带异常处理和JavaScript备用方案）
     */
    private void clickElement(WebElement element) {
        try {
            element.click();
        } catch (Exception e) {
            // 如果普通点击失败，尝试JavaScript点击
            try {
                ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].click();", element
                );
            } catch (Exception jsError) {
                throw new RuntimeException("元素点击失败", jsError);
            }
        }
    }
}