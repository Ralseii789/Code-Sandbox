# 使用官方 OpenJDK 21 的 slim 版本，体积小且足够运行 Spring Boot
FROM openjdk:21-jdk-slim

# 设置工作目录
WORKDIR /app

# 将宿主机的 JAR 包复制到镜像中，并重命名为 app.jar
COPY target/code-sandbox-*.jar app.jar

# 应用监听的端口（根据你的配置，这里为 8606）
EXPOSE 8606

# 设置时区（可选，避免日志时间偏差）
ENV TZ=Asia/Shanghai

# 启动命令：指定端口、激活 profile、传入密钥
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=8606 --spring.profiles.active=prod --auth.secret=${AUTH_SECRET}"]