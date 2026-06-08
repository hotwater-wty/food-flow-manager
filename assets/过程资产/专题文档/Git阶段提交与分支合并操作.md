# Git 阶段提交与分支合并操作

## 1. 解决什么问题

本文记录当前项目中常用的 Git 操作流程，重点解决以下问题：

- 正常开发时如何审查当前改动，避免把无关内容混入同一次提交。
- 如何完成暂存、提交、推送、拉取等基础操作。
- 如何查看提交日志、创建分支、切换分支和跟踪远程分支。
- 如何把开发分支或 `master` 合并到 `main`，以及遇到冲突时如何处理。
- 如何查看和调整本地 Git 账户、编码、中文路径显示等基础配置。

当前项目曾经出现过大量代码长期停留在工作区、误操作后不易回滚的问题，因此后续开发应优先建立“阶段性提交”的习惯。

## 2. 当前项目如何使用

### 2.1 Git 四个位置

日常使用时先理解 Git 中的四个位置：

```text
工作区 -> 暂存区 -> 本地仓库 -> 远程仓库
```

含义如下：

- 工作区：当前正在编辑但还没有提交的文件。
- 暂存区：通过 `git add` 放入“准备提交清单”的内容。
- 本地仓库：通过 `git commit` 保存下来的本机检查点。
- 远程仓库：通过 `git push` 推送到 GitHub 的备份和协作位置。

`git add` 不是保存检查点，只是把改动放入暂存区。真正能用于回滚和追踪历史的是 `git commit`。

### 2.2 正常开发流程

每完成一个独立功能点或一个清晰重构点，按以下流程处理：

```powershell
git status --short --branch
git diff --stat
git diff --name-status
git diff
```

确认改动范围后暂存相关文件：

```powershell
git add path/to/file1.java path/to/file2.java
```

如果本轮改动全部都属于同一个提交意图，可以使用：

```powershell
git add -A
```

暂存后必须检查“即将提交”的内容：

```powershell
git diff --cached --stat
git diff --cached --name-status
git diff --cached
```

确认无误后提交：

```powershell
git commit -m "清晰描述本次改动"
```

提交后检查本地状态：

```powershell
git status --short --branch
git log --oneline --decorate -n 5
```

最后推送：

```powershell
git push
```

如果当前分支还没有关联远程分支：

```powershell
git push -u food-flow-manager 当前分支名
```

### 2.3 提交粒度

一次提交应只表达一个明确意图，例如：

```text
构建会话模块的框架，完成未预约到店占座功能
完成菜品模块基础 CRUD 功能
docs: 整理 V1 收尾文档
merge: merge V1 master into main
```

不建议把以下内容混在同一个提交里：

- 业务功能代码和大规模格式化。
- 文档整理和核心业务逻辑。
- 配置修复和无关模块重构。
- 多个互不相关的功能点。

如果已经产生了一堆混杂改动，应先用 `git diff --name-status` 按业务意图分组，再逐组 `git add` 和 `git commit`。

## 3. 核心命令示例

### 3.1 查看状态和差异

查看当前分支和文件状态：

```powershell
git status --short --branch
```

查看文件改动规模：

```powershell
git diff --stat
```

查看哪些文件被新增、修改、删除、重命名：

```powershell
git diff --name-status
```

查看未暂存的具体差异：

```powershell
git diff
```

查看已暂存、即将提交的差异：

```powershell
git diff --cached
```

### 3.2 暂存、取消暂存和恢复

暂存指定文件：

```powershell
git add src/main/java/com/foodflow/module/user/service/UserService.java
```

暂存所有新增、修改、删除：

```powershell
git add -A
```

取消暂存，但保留工作区改动：

```powershell
git restore --staged path/to/file
```

撤销工作区中尚未提交的误改：

```powershell
git restore path/to/file
```

从某个历史提交取回文件：

```powershell
git restore --source=<commit-id> -- path/to/file
```

放弃正在进行的合并：

```powershell
git merge --abort
```

### 3.3 提交和推送

创建本地提交：

```powershell
git commit -m "提交说明"
```

推送当前分支：

```powershell
git push
```

首次推送新分支，并建立 upstream：

```powershell
git push -u food-flow-manager 分支名
```

### 3.4 克隆仓库

首次在一台新电脑或新目录中获取项目代码时，使用 `git clone`。

克隆仓库到当前目录下的默认文件夹：

