# GitHub Actions 云构建（绕开本机网络封锁）

## 原理

本机无法直连 `maven.neoforged.net`，但 GitHub 的云服务器可以。把模组工程推到 GitHub 后，构建在云端完成，**jar 包直接从 GitHub 下载**，本机全程不用代理、不用缓存。

## 推送工程（本工程已 git init 并完成首次提交）

```bash
git remote add origin https://github.com/<你的用户名>/<仓库名>.git
git push -u origin main
```

- 首次 push 会要求登录 GitHub（弹出 Git Credential Manager 窗口，或按提示输入 Personal Access Token）。
- 生成令牌：GitHub 网页 → Settings → Developer settings → Personal access tokens → Generate new token，勾选 `repo`，复制粘贴到凭据提示框。

## 之后每次使用

- **改了代码出新 jar**：`git add . && git commit -m "改动说明" && git push` → 云端自动构建 → 去 Actions 页下载新 jar。
- **手动触发**：仓库页 → Actions → `Build Mod` → 右侧 **Run workflow**。
- **下载 jar**：Actions → 最新一次运行 → 底部 **Artifacts** → `mod-jar` → 解压得到 `examplemod-1.0.0.jar`，放进游戏 `mods` 目录。

## 说明

- `.github/workflows/build.yml`：JDK 21 + `./gradlew build` + 上传 `build/libs/*.jar`；`GameTest` job 跑游戏内测试，当前缺测试结构模板时可能失败但不阻塞主构建。
- 首次云端构建 10-20 分钟（下载 NeoForge + 反编译），之后有依赖缓存会快很多。
- 本机仍可离线构建：依赖缓存齐全后用 `./gradlew build --offline`（见 build-offline.bat）。
- 想自动发布 Modrinth / CurseForge：按技能 `neoforge-mod-dev` 的 `publishing.md` 配置 Minotaur / CurseGradle，并在 workflow 追加发布步骤（token 放仓库 Secrets）。
