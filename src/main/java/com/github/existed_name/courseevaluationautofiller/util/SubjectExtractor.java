package com.github.existed_name.courseevaluationautofiller.util;

import com.github.existed_name.courseevaluationautofiller.config.XPathConstants;
import com.github.existed_name.courseevaluationautofiller.model.EvaluationSubject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


/**
 * 科目列表提取器
 * 负责从评价列表页解析出所有待评价的科目信息
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 */
public class SubjectExtractor {
    
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(10);
    
    private final WebDriver driver;
    private final WebDriverWait wait;
    
    public SubjectExtractor(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }
    
    /**
     * 提取所有待评价的科目
     * 
     * @return 待评价科目列表
     * @throws RuntimeException 如果解析失败
     */
    public List< EvaluationSubject > extractPendingSubjects() {
        System.out.println("\n=== 开始解析评价列表 ===");
        
        try {
            // 步骤1：定位评价表格
            WebElement table = locateEvaluationTable();
            System.out.println("✓ 成功定位到评价表格");
            
            // 步骤2：提取所有行
            List<WebElement> allRows = table.findElements(By.xpath( XPathConstants.XPATH_TABLE_ROWS));
            System.out.printf("✓ 表格共有 %d 行数据%n", allRows.size());
            
            // 步骤3：解析每一行
            List<EvaluationSubject> allSubjects = new ArrayList<>();
            List<EvaluationSubject> pendingSubjects = new ArrayList<>();
            
            for (int i = 0; i < allRows.size(); i++) {
                WebElement row = allRows.get(i);
                try {
                    EvaluationSubject subject = parseRow(row, i + 1);
                    allSubjects.add(subject);
                    
                    if (subject.isPending()) {
                        pendingSubjects.add(subject);
                    }
                    
                } catch (Exception e) {
                    System.err.printf("⚠ 第 %d 行解析失败: %s%n", i + 1, e.getMessage());
                }
            }
            
            // 步骤4：输出统计信息
            printExtractionSummary(allSubjects, pendingSubjects);
            
            return pendingSubjects;
            
        } catch (Exception e) {
            System.err.println("✗ 列表解析失败：" + e.getMessage());
            printCurrentPageInfo();
            throw new RuntimeException("无法提取科目列表", e);
        }
    }
    
    /**
     * 定位评价列表表格
     */
    private WebElement locateEvaluationTable() {
        System.out.println("正在定位评价表格...");
        
        try {
            // 优先使用class/id定位
            return wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath( XPathConstants.XPATH_EVALUATION_TABLE)
            ));
        } catch (Exception e1) {
            System.out.println("尝试备用定位方式（通过表头）...");
            
            try {
                // 备用方案：通过表头定位
                return wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath( XPathConstants.XPATH_EVALUATION_TABLE_BY_HEADER)
                ));
            } catch (Exception e2) {
                throw new RuntimeException("未找到评价列表表格");
            }
        }
    }
    
    /**
     * 解析单行数据
     * 
     * @param row 表格行元素
     * @param rowNumber 行号（从1开始，用于日志）
     * @return EvaluationSubject对象
     */
    private EvaluationSubject parseRow(WebElement row, int rowNumber) {
        try {
            // 提取课程名称
            String courseName = extractCourseName(row);
            
            // 提取教师姓名
            String teacherName = extractTeacherName(row);
            
            // 提取状态
            String status = extractStatus(row);
            
            // 提取操作按钮
            WebElement actionButton = extractActionButton(row);
            
            EvaluationSubject subject = new EvaluationSubject(
                courseName, teacherName, status, actionButton
            );
            
            System.out.printf("  [%d] %s%n", rowNumber, subject);
            
            return subject;
            
        } catch (Exception e) {
            throw new RuntimeException("行解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 提取课程名称
     */
    private String extractCourseName(WebElement row) {
        try {
            // 尝试多种可能的列位置
            List<WebElement> cells = row.findElements(By.xpath( XPathConstants.XPATH_COURSE_NAME_CELL));
            
            for (WebElement cell : cells) {
                String text = cell.getText().trim();
                if (!text.isEmpty() && !text.matches("\\d+")) { // 排除纯数字（序号列）
                    return text;
                }
            }
            
            // 如果以上方法失败，尝试获取第一个非空单元格
            List<WebElement> allCells = row.findElements(By.tagName("td"));
            for (WebElement cell : allCells) {
                String text = cell.getText().trim();
                if (!text.isEmpty() && !text.matches("\\d+") && 
                    !text.contains("评价") && !text.contains("完成")) {
                    return text;
                }
            }
            
            return "未知课程";
            
        } catch (Exception e) {
            return "未知课程";
        }
    }
    
    /**
     * 提取教师姓名
     */
    private String extractTeacherName(WebElement row) {
        try {
            WebElement cell = row.findElement(By.xpath( XPathConstants.XPATH_TEACHER_NAME_CELL));
            return cell.getText().trim();
        } catch (NoSuchElementException e) {
            // 如果定位失败，尝试通过文本模式匹配（姓名通常2-4个汉字）
            List<WebElement> cells = row.findElements(By.tagName("td"));
            for (WebElement cell : cells) {
                String text = cell.getText().trim();
                if (text.matches("[\\u4e00-\\u9fa5]{2,4}")) { // 2-4个汉字
                    return text;
                }
            }
            return "未知教师";
        }
    }
    
    /**
     * 提取评价状态
     */
    private String extractStatus(WebElement row) {
        try {
            WebElement cell = row.findElement(By.xpath( XPathConstants.XPATH_STATUS_CELL));
            String status = cell.getText().trim();
            
            // 标准化状态文本
            if (status.contains("待评价") || status.contains("未评价")) {
                return "待评价";
            } else if (status.contains("已完成") || status.contains("已评价")) {
                return "已完成";
            }
            
            return status;
            
        } catch (NoSuchElementException e) {
            return "未知状态";
        }
    }
    
    /**
     * 提取操作按钮
     */
    private WebElement extractActionButton(WebElement row) {
        try {
            return row.findElement(By.xpath( XPathConstants.XPATH_ACTION_BUTTON));
        } catch (NoSuchElementException e) {
            System.err.println("⚠ 未找到操作按钮");
            return null;
        }
    }
    
    /**
     * 打印提取结果汇总
     */
    private void printExtractionSummary(List<EvaluationSubject> all, 
                                       List<EvaluationSubject> pending) {
        System.out.println("\n┌─────────────────────────────────");
        System.out.printf("│ 成功解析页面，发现 %d 门科目%n", all.size());
        System.out.printf("│ 其中待评价: %d 门%n", pending.size());
        System.out.printf("│ 已完成评价: %d 门%n", all.size() - pending.size());
        System.out.println("└─────────────────────────────────");
        
        if (!pending.isEmpty()) {
            System.out.println("\n待评价科目清单：");
            for (int i = 0; i < pending.size(); i++) {
                System.out.printf("  %d. %s - %s%n", 
                    i + 1, 
                    pending.get(i).courseName(), 
                    pending.get(i).teacherName());
            }
        }
        
        System.out.println("\n=== 列表解析完成 ===\n");
    }
    
    /**
     * 打印当前页面信息（用于调试）
     */
    private void printCurrentPageInfo() {
        System.out.println("┌─────────────────────────────────");
        System.out.println("│ 调试信息：");
        System.out.println("│ 当前URL:  " + driver.getCurrentUrl());
        System.out.println("│ 页面标题: " + driver.getTitle());
        System.out.println("└─────────────────────────────────");
    }
}