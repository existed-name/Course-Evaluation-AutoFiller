package com.github.existed_name.courseevaluationautofiller.model;

import org.openqa.selenium.WebElement;

/**
 * 评价科目数据模型
 * 
 * @param courseName 课程名称（如：数据结构）
 * @param teacherName 教师姓名（如：张三）
 * @param status 评价状态（待评价/已完成）
 * @param actionButton 操作按钮的WebElement引用
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/1 18:21
 */
public record EvaluationSubject(
    String courseName,
    String teacherName,
    String status,
    WebElement actionButton
) {
    
    /**
     * 判断是否为待评价状态
     */
    public boolean isPending() {
        return "待评价".equals(status) || "未评价".equals(status);
    }
    
    /**
     * 格式化输出科目信息
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (状态: %s)", 
            courseName, teacherName, status, 
            actionButton != null ? "可操作" : "无操作按钮");
    }
}