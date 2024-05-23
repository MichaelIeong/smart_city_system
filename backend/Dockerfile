# 使用官方的Maven镜像作为构建阶段
FROM maven AS build

# 设置工作目录
WORKDIR /app

# 将项目的pom.xml文件复制到容器中
COPY pom.xml .

# 下载项目依赖
RUN mvn dependency:go-offline -B

# 将项目的源代码复制到容器中
COPY src ./src

# 构建Spring Boot应用程序的WAR包
RUN mvn clean package -DskipTests

# 使用来自Docker Hub的官方OpenJDK镜像作为运行阶段
FROM openjdk:22

# 添加一个指向/tmp的卷
VOLUME /tmp

# 使容器外部可以访问8080端口
EXPOSE 8080

# 将构建阶段生成的WAR包添加到容器中
COPY --from=build /app/target/*.war app.war

# 运行WAR包
ENTRYPOINT ["java","-jar","/app.war"]