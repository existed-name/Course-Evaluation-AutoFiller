package com.github.existed_name.courseevaluationautofiller.config;

/**
 * 集中管理所有XPath表达式
 * 
 * 命名规范：
 * - XPATH_ 前缀表示XPath表达式
 * - _BTN 后缀表示按钮
 * - _LINK 后缀表示链接
 * - _TABLE 后缀表示表格
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/1 18:22
 */
public final class XPathConstants {
    
    private XPathConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ==================== 导航相关 ====================
    
    /**
     * 教学质量评价菜单入口
     * 适配多种可能的文本：教学质量评价、教学评价、质量评价
     */
    public static final String XPATH_EVALUATION_MENU =
            "//*[@id=\"navItem_app_evaluateOnline\"]/a/div[2]/div"; // 教学质量评价
//    or " +
//        "//a[contains(text(), '教学质量评价') or " +
//        "contains(text(), '教学评价') or " +
//        "contains(text(), '质量评价')]";
    
    /**
     * 备用方案：通过href属性定位
     */
    public static final String XPATH_EVALUATION_MENU_BY_HREF = 
        "//a[contains(@href, 'evaluation.cfm') or " +
        "contains(@href, 'quality.cfm')]";
    
    // ==================== 列表提取相关 ====================
    
    /**
     * 评价列表主表格
     * 尝试多种可能的class名称
     */
    public static final String XPATH_EVALUATION_TABLE = 
        "//table[contains(@class, 'evaluation') or " +
        "//*[@id=\"Questionnaire\"] or " +
        "contains(@class, 'quality') or " +
        "contains(@id, 'eval')]";
    
    /**
     * 备用方案：通过表头定位表格
     */
    public static final String XPATH_EVALUATION_TABLE_BY_HEADER = 
        "//table[.//th[contains(text(), '课程')] and " + // 把“课程名称”改为“课程”
        ".//th[contains(text(), '任课单位')] and " + // 额外补充
        ".//th[contains(text(), '学分')] and " + // 额外补充
        ".//th[contains(text(), '周次')] and " + // 额外补充
        ".//th[contains(text(), '教师')]]"; // 把“教师姓名”改为“教师”
    
    /**
     * 表格中的所有数据行（排除表头）
     */
    public static final String XPATH_TABLE_ROWS = 
        ".//tbody/tr | .//tr[position() > 1]";
    
    /**
     * 待评价状态的行
     */
    public static final String XPATH_PENDING_ROWS = 
        ".//tr[contains(., '待评价') or contains(., '未评价')]";
    
    /**
     * 行内的课程名称单元格（通常是第1或第2列）
     */
    public static final String XPATH_COURSE_NAME_CELL = 
        "./td[1] | ./td[2]";
    
    /**
     * 行内的教师姓名单元格
     */
    public static final String XPATH_TEACHER_NAME_CELL = 
        "./td[contains(@class, 'teacher') or position()=2 or position()=3]";
    
    /**
     * 行内的状态单元格
     */
    public static final String XPATH_STATUS_CELL = 
        "./td[contains(text(), '待评价') or " +
        "contains(text(), '已完成') or " +
        "contains(text(), '未评价')]";
    
    /**
     * 行内的"进入评价"按钮/链接
     */
    public static final String XPATH_ACTION_BUTTON = 
        ".//a[contains(text(), '进入评价') or " +
        "contains(text(), '评价') or " +
        "contains(@href, 'evaluate')] | " +
        ".//button[contains(text(), '进入评价') or " +
        "contains(text(), '评价')]";
}