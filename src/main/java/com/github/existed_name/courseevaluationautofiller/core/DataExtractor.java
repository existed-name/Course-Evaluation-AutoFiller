package com.github.existed_name.courseevaluationautofiller.core;

import com.github.existed_name.courseevaluationautofiller.config.SystemConfig;
import com.github.existed_name.courseevaluationautofiller.model.CourseEvaluation;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;


/**
 * 数据提取服务
 * 负责从评价列表页提取课程信息
 * v1.0 增加空列表检测
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/2
 */
public class DataExtractor {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public DataExtractor(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    /**
     * 提取所有待评价的课程
     *
     * @return 待评价课程列表（如果全部完成则返回空列表）
     */
    public List< CourseEvaluation > extractPendingCourses() {
        System.out.println("=== 开始解析评价列表 ===");

        try {
            // 🔑 新增：先检查是否已完成所有评价
            if (isAllCompleted()) {
                System.out.println("✓ 检测到完成提示，所有课程已评价完成");
                return new ArrayList<>(); // 返回空列表
            }

            // 步骤1：定位评价表格
            WebElement table = locateEvaluationTable();
            System.out.println("✓ 成功定位到评价表格");

            // 步骤2：提取所有行
            List<WebElement> rows = table.findElements(By.xpath( SystemConfig.XPATH_TABLE_ROWS));
            System.out.printf("✓ 表格共有 %d 行数据%n", rows.size());

            // 🔑 新增：如果表格存在但无数据行，也认为是完成状态
            if (rows.isEmpty()) {
                System.out.println("✓ 表格为空，所有课程已评价完成");
                return new ArrayList<>();
            }

            // 步骤3：解析每一行
            List<CourseEvaluation> courses = new ArrayList<>();

            for (int i = 0; i < rows.size(); i++) {
                WebElement row = rows.get(i);
                try {
                    CourseEvaluation course = parseRow(row, i + 1);
                    if (course.needsEvaluation()) {
                        courses.add(course);
                        System.out.printf("  [%d] %s%n", i + 1, course.getFullInfo());
                    }
                } catch (Exception e) {
                    System.err.printf("⚠ 第 %d 行解析失败: %s%n", i + 1, e.getMessage());
                }
            }

            // 步骤4：输出统计信息
            printExtractionSummary(courses);

            return courses;

        } catch (Exception e) {
            // 🔑 改进：如果找不到表格，检查是否是因为已完成
            if (isAllCompleted()) {
                System.out.println("✓ 检测到完成提示，所有课程已评价完成");
                return new ArrayList<>();
            }

            // 确实是错误
            System.err.println("✗ 列表解析失败: " + e.getMessage());
            printDebugInfo();
            throw new RuntimeException("无法提取课程列表", e);
        }
    }

    /**
     * 🔑 新增方法：检查是否已完成所有评价
     * 检测特征：出现"太棒了"提示框
     */
    private boolean isAllCompleted() {
        try {
            // 检查完成提示元素是否存在
            for (String xpath : SystemConfig.XPATH_ALL_COMPLETED_INDICATORS) {
                try {
                    WebElement completionNotice = driver.findElement(By.xpath(xpath));
                    if (completionNotice.isDisplayed()) {
                        return true;
                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 定位评价列表表格
     * 使用数组逐个尝试
     */
    private WebElement locateEvaluationTable() {
        System.out.println("正在定位评价表格...");

        for (String xpath : SystemConfig.XPATH_EVALUATION_TABLE) {
            try {
                WebElement table = wait.until(
                        ExpectedConditions.presenceOfElementLocated(By.xpath(xpath))
                );
                System.out.println("   [成功] 使用XPath: " + xpath);
                return table;
            } catch (Exception e) {
                System.out.println("   [跳过] XPath无效: " + xpath);
            }
        }

        throw new RuntimeException("所有XPath方案均失败，未找到评价表格");
    }

    /**
     * 解析单行数据
     * 表格结构：序号 | 任课单位 | 课程-教师 | 学分 | 周次 | 按钮
     */
    private CourseEvaluation parseRow(WebElement row, int rowNumber) {
        try {
            // 提取序号（第1列）
            String index = extractCellText(row, SystemConfig.XPATH_CELL_INDEX, String.valueOf(rowNumber));

            // 提取任课单位（第2列）
            String department = extractCellText(row, SystemConfig.XPATH_CELL_DEPARTMENT, "未知单位");

            // 提取课程和教师（第3列，格式："课程 - 教师"）
            String courseTeacherText = extractCellText(row, SystemConfig.XPATH_CELL_COURSE_TEACHER, "");
            String[] parts = courseTeacherText.split(" - ");
            String courseName = parts.length > 0 ? parts[0].trim() : "未知课程";
            String teacherName = parts.length > 1 ? parts[1].trim() : "未知教师";

            // 提取学分（第4列）
            String credits = extractCellText(row, SystemConfig.XPATH_CELL_CREDITS, "0.00");

            // 提取周次（第5列）
            String weeks = extractCellText(row, SystemConfig.XPATH_CELL_WEEKS, "未知");

            // 提取评价按钮（第6列）
            WebElement evalButton = extractEvalButton(row);

            return new CourseEvaluation(
                    index, department, courseName, teacherName,
                    credits, weeks, evalButton
            );

        } catch (Exception e) {
            throw new RuntimeException("行解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取单元格文本
     */
    private String extractCellText(WebElement row, String xpath, String defaultValue) {
        try {
            return row.findElement(By.xpath(xpath)).getText().trim();
        } catch (NoSuchElementException e) {
            return defaultValue;
        }
    }

    /**
     * 提取评价按钮
     * 先定位按钮列，再在列内查找按钮
     */
    private WebElement extractEvalButton(WebElement row) {
        try {
            // 先定位第6列（按钮列）
            WebElement buttonCell = row.findElement(By.xpath(SystemConfig.XPATH_CELL_BUTTON));

            // 在按钮列内尝试多种XPath
            for (String xpath : SystemConfig.XPATH_EVAL_BUTTON) {
                try {
                    return buttonCell.findElement(By.xpath(xpath));
                } catch (NoSuchElementException e) {
                    continue;
                }
            }

            System.err.println("⚠ 未找到评价按钮");
            return null;

        } catch (NoSuchElementException e) {
            System.err.println("⚠ 未找到按钮列");
            return null;
        }
    }

    /**
     * 打印提取结果汇总
     */
    private void printExtractionSummary(List<CourseEvaluation> courses) {
        System.out.println("\n┌─────────────────────────────────");
        System.out.printf("│ 发现 %d 门待评价课程%n", courses.size());
        System.out.println("└─────────────────────────────────");

        if (!courses.isEmpty()) {
            System.out.println("\n待评价课程清单：");
            for (int i = 0; i < courses.size(); i++) {
                CourseEvaluation course = courses.get(i);
                System.out.printf("  %d. %s - %s（%s）%n",
                        i + 1,
                        course.courseName(),
                        course.teacherName(),
                        course.department());
            }
        }

        System.out.println("\n=== 列表解析完成 ===\n");
    }

    /**
     * 打印调试信息
     */
    private void printDebugInfo() {
        System.err.println("┌─────────────────────────────────");
        System.err.println("│ 调试信息:");
        System.err.println("│ URL:  " + driver.getCurrentUrl());
        System.err.println("│ 标题: " + driver.getTitle());
        System.err.println("└─────────────────────────────────");
    }
}