# Docker 容器配置与部署

## 1. 解决什么问题

本文档记录当前项目从 Dockerfile、docker-compose.yml 编写，到 Ubuntu 服务器部署上线的完整思路。它不是 Docker 教材，而是面向后续项目复用的部署笔记。

当前项目采用的部署方式是：

```text
服务器拉取代码
  -> 服务器本地执行 Maven 打包
  -> 将 jar 复制为 Dockerfile 约定名称
  -> Docker Compose 构建应用镜像
  -> 同时启动 MySQL 容器和 Spring Boot 应用容器
```

这个方案适合当前学习阶段：流程清晰，能理解 jar、镜像、容器、网络、环境变量之间的关系，也避免一开始就引入 CI/CD 或复杂镜像仓库。

## 2. 当前项目如何使用

当前项目包含：

```text
Dockerfile
docker-compose.yml
assets/schema.sql
```

Dockerfile 负责构建 Spring Boot 应用镜像。

docker-compose.yml 负责同时管理：

- MySQL 容器。
- Spring Boot 应用容器。
- 容器网络。
- 数据库数据卷。
- 数据库初始化 SQL。
- Spring Boot 数据源环境变量。

当前应用容器依赖一个固定名称的 jar：

```text
food-flow-manager.jar
```

因此部署前需要先执行 Maven 打包，并把生成的 jar 复制到项目根目录：

```bash
./mvnw clean package -DskipTests
cp target/food-flow-manager-0.0.1-SNAPSHOT.jar food-flow-manager.jar
```

## 3. 核心配置或代码示例

### 3.1 Dockerfile 基本结构

当前项目使用 Java 17 JRE 镜像：

```dockerfile
FROM eclipse-temurin:17-jre-jammy
```

选择原因：

- 当前项目编译目标是 Java 17。
- 运行 Spring Boot jar 只需要 JRE，不需要完整 JDK。
- `eclipse-temurin` 是常用 Java 官方镜像来源。
- 宿主机是 Ubuntu 24.04，不要求容器基础镜像也必须是 Ubuntu 24.04。

编码和时区配置：

```dockerfile
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"
```

作用：

- 保证 Java 进程按 UTF-8 处理文件和日志。
- 保证应用时区使用 `Asia/Shanghai`。
- 避免沿用旧课程中 CentOS7 + 手动安装 JDK 的方式。

应用目录和启动命令：

```dockerfile
RUN mkdir -p /app/food-flow-manager
WORKDIR /app/food-flow-manager

COPY food-flow-manager.jar food-flow-manager.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/food-flow-manager/food-flow-manager.jar"]
```

要点：

- `COPY food-flow-manager.jar` 要求 jar 文件位于 Docker 构建上下文中。
- 当前 `build.context` 是项目根目录，所以 jar 应放在 Dockerfile 同级目录。
- `EXPOSE 8080` 只是声明容器内部服务端口，真正对外暴露还需要 compose 的 `ports`。

### 3.2 docker-compose.yml 基本结构

当前 compose 文件包含两个服务：

```yaml
services:
  mysql:
    image: mysql:8

  food-flow-manager:
    build:
      context: .
      dockerfile: Dockerfile
```

其中：

- `mysql` 是数据库服务名。
- `food-flow-manager` 是应用服务名。
- `context: .` 表示 Docker 构建上下文是当前项目目录。

应用容器连接 MySQL 时，不能使用：

```text
localhost:3306
```

而应该使用：

```text
mysql:3306
```

原因是：

```text
本地运行 Spring Boot 时，localhost = 本机
容器内运行 Spring Boot 时，localhost = 应用容器自己
Compose 网络中访问 MySQL，应使用 MySQL 的 service 名 mysql
```

因此 compose 中使用环境变量覆盖 Spring Boot 数据源配置：

```yaml
environment:
  SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/food_flow_manager?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
  SPRING_DATASOURCE_USERNAME: root
  SPRING_DATASOURCE_PASSWORD: <mysql-password>
```

这里的密码必须和 MySQL 容器中设置的密码一致。

