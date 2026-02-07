来到<u>[规划整体项目](/AI-Prompt/01-整体规划.md)</u>的那个对话

---

因为我先做的Release说明，所以直接把这个说明发给他，再把要求发给他
```
这是我的1.0.0Release说明（之前Pre-Release了v0.1、0.2、0.3，都是各个功能慢慢堆上来的——在Release说明里面慢慢增加，到0.3改进后变成完全体，于是我就标记成1.0正式Release了🧐）

# 课程评价收割机v1.0.0

---

<details>
<summary>
<h2>🚀 新增功能</h2>
</summary>

* 该版本为正式发布版本

</details>


<details>
<summary>
<h2>🐞 修复问题</h2>
</summary>

* 完成所有课程评价后，评价列表会消失，脚本显示找不到列表

</details>


<details>
<summary>
<h2>📃 文件说明</h2>
</summary>

* 见下面的`Assets`(只能用ASCII字符命名，不能用中文命名，可以下载后自己改名字：`课程评价收割机`)
* `Course-Evaluation-AutoFillerV1.0.0.zip`: **最方便**，解压即可食用（运行里面的`.exe`文件）
* `Course-Evaluation-AutoFillerV1.0.0.exe`: 安装器，运行后会在`C:\Program Files (x86)`安装相应文件，并在桌面生成快捷方式。可在设置(Win + I) → 应用 → 安装的应用中找到并删除(或者直接删除文件夹、桌面快捷方式)
* `Course-Evaluation-AutoFillerV1.0.0.jar`: 包含所有依赖的Jar包(`FatJar`)，可在**Java环境**上运行
* `Source code`源码

</details>


<details>
<summary>
<h2>🛠 使用教程</h2>
</summary>

1. 电脑端(推荐Win11) → 打开并登录aTrust
2. 通过aTrust进入教务系统，这样可以让Edge保留教务系统登录状态
3. 打开下载好的`课程评价收割机.exe`，脚本会自动执行：打开命令行、打开Edge、进入教务系统、进入评价界面、填写各课程的评价
4. 下载Jar包的同学：
    * 打开cmd/Terminal( `win + R`输入`cmd` → 回车；或者Win → 搜索cmd/Terminal → 打开“命令提示符”)
    * 先输入`D:`切换到D盘(假设下载到D盘)
    * 输入`cd jar包安装目录`，切换目录
    * 输入`java -jar jar包名称`即可运行

</details>


<details>
<summary>
<h2>⚠️ 注意事项</h2>
</summary>

1. 🚨 运行风险提示
* 脚本会关闭之前的 Edge 进程，<u>***请确保 Edge 上的任务保存后再运行脚本***</u>

2. 🔑 环境与账号准备
* 提前登录：**请提前登录电脑 aTrust，从 aTrust 登录教务系统**
* **数据目录**：请确保系统存在目录: `C:\Users\你的用户名\AppData\Local\Microsoft\Edge\User Data`，这里存了 Edge 浏览数据等等
* **Edge设置**：如果 Edge 打开了“`关闭浏览器后清理浏览数据`”，请**禁用**，避免清除登录状态

3. 🌐 网络与代理配置
* 只打开aTrust，或者先开aTrust、后开梯子(并且确保梯子是 **“规则模式”** 而不是全局代理)；如果先打开梯子、后打开aTrust，教务系统会要求再次登录
* 下载文件时若遇网络问题，请检查梯子状况、尝试重启梯子/切换节点
* 脚本执行时请勿退出/重启梯子、aTrust，会导致网络代理中断

4. 🚑 故障排查与“清扫”
退出脚本 (关闭命令行) 后可能有**进程残留**，导致移动文件夹时提醒“`正在使用`”：

1. 首先确保脚本窗口、Edge 已关闭
2. 若还是“正在使用”，可以 `Ctrl + Shift + Esc` 打开任务管理器 → 搜索脚本名称 `课程评价收割机` 以及 `Edge` 并停止相应进程 (比如 `Edge`、`Microsoft Edge WebDriver`)
3. 如果还是提醒，那就 `Win + R` → 输入 `resmon` → 进入资源监视器 → 搜索句柄填写该文件夹的名字，关闭可疑的那个进程

5. 🛡️ 其他
* **安装位置**：如果想通过 Windows 的设置来一键删除应用，则不建议移动 `installer.exe` 安装的应用文件夹，否则会找不到位置 (不过也可以移回去😅)
* **驱动说明**：此脚本的 Edge、EdgeDriver 版本号为 `144.0.3719.104` (`144.0.x`)，可能与用户版本不匹配，作者会尽快优化
    * **版本核对**：浏览器地址栏搜索 `edge://version/` 可以查看 `Profile path` (数据储存目录) 和 Edge 版本号

