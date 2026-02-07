@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ========================================
echo    课程评价收割机 - 全自动打包脚本
echo ========================================
echo.

:: 切换到脚本所在目录（通常用于命令行在非脚本目录调用脚本）
cd /d "%~dp0"
echo [INFO] 工作目录: %CD%
echo.

:: ===== 第1步：分析依赖并捕获结果 =====
echo [STEP 1/4] 分析 JAR 依赖模块...

:: 先把 jdeps 输出存到临时文件，再读取最后一行（模块列表）
jdeps --ignore-missing-deps --print-module-deps ^
./jpackage_input/Course-Evaluation-AutoFillerV1.0-1.0.jar > temp_modules.txt

if errorlevel 1 (
    echo [ERROR] jdeps 分析失败！
    del temp_modules.txt 2>nul
    pause
    exit /b 1
)

:: 读取文件内容到变量（这就是检测到的依赖模块）
set /p MODULES=<temp_modules.txt
del temp_modules.txt

echo [OK] 检测到依赖模块: %MODULES%
echo.

:: ===== 第2步：用检测到的模块构建JRE =====
echo [STEP 2/4] 剪裁 JRE...
echo [INFO] 使用模块: %MODULES%

jlink ^
--add-modules %MODULES% ^
--output ./v1.0-jre ^
--strip-debug ^
--compress zip-9 ^
--no-header-files ^
--no-man-pages

if errorlevel 1 (
    echo [ERROR] jlink 构建 JRE 失败！
    pause
    exit /b 1
)
echo [OK] 自定义 JRE 构建完成: ./v0.3-jre
echo.

:: ===== 第3步：打包为可执行镜像 =====
echo [STEP 3/4] 打包为 APP-IMAGE...
jpackage ^
  --type app-image ^
  --name "课程评价收割机" ^
  --app-version 1.0 ^
  --icon ./logo.ico ^
  --input  ./jpackage_input^
  --main-jar Course-Evaluation-AutoFillerV1.0-1.0.jar ^
  --main-class com.github.existed_name.courseevaluationautofiller.AutoEvaluationMain ^
  --runtime-image ./v1.0-jre ^
  --win-console ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "-Dsun.stdout.encoding=UTF-8" ^
  --java-options "-Dsun.stderr.encoding=UTF-8" ^
  --dest ./jpackage_output

if errorlevel 1 (
    echo [ERROR] App-Image 打包失败！
    pause
    exit /b 1
)
echo [OK] App-Image 打包完成
echo.

:: ===== 第4步：打包为EXE安装程序 =====
echo [STEP 4/4] 打包为 EXE 安装程序...
jpackage ^
  --type exe ^
  --name "课程评价收割机" ^
  --app-version 1.0 ^
  --icon ./logo.ico ^
  --input  ./jpackage_input^
  --main-jar Course-Evaluation-AutoFillerV1.0-1.0.jar ^
  --main-class com.github.existed_name.courseevaluationautofiller.AutoEvaluationMain ^
  --runtime-image ./v1.0-jre ^
  --win-shortcut ^
  --win-menu ^
  --win-console ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --java-options "-Dsun.stdout.encoding=UTF-8" ^
  --java-options "-Dsun.stderr.encoding=UTF-8" ^
  --dest ./jpackage_output

if errorlevel 1 (
    echo [ERROR] EXE 安装程序打包失败！
    pause
    exit /b 1
)
echo [OK] EXE 安装程序打包完成
echo.

:: ===== 完成 =====
echo ========================================
echo    全自动构建完成！
echo    输出目录: ./jpackage_output
echo ========================================
pause