### 3.3 本地配置与容器配置的边界

`application-dev.yaml` 适合本地开发：

```text
jdbc:mysql://localhost:3306/food_flow_manager
```

compose 环境变量适合容器部署：

```text
jdbc:mysql://mysql:3306/food_flow_manager
```

不要为了 Docker 部署直接把本地 `application-dev.yaml` 改成 `mysql:3306`。否则本地 IDEA 或 Maven 启动项目时会找不到数据库。

当前推荐边界：

```text
本地开发：使用 application-dev.yaml
Docker 部署：使用 docker-compose.yml 的环境变量覆盖
```

### 3.4 MySQL 初始化脚本

MySQL 官方镜像会在首次初始化数据库目录时执行：

```text
/docker-entrypoint-initdb.d/
```

当前项目可将 SQL 挂载进去：

```yaml
volumes:
  - "/usr/local/app/mysql/data:/var/lib/mysql"
  - "./assets/schema.sql:/docker-entrypoint-initdb.d/schema.sql"
```

注意：

- `schema.sql` 只会在 MySQL 数据目录第一次为空时执行。
- 如果 `/usr/local/app/mysql/data` 已经存在旧数据，后续修改 SQL 不会自动重跑。
- 如果需要重新初始化，需要先确认数据可以删除，再清理对应数据目录。

### 3.5 Maven Wrapper 在 Linux 上的执行权限

Windows 使用：

```powershell
.\mvnw.cmd clean package -DskipTests
```

Linux 使用：

```bash
./mvnw clean package -DskipTests
```

如果 Linux 提示：

```text
Permission denied
```

需要执行：

```bash
chmod +x mvnw
```

`chmod +x mvnw` 的作用是给 `mvnw` 脚本增加可执行权限。

## 4. 服务器部署流程

### 4.1 前置准备

服务器建议使用固定目录：

```text
/opt/food-flow-manager
```

或者：

```text
/usr/local/app/food-flow-manager
```

当前阶段不要把 `docker-compose.yml` 单独迁移到其他目录。因为当前 compose 依赖项目根目录中的：

- Dockerfile。
- food-flow-manager.jar。
- assets/schema.sql。

如果移动 compose 文件，需要同步修改 `build.context`、Dockerfile 路径、jar 路径和 SQL 挂载路径。

### 4.2 拉取代码

首次部署：

```bash
cd /opt
git clone <repository-url> food-flow-manager
cd food-flow-manager
```

更新部署：

```bash
cd /opt/food-flow-manager
git pull
```

### 4.3 服务器本地打包

```bash
chmod +x mvnw
./mvnw clean package -DskipTests
cp target/food-flow-manager-0.0.1-SNAPSHOT.jar food-flow-manager.jar
```

当前不建议把 `food-flow-manager.jar` 提交到 Git。jar 是构建产物，应通过服务器打包、CI/CD 构建或手动上传产生。

### 4.4 启动容器

在 `docker-compose.yml` 所在目录执行：

```bash
docker compose up -d --build
```

含义：

- `up`：创建并启动 compose 中定义的服务。
- `-d`：后台运行。
- `--build`：启动前重新构建应用镜像。

### 4.5 验证部署

查看容器状态：

```bash
docker compose ps
```

查看应用日志：

```bash
docker compose logs -f food-flow-manager
```

查看 MySQL 日志：

```bash
docker compose logs -f mysql
```

访问健康检查：

```bash
curl http://localhost:8080/actuator/health
```

访问 Knife4j：

```text
http://服务器IP:8080/doc.html
```

部署成功不能只看容器是否启动，还要确认：

- 应用日志中出现 Spring Boot 启动成功信息。
- 没有数据库连接异常。
- `/actuator/health` 可访问。
- Knife4j 页面可访问。
- 至少一个核心接口可正常调用。

### 4.6 更新部署

后续代码更新后：

```bash
git pull
./mvnw clean package -DskipTests
cp target/food-flow-manager-0.0.1-SNAPSHOT.jar food-flow-manager.jar
docker compose up -d --build
```

