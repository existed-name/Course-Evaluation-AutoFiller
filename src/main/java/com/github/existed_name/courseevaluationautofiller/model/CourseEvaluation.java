package com.github.existed_name.courseevaluationautofiller.model;

import org.openqa.selenium.WebElement;

/**
 * 课程评价数据模型
 * 对应教学质量评价列表页面的表格结构（6列）
 * 
 * @param index 序号（第1列）
 * @param department 任课单位（第2列，如：计科）
 * @param courseName 课程名称（第3列，从"课程 - 教师"中提取）
 * @param teacherName 教师姓名（第3列，从"课程 - 教师"中提取）
 * @param credits 学分（第4列，如：2.50）
 * @param weeks 周次（第5列，如：02-09）
 * @param evalButton 网上评价按钮（第6列）
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/2 11:59
 */
public record CourseEvaluation(
    String index,
    String department,
    String courseName,
    String teacherName,
    String credits,
    String weeks,
    WebElement evalButton
) {
    
    /**
     * 判断是否需要评价
     * 根据你的需求：只要出现在列表里就是待评价
     */
    public boolean needsEvaluation() {
        return evalButton != null && evalButton.isDisplayed();
    }
    
    /**
     * 获取课程完整信息（用于日志输出）
     */
    public String getFullInfo() {
        return String.format("[%s] %s - %s（%s学分，第%s周）", 
            index, courseName, teacherName, credits, weeks);
    }
    
    /**
     * 格式化输出
     */
    @Override
    public String toString() {
        return String.format("%s | %s | %s - %s | %s学分 | %s周 | %s", 
            index, 
            department, 
            courseName, 
            teacherName, 
            credits, 
            weeks,
            evalButton != null ? "可评价" : "无按钮");
    }
}