```powershell
git clone https://github.com/hotwater-wty/food-flow-manager.git
```

克隆仓库并指定本地目录名：

```powershell
git clone https://github.com/hotwater-wty/food-flow-manager.git food-flow-manager
```

进入项目目录：

```powershell
cd food-flow-manager
```

克隆后检查当前分支和远程地址：

```powershell
git status --short --branch
git remote -v
git branch -vv --all
```

如果只想克隆指定分支，例如 `main`：

```powershell
git clone -b main https://github.com/hotwater-wty/food-flow-manager.git
```

注意事项：

- `git clone` 用于首次获取仓库；已有本地仓库后，日常同步使用 `git fetch` 或 `git pull`。
- 不要在已有 `.git` 的项目目录里再次执行 `git clone`，否则会嵌套出一个新的仓库目录。
- 克隆私有仓库或推送代码时，需要本机 GitHub 凭据、token、SSH key 或 GitHub CLI 登录状态支持。

### 3.5 拉取和同步

只更新远程分支信息，不修改当前工作区：

```powershell
git fetch food-flow-manager
```

拉取当前分支对应的远程更新，要求必须能快进：

```powershell
git pull --ff-only
```

拉取指定远程分支：

```powershell
git pull --ff-only food-flow-manager main
```

开发前推荐先执行：

```powershell
git status --short --branch
git fetch food-flow-manager
git pull --ff-only
```

如果当前工作区有未提交改动，先提交或 stash，不要直接 pull。

### 3.6 日志查看

查看最近提交：

```powershell
git log --oneline --decorate -n 10
```

查看分支图：

```powershell
git log --graph --oneline --decorate --all --max-count=30
```

查看某个提交修改了哪些文件：

```powershell
git show --stat <commit-id>
```

查看某个提交的完整差异：

```powershell
git show <commit-id>
```

查看提交作者、提交者和日期：

```powershell
git log --pretty=format:"%h %d%n  author=%an <%ae>%n  committer=%cn <%ce>%n  authorDate=%ad%n  commitDate=%cd%n  subject=%s%n" --date=iso-strict -n 10
```

### 3.7 分支创建和切换

查看本地和远程分支：

```powershell
git branch -vv --all
```

创建并切换到新分支：

```powershell
git switch -c feature/session-module
```

切换已有分支：

```powershell
git switch main
```

从远程分支创建本地跟踪分支：

```powershell
git switch -c main --track food-flow-manager/main
```

给当前状态创建备份分支：

```powershell
git branch backup/main-before-v1-merge
```

### 3.8 分支合并

合并前先确认工作区干净：

```powershell
git status --short --branch
```

切换到接收合并结果的目标分支：

```powershell
git switch main
```

合并来源分支：

```powershell
git merge master -m "merge: merge V1 master into main"
```

如果两条分支没有共同祖先，需要允许无关历史合并：

```powershell
git merge master --allow-unrelated-histories -m "merge: merge V1 master into main"
```

合并完成后检查：

```powershell
git status --short --branch
git log --graph --oneline --decorate -n 20
```

运行测试后推送：

```powershell
.\mvnw.cmd test
git push food-flow-manager main
```

## 4. 常见问题

### 4.1 合并时 `path/to/file` 报错

`path/to/file` 只是示例占位符，执行时必须替换成真实文件路径。

错误示例：

```powershell
git checkout --theirs path/to/file
```

正确示例：

```powershell
git checkout --theirs README.md
git add README.md
```

### 4.2 合并冲突如何处理

查看冲突文件：

```powershell
git status
git diff --name-only --diff-filter=U
```

冲突文件中通常会出现：

```text
<<<<<<< HEAD
当前分支内容
=======
被合并分支内容
>>>>>>> master
```

如果当前在 `main` 上合并 `master`：

- `ours` / `HEAD` 表示 `main` 当前内容。
- `theirs` 表示 `master` 中要合并进来的内容。

保留当前分支版本：

```powershell
git checkout --ours path/to/file
git add path/to/file
```

保留被合并分支版本：

```powershell
git checkout --theirs path/to/file
git add path/to/file
```

手动处理时，打开冲突文件，删掉 `<<<<<<<`、`=======`、`>>>>>>>` 标记，整理成最终内容，然后：

```powershell
git add path/to/file
```

所有冲突处理完成后提交：

```powershell
git status
git commit
```

如果冲突太乱或合并方向错误：

