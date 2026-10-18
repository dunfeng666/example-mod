@echo off
REM ============================================================
REM 一键离线构建（永久方案·无代理可用）
REM 前提：先在能联网的网络（手机热点/公司网络）跑一次：
REM     gradlew.bat build
REM 成功后本机 ~/.gradle/caches 已存满 NeoForge 依赖，之后
REM 永远用本脚本构建，全程不联网、不卡 maven.neoforged.net。
REM 换 NeoForge 版本 / 新增外部依赖时，需再借一次网跑普通 build。
REM ============================================================
cd /d "%~dp0"
gradlew.bat build --offline %*
if errorlevel 1 (
  echo.
  echo [提示] 离线构建失败。常见原因：
  echo   1. 本机缓存未建全 - 先用联网环境跑一次 gradlew.bat build
  echo   2. 缺少新依赖 - 借网跑普通 build 后再用本脚本
)
pause
