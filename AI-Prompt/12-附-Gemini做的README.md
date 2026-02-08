
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