```powershell
git merge --abort
```

### 4.3 `AA README.md` 是什么意思

`AA` 表示两个分支都新增了同名文件，Git 不知道保留哪一份。

例如在 `main` 上合并 `master` 时：

```text
AA README.md
```

如果最终应以 `master` 的 README 为准：

```powershell
git checkout --theirs README.md
git add README.md
git commit
```

### 4.4 GitHub 贡献图不显示提交

常见原因：

- 提交作者邮箱没有绑定到 GitHub 账户。
- 提交没有进入仓库默认分支或 `gh-pages` 分支。
- 提交刚完成，GitHub 贡献图还未刷新。
- 提交在 fork、私有仓库或非默认分支中，统计规则不同。

查看本地提交作者：

```powershell
git log --pretty=format:"%h %an <%ae> %s" -n 10
```

查看远程默认分支：

```powershell
git remote show food-flow-manager
```

如果近期提交都在功能分支或 `master`，而 GitHub 默认分支是 `main`，需要把这些提交合并到 `main`，贡献图才更可能正常统计。

### 4.5 中文路径显示为八进制转义

如果 `git status` 中中文路径显示为：

```text
\350\277\207\347\250\213...
```

设置：

```powershell
git config --global core.quotepath false
```

这会让 Git 直接显示中文路径。

可能影响：

- 旧终端或非 UTF-8 环境可能显示乱码。
- 依赖 Git 默认转义输出的脚本可能受影响。

### 4.6 终端中文提交信息乱码

推荐配置：

```powershell
git config --global i18n.commitEncoding utf-8
git config --global i18n.logOutputEncoding utf-8
```

当前 PowerShell 临时切换到 UTF-8：

```powershell
chcp 65001
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$OutputEncoding = [System.Text.UTF8Encoding]::new()
```

这只影响 Git 命令行显示和提交日志编码，不影响 Spring Boot 运行时。

## 5. 本地 Git 配置

### 5.1 查看账户配置

查看所有配置：

```powershell
git config --list
```

查看用户名和邮箱：

```powershell
git config user.name
git config user.email
```

查看配置来源：

```powershell
git config --show-origin --get user.name
git config --show-origin --get user.email
```

### 5.2 设置账户配置

只设置当前仓库：

```powershell
git config user.name "hotwater-wty"
git config user.email "2622206360@qq.com"
```

设置全局默认：

```powershell
git config --global user.name "hotwater-wty"
git config --global user.email "2622206360@qq.com"
```

当前仓库配置只影响当前项目，全局配置影响本机所有没有单独配置账户的仓库。

### 5.3 查看远程配置

查看远程仓库地址：

```powershell
git remote -v
```

查看远程分支、默认分支和本地跟踪关系：

```powershell
git remote show food-flow-manager
```

### 5.4 推荐基础配置

```powershell
git config --global core.quotepath false
git config --global i18n.commitEncoding utf-8
git config --global i18n.logOutputEncoding utf-8
```

如需检查：

```powershell
git config --show-origin --get core.quotepath
git config --show-origin --get i18n.commitEncoding
git config --show-origin --get i18n.logOutputEncoding
```

## 6. 后续项目复用清单

1. 开发前先执行 `git status --short --branch`，确认所在分支和工作区状态。
2. 拉取前先保证工作区干净，优先使用 `git pull --ff-only`。
3. 每个独立功能点完成后及时提交，不把大量改动长期留在工作区。
4. 提交前必须查看 `git diff` 和 `git diff --cached`。
5. 一次提交只表达一个清晰意图，不混入无关改动。
6. 新功能建议从 `main` 创建功能分支，完成后合并回 `main`。
7. 合并前先创建备份分支，尤其是主分支整理、上线前合并等高风险操作。
8. 遇到冲突先执行 `git status` 和 `git diff --name-only --diff-filter=U`，确认真实冲突文件。
9. 冲突命令中的 `path/to/file` 必须替换成真实路径。
10. 在 `main` 合并 `master` 时，`ours` 是 `main`，`theirs` 是 `master`。
11. 合并后必须运行测试，再推送远程。
12. GitHub 贡献图异常时，优先检查提交邮箱、远程默认分支和提交是否已进入默认分支。
13. 中文路径显示异常时配置 `core.quotepath false`，中文日志乱码时配置 UTF-8。
14. 重要提交推送到远程后，才算具备本机之外的恢复能力。
