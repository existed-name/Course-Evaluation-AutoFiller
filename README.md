# 🎓 课程评价收割机 (Course-Evaluation-AutoFiller) 🚀

<div align="center">
  <img src="https://img.shields.io/badge/Language-Java-orange.svg" />
  <img src="https://img.shields.io/badge/Framework-Selenium-green.svg" />
  <img src="https://img.shields.io/badge/Tool-Maven-blue.svg" />
  <img src="https://img.shields.io/badge/Release-v1.0.0-red.svg" />
  <img src="https://img.shields.io/badge/License-GPL3.0-yellow.svg" />
  <br>
  <img src="/assets/images/logo.ico" />
</div>

> **告别繁琐，一键收割！**  
> 这是一个使用 AI 开发、基于 Java + Selenium 的极简自动化脚本，帮助同学们从教务系统冗长的教学质量评价问卷中解脱出来

---

<details>
<summary>
<h2>🌟 核心功能</h2>
</summary>

* **自动接管**：支持接管已登录的 Edge 浏览器会话，跳过复杂的登录流程。
* **智能填充**：全自动勾选“非常满意”，并统一填充课程评语。
* **循环执行**：自动遍历课程列表，直到收割完最后一门课。
* **容错机制**：列表清空自动识别，避免脚本报错。

</details>


<details>
<summary>
<h2>📺 脚本运行演示</h2>
</summary>

清朝画质😅  
![demo](/assets/images/Course-Evaluation-AutoFiller-v0.3-demo.gif "demo")  
![ScreenShot](/assets/images/Console-ScreenShot.png "截图") 

</details>


<details>
<summary>
<h2>📦 下载安装</h2>
</summary>

请前往 [Releases 页面](https://github.com/existed-name/Course-Evaluation-AutoFiller/releases)(**发布界面有详细使用说明**)  
下载最新版本：

| 文件名 | 适用场景 |
| :--- | :--- |
| `Course-Evaluation-AutoFillerV1.0.0.zip` | **推荐使用**。解压即用，内含运行环境。 |
| `Course-Evaluation-AutoFillerV1.0.0.exe` | 安装器。适合喜欢桌面快捷方式的同学。 |
| `Course-Evaluation-AutoFillerV1.0.0.jar` | 进阶使用。适合已安装 Java 环境的开发者。 |

</details>


<details>
<summary>
<h2>🛠️ 快速上手</h2>
</summary>

1. **环境准备**：
   * 电脑端打开并登录 **aTrust**、确保登录教务系统主页
   * 确保 Edge 浏览器未开启“关闭时清理浏览数据”。
2. **运行脚本**：
   * 双击运行 `课程评价收割机.exe`。
   * 脚本将自动打开 Edge 浏览器并执行评价流程。
3. **静候佳音**：
   * 脚本运行期间，请勿手动关闭命令行窗口或操作正在运行的 Edge。
4. 👉 完整安装教程与常见问题，请移步 [Releases 页面](https://github.com/existed-name/Course-Evaluation-AutoFiller/releases)

</details>

---

<details>
<summary>
<h2>💻 技术实现</h2>
</summary>

* 👉 [详细技术点](/docs/Tech-Details.md)( 包括描述 → 解决方案 → 代码示例 → 关键要点 )
* 登录状态保持（复用用户数据目录）
* XPath动态定位（文本匹配 + 备用方案）
* Stale Element处理（动态重定位）
* 空列表检测（完成状态识别）
* 表单自动填写（批量选择）
* 页面跳转验证（URL + DOM双重检查）
* 进程管理（强制清理占用）

</details>


<details>
<summary>
<h2>📃 仓库文件说明</h2>
</summary>

1. `AI-Prompt`: 开发中使用的AI提示词
2. `dev-log`: 开发日志, 记录我的思考、排查过程
3. `docs`:
   * `Tech-Details.md`: 技术点
   * `Auto-Packaging.bat`: 自动打包批处理文件( 可用记事本打开查看代码 )
5. `src/main`: 源码
   * 无测试代码(`Test`)
   * `resources`包内为`msedgedriver.exe`
   * 下载源码的同学可在`config`包的`SystemConfig`类的最下面修改`DEFAULT_COMMENT`(填写的评语)
6. `pom.xml`、`dependency-reduced-pom.xml`: 项目依赖

</details>


<details>
<summary>
<h2>💡 附</h2>
</summary>

1. **免责声明**：本工具仅用于学术交流和自动化技术探索，请合理使用，切勿干扰教务系统正常运行。

2. **如果你觉得好用，请给个 ⭐️ Star 吧！**   

3. **特别鸣谢**: 所有参与本项目规划、开发、维护的AI
   * <a href="https://claude.ai/new"> ClaudeSonnet4.5 </a>: 开发模块2以及后续整个脚本、技术点文档编写
   * <a href="https://gemini.google.com/app"> Gemini3 </a>: 项目规划、编写模块1&2的提示词、技术讲解、Readme模板编写
   * <a href="https://www.kimi.com/"> KimiK2.5 </a>: 模块1的开发、bug 修复、总结
   * <a href="https://chat.deepseek.com/"> DeepSeekV3.2 </a>: 模块1的 bug 修复、建议
   
4. [Issues 提交](https://github.com/existed-name/Course-Evaluation-AutoFiller/issues) | [讨论区](https://github.com/existed-name/Course-Evaluation-AutoFiller/discussions)

5. [本项目使用 GPL3.0 开源协议](/LICENSE)

</details>
