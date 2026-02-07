package com.github.existed_name.courseevaluationautofiller.config;

import java.time.Duration;

/**
 * 系统配置常量
 * 集中管理所有配置项和XPath表达式
 * v1.0 - 增加完成提示检测配置
 *
 * @author <a href="https://github.com/existed-name"> existed-name </a>
 * @author <a href="https://claude.ai/new"> Claude4.5 </a>
 * @author <a href="https://gemini.google.com/app"> Gemini3 </a>
 * @author <a href="https://www.kimi.com/"> KimiK2.5 </a>
 * @CreateTime 2026/2/2
 */
public final class SystemConfig {

    private SystemConfig() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ==================== 系统路径配置 ====================

    /** Edge 配置文件名 */
    public static final String EDGE_PROFILE = "Default";

    // ==================== 业务 URL 配置 ====================

    /**
     * 教务系统目标地址
     * 👈【修改】改成你的教务系统首页URL
     */
    public static final String TARGET_URL =
            "https://matrix.dean.swust.edu.cn/acadmicManager/index.cfm?event=studentPortal:DEFAULT_EVENT";

    // ==================== 等待时间配置 ====================

    /** 显式等待超时（秒） */
    public static final Duration EXPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(10);

    /** 隐式等待全局超时（秒） */
    public static final Duration IMPLICIT_WAIT_TIMEOUT = Duration.ofSeconds(5);

    /** 页面初始加载等待（毫秒）- 仅用于浏览器启动后 */
    public static final long INITIAL_LOAD_DELAY = 3000;

    // ==================== 登录状态检测配置 ====================

    /** 登录成功标识元素列表（任一匹配即认为成功） */
    public static final String[] LOGIN_SUCCESS_INDICATORS = {
            "//*[@id='navAccountLink']",
            "//a[contains(normalize-space(.), '评价')]",
            "//a[contains(normalize-space(.), '选课')]",
            "//a[contains(normalize-space(.), '成绩')]",
            "//a[contains(normalize-space(.), '注销')]"
    };

    /** 登录页特征元素 */
    public static final String[] LOGIN_PAGE_INDICATORS = {
            "//input[@id='username']",
            "//input[@type='password']",
            "//button[contains(text(),'登录')]"
    };

    // ==================== 导航相关 XPath ====================

    /**
     * 教学质量评价菜单入口（多备用方案）
     * 优先级从高到低
     */
    public static final String[] XPATH_EVALUATION_MENU = {
            "//*[@id='navItem_app_evaluateOnline']/a/div[2]/div",  // 精确ID路径
            "//*[@id='navItem_app_evaluateOnline']//a",            // ID通配
            "//a[contains(normalize-space(.), '教学质量评价')]",     // 文本匹配（处理空格）
            "//a[contains(text(), '教学质量评价')]",                // 文本匹配（标准）
            "//a[contains(@href, 'evaluateOnline')]"              // href特征
    };

    /**
     * 评价页面特征（用于验证导航成功）
     */
    public static final String[] EVALUATION_PAGE_INDICATORS = {
            "//*[@id='headArea']//h2[contains(text(), '教学质量评价')]",
            "//*[@id='Questionnaire']",
            "//h2[contains(normalize-space(.), '教学质量评价')]"
    };

    /**
     * 评价页面URL特征
     */
    public static final String[] EVALUATION_URL_KEYWORDS = {
            "evaluate",
            "evaluation",
            "quality"
    };

    // ==================== 列表提取相关 XPath ====================

    /**
     * 评价列表表格（多层定位）
     */
    public static final String[] XPATH_EVALUATION_TABLE = {
            "//*[@id='Questionnaire']/table",                      // 最精确
            "//*[@id='Questionnaire']//table",                     // ID通配
            "//*[@id='contentArea']//table",                       // 外层容器
            "//table[.//th[contains(text(), '课程')]]"             // 表头特征
    };