</details>

---

欢迎<u>[提出问题](https://github.com/existed-name/Course-Evaluation-AutoFiller/issues)</u>和<u>[讨论](https://github.com/existed-name/Course-Evaluation-AutoFiller/discussions)</u>🤗
```

```java
你可以帮我生成一份简洁清晰好看的仓库README嘛——我根据实际情况修改增删后即可使用的那种📕

记得带emoji、标记哪里需要我改👀
```

---

这里是Gemini做的README

```
# 🎓 课程评价收割机 (Course-Evaluation-AutoFiller) 🚀

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![Selenium](https://img.shields.io/badge/Framework-Selenium-green.svg)
![Version](https://img.shields.io/badge/Release-v1.0.0-blue.svg)

> **告别繁琐，一键收割！** 这是一个基于 Java + Selenium 开发的极简自动化脚本，旨在帮助同学们从教务系统那冗长的教学质量评价问卷中解脱出来。

---

## 🌟 核心功能
* **自动接管**：支持接管已登录的 Edge 浏览器会话，跳过复杂的登录流程。
* **智能填充**：全自动勾选“非常满意”，并随机/统一填充课程评语。
* **循环执行**：自动遍历课程列表，直到收割完最后一门课。
* **容错机制**：列表清空自动识别，避免脚本报错。

## 📦 下载与安装

请前往 [Releases 页面]([这里替换成你的GitHub仓库Releases链接]) 下载最新版本：

| 文件名 | 适用场景 |
| :--- | :--- |
| `Course-Evaluation-AutoFillerV1.0.0.zip` | **推荐使用**。解压即用，内含运行环境。 |
| `Course-Evaluation-AutoFillerV1.0.0.exe` | 安装器。适合喜欢桌面快捷方式的同学。 |
| `Course-Evaluation-AutoFillerV1.0.0.jar` | 进阶使用。适合已安装 Java 环境的开发者。 |

## 🛠️ 快速上手 (必看!)

1. **环境准备**：
   * 电脑端打开并登录 **aTrust**（[在此处补充你的学校特有网络要求，如：确保进入教务系统主页]）。
   * 确保 Edge 浏览器未开启“关闭时清理浏览数据”。
2. **运行脚本**：
   * 双击运行 `课程评价收割机.exe`。
   * 脚本将自动打开 Edge 浏览器并执行评价流程。
3. **静候佳音**：
   * 脚本运行期间，请勿手动关闭命令行窗口或操作正在运行的 Edge。

## ⚠️ 注意事项
* **进程冲突**：脚本运行前会尝试关闭已有的 Edge 进程，请**务必提前保存网页上的重要工作**。
* **驱动版本**：当前内置驱动版本为 `[这里改写成你的具体版本，如 144.0.3719.104]`。若运行报错，请检查浏览器版本。
* **代理问题**：请确保 aTrust 处于正常连接状态。若使用梯子，请务必开启“规则模式”。

## 🚑 故障排查
如果遇到“文件正在使用”或无法删除文件夹：
1. `Ctrl + Shift + Esc` 打开任务管理器。
2. 结束 `Microsoft Edge WebDriver` 进程。
3. 若仍有问题，请参考内置教程中的 `resmon` (资源监视器) 处理方法。

## 🛠️ 开发与贡献
如果你想参与改进这个项目：
1. `git clone [[这里替换成你的仓库HTTPS/SSH链接]]`
2. 使用 IntelliJ IDEA 打开项目。
3. 确保安装了 Maven，并自动导入 Selenium 依赖。

---

**免责声明**：本工具仅用于学术交流和自动化技术探索，请合理使用，切勿干扰教务系统正常运行。

**如果你觉得好用，请给个 ⭐️ Star 吧！** [Issues 提交]([这里替换成你的Issues链接]) | [讨论区]([这里替换成你的Discussions链接])
```

---

然后让Claude给一个技术点清单，方便复制到Readme
```java
嘿Claude👀

我在写Readme文档📕，帮我总结项目技术点（例如：如何处理登录状态、如何接管现有浏览器 Session、如何进行异常处理等等具体的技术实现问题，方便我自己和其他人了解），并以 Markdown 的格式输出

要求语言专业简洁；写进 Artifacts文档，对话里面仅保留简短的文件说明📃
```
