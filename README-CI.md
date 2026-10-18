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

## 当前 CI 质量门（2026-10-18 已上线）

`.github/workflows/build.yml` 含两个 job，**都必须是绿色**才算通过：

1. **Compile & Package**：`./gradlew spotlessCheck build`——先过 Spotless 格式检查（palantir-java-format，`./gradlew spotlessApply` 可自动修），再编译打包；
2. **GameTest**：`./gradlew runGameTestServer`——游戏内自动化测试硬门，有失败即红。

- 首次云端构建 10-20 分钟（下载 NeoForge + 反编译），之后有依赖缓存会快很多。
- 本机仍可离线构建：依赖缓存齐全后用 `./gradlew build --offline`（见 build-offline.bat）。
- 想自动发布 Modrinth / CurseForge：按技能 `neoforge-mod-dev` 的 `publishing.md` 配置 Minotaur / CurseGradle，并在 workflow 追加发布步骤（token 放仓库 Secrets）。

## 多版本构建（Multi-Version Matrix）

当前工程是 **1.21.1 单版本**。要让一份代码（或一个仓库）同时验证多个 Minecraft 版本（如 1.20.1 / 1.21.1 / 1.21.4），用「**多分支 + matrix**」方案：

### 方案

- **每个 MC 版本一个分支**：`main` 保持 1.21.1；另建 `mc/1.20.1`、`mc/1.21.4` 等分支，分支里的 `gradle.properties` / `build.gradle` / 源码按该版本调整；
- **一个 workflow 同时覆盖所有分支**：`push` 触发所有分支；加 `workflow_dispatch` 的版本下拉，支持手动指定；
- **matrix**：按版本并行跑同一套 job，每个版本一个 runner。

### 可复制的 matrix workflow（替换 build.yml 的 job 部分）

```yaml
name: Build Mod (Multi-Version)
on:
  push:
    branches: [main, 'mc/*']          # 主分支 + 版本分支都触发
  workflow_dispatch:
    inputs:
      version_branch:
        description: '选择要构建的版本分支'
        required: false
        type: choice
        options: ['main', 'mc/1.20.1', 'mc/1.21.4']

jobs:
  build:
    strategy:
      fail-fast: false
      matrix:
        version: [1.21.1]             # 加版本 = 在这里加一行，并准备对应分支
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          ref: ${{ github.event.inputs.version_branch || github.ref }}
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21
          cache: gradle
      - name: Make gradlew executable
        run: chmod +x gradlew
      - name: Spotless + Build
        run: ./gradlew spotlessCheck build --no-daemon
      - name: GameTest
        run: ./gradlew runGameTestServer --no-daemon || true   # 分支未配测试模板时先放行，配好后去掉 || true
      - uses: actions/upload-artifact@v4
        with:
          name: mod-jar-${{ matrix.version }}
          path: build/libs/*.jar
```

### 加一个新版本（3 步）

1. `git checkout -b mc/1.20.1`（从 main 复制）；
2. 改该分支 `gradle.properties`：`minecraft_version`、`neo_version`（1.20.1 对应 `20.4.x`，且 1.20.x 的 MDK 用旧版 ModDevGradle/NeoGradle——以该版本官方 MDK 为准）；按需调整 `build.gradle` 插件版本；
3. 修该版本 API 差异的源码（参考技能 `neoforge-mod-dev` 的 `neoforge-porting`），push 即自动进入矩阵。

### 注意

- **ModDevGradle 版本随 MC 版本走**，1.20.x 与 1.21.x 不通用，各分支单独锁定；
- matrix 里每个版本必须是**真实存在的分支**，否则该行构建必失败；
- 版本差异的 API 修正记录见技能 `neoforge-porting`（1.20 → 1.21.1 迁移要点）。

> 当前仓库保持单版本（1.21.1）稳定构建；需要多版本时按上面方案加分支即可，workflow 已预留 matrix 结构。
