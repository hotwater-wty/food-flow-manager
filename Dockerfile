# 使用 eclipse-temurin:17-jre-jammy 作为基础镜像
FROM eclipse-temurin:17-jre-jammy

# 统一编码
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV TZ=Asia/Shanghai
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"

# 创建应用目录
RUN mkdir -p /app/food-flow-manager
WORKDIR /app/food-flow-manager

# 复制应用 JAR 文件到容器
COPY  food-flow-manager.jar  food-flow-manager.jar

# 暴露端口
EXPOSE 8080

# 运行命令
ENTRYPOINT ["java","-jar","/app/food-flow-manager/food-flow-manager.jar"]
