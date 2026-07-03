# syntax=docker/dockerfile:1

# 构建阶段：使用 JDK 镜像在容器内完成 Maven 打包
FROM eclipse-temurin:17-jdk-jammy AS builder

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV TZ=Asia/Shanghai
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

WORKDIR /build

# 先复制 Maven Wrapper 和 pom.xml，利用 Docker 层缓存依赖下载结果
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# 再复制源码并打包，避免每次源码变化都重新下载依赖
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# 运行阶段：只保留 JRE 和最终 jar，减小运行镜像体积
FROM eclipse-temurin:17-jre-jammy

ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

WORKDIR /app/food-flow-manager

COPY --from=builder /build/target/food-flow-manager-0.0.1-SNAPSHOT.jar food-flow-manager.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/food-flow-manager/food-flow-manager.jar"]