    /**
     * 🔑 新增：所有评价完成的提示元素
     * 当所有课程评价完成后，会显示"太棒了"提示框
     */
    public static final String[] XPATH_ALL_COMPLETED_INDICATORS = {
            "//*[@id='Questionnaire']/div[@class='systemNotice']",                    // 精确定位
            "//*[@id='Questionnaire']//div[contains(@class, 'systemNotice')]",        // class模糊匹配
            "//div[@class='systemNotice']//h3[contains(text(), '太棒了')]",           // 通过标题文本
            "//div[contains(@class, 'systemNotice')]//p[contains(text(), '完成了目前所有的问卷')]", // 通过内容文本
            "//h3[contains(text(), '太棒了')]",                                       // 仅标题
            "//*[@id='Questionnaire']//h3[contains(text(), '太棒了')]"                // ID+标题组合
    };

    /**
     * 表格行（排除表头）
     */
    public static final String XPATH_TABLE_ROWS = ".//tbody/tr | .//tr[position() > 1]";

    /**
     * 行内单元格XPath（根据你的6列结构）
     */
    public static final String XPATH_CELL_INDEX = "./td[1]";         // 序号
    public static final String XPATH_CELL_DEPARTMENT = "./td[2]";    // 任课单位
    public static final String XPATH_CELL_COURSE_TEACHER = "./td[3]"; // 课程 - 教师
    public static final String XPATH_CELL_CREDITS = "./td[4]";       // 学分
    public static final String XPATH_CELL_WEEKS = "./td[5]";         // 周次
    public static final String XPATH_CELL_BUTTON = "./td[6]";        // 按钮列

    /**
     * "网上评价"按钮（超链接）
     */
    public static final String[] XPATH_EVAL_BUTTON = {
            ".//a[@class='stat info' and @title='网上评价']",      // 精确匹配
            ".//a[contains(@title, '网上评价')]",                  // title匹配
            ".//a[contains(@href, 'evaluateResponse')]",          // href特征
            ".//a[contains(text(), '网上评价')]"                   // 文本匹配
    };

    // ==================== 问卷填写相关 XPath ====================

    /**
     * 问卷页面特征（验证跳转成功）
     */
    public static final String[] QUESTIONNAIRE_PAGE_INDICATORS = {
            "//*[@id='labDetail' and contains(text(), '教学质量评价问卷')]",
            "//*[@id='sheetTable']",
            "//span[@class='active' and @id='labDetail']"
    };

    /**
     * 问卷页面URL特征
     */
    public static final String[] QUESTIONNAIRE_URL_KEYWORDS = {
            "evaluateResponse",
            "evaluate"
    };

    /**
     * 问卷表格
     */
    public static final String XPATH_QUESTIONNAIRE_TABLE = "//*[@id='sheetTable']";

    /**
     * 所有选项单元格（包含 class="quota ltr"）
     */
    public static final String XPATH_ALL_OPTION_CELLS =
            "//td[@class='quota ltr']";

    /**
     * 单元格内的"非常满意"选项（data-opt="1"）
     */
    public static final String XPATH_OPTION_VERY_SATISFIED =
            ".//a[@data-opt='1']";

    /**
     * 评语文本框
     */
    public static final String XPATH_COMMENT_TEXTAREA = "//*[@id='CourseComment']";

    /**
     * 提交按钮
     */
    public static final String XPATH_SUBMIT_BUTTON = "//*[@id='postTrigger']";

    // ==================== 行为控制开关 ====================

    /** 是否自动关闭浏览器（调试时建议设为false） */
    public static boolean AUTO_CLOSE_BROWSER = false;

    /** 强制清理 Edge 进程 */
    public static final boolean FORCE_KILL_EDGE_PROCESS = true;

    /** 评语内容 */
    public static final String DEFAULT_COMMENT = "无";

    /** 每次操作后的短暂等待（毫秒）- 让页面有时间响应 */
    public static final long SHORT_DELAY = 500;
}