必要时查看日志：

```bash
docker compose logs -f food-flow-manager
```

## 5. 常见问题

### 5.1 应用容器连接不上 MySQL

常见原因：

- 在容器中仍使用 `localhost:3306`。
- Spring 数据源密码和 MySQL 容器密码不一致。
- MySQL 容器还没完全启动，应用容器已经开始连接。
- 初始化 SQL 没有执行，数据库或表不存在。

排查顺序：

```bash
docker compose ps
docker compose logs -f mysql
docker compose logs -f food-flow-manager
```

核心判断：

```text
容器内连接 MySQL 使用 mysql:3306
本地连接 MySQL 使用 localhost:3306
```

### 5.2 `schema.sql` 没有执行

可能原因：

- MySQL 数据目录不是第一次初始化。
- SQL 挂载路径错误。
- SQL 文件不存在于服务器对应路径。

当前项目使用：

```yaml
./assets/schema.sql:/docker-entrypoint-initdb.d/schema.sql
```

因此执行 compose 的目录必须能找到：

```text
assets/schema.sql
```

### 5.3 `docker compose build` 找不到 jar

常见原因：

- 没有执行 Maven 打包。
- 没有把 target 下的 jar 复制到项目根目录。
- jar 名称和 Dockerfile 中的 `COPY` 名称不一致。

当前 Dockerfile 要求：

```text
food-flow-manager.jar
```

因此需要：

```bash
cp target/food-flow-manager-0.0.1-SNAPSHOT.jar food-flow-manager.jar
```

### 5.4 `depends_on` 不等于服务就绪

compose 中：

```yaml
depends_on:
  - mysql
```

只能保证 MySQL 容器先启动，不能保证 MySQL 已经可以接受连接。

当前 V1 可以先通过查看日志和手动重启处理。如果后续要增强部署可靠性，可以加入：

- MySQL healthcheck。
- 应用容器 restart 策略。
- 等待数据库就绪的启动脚本。

### 5.5 是否应该提交 jar 到 Git

不建议。

原因：

- jar 是构建产物，不是源代码。
- jar 文件较大，会污染 Git 历史。
- 后续每次构建都会产生新的 jar，版本管理成本高。

当前推荐：

```text
Git 管源码、Dockerfile、docker-compose.yml
服务器或 CI/CD 负责打包 jar
```

如果需要避免误提交项目根目录 jar，可以在 `.gitignore` 中加入：

```gitignore
/*.jar
```

注意不要写成简单的 `*.jar`，否则可能影响 Maven Wrapper 的 jar。

### 5.6 明文密码问题

当前学习阶段可以先在 compose 中写明数据库密码，便于理解部署链路。

后续更推荐使用：

- `.env` 文件。
- 服务器环境变量。
- 非 root 数据库账号。
- 部署平台的密钥管理能力。

不要在 README 或公开文档中暴露真实生产密码。

## 6. 后续项目复用清单

新 Java 后端项目接入 Docker 部署时，可以按以下顺序检查：

1. 确认 Java 版本，选择合适基础镜像，例如 `eclipse-temurin:17-jre-jammy`。
2. Dockerfile 中只放运行时必需内容，不手动安装 JDK。
3. 明确 jar 名称和 Dockerfile `COPY` 路径。
4. 区分本地数据库地址 `localhost` 和容器网络地址 `mysql`。
5. 使用 compose 环境变量覆盖容器部署配置。
6. MySQL 初始化 SQL 挂载到 `/docker-entrypoint-initdb.d/`。
7. 明确 SQL 只在数据目录首次初始化时执行。
8. 在服务器执行 `chmod +x mvnw` 后再运行 Maven Wrapper。
9. 在 `docker-compose.yml` 所在目录执行 `docker compose up -d --build`。
10. 用 `docker compose ps`、日志、健康检查和核心接口验证部署。
11. 不提交 jar 到 Git，jar 由服务器打包或 CI/CD 生成。
12. V2 或部署增强阶段再引入 `.env`、healthcheck、非 root 数据库账号、镜像仓库和 CI/CD。
