# 使用来自Docker Hub的官方OpenJDK镜像
FROM openjdk:22-jdk

# 添加一个指向/tmp的卷
VOLUME /tmp

# 使容器外部可以访问8080端口
EXPOSE 8080

# 应用程序的jar文件
ARG WAR_FILE=target/*.war

# 将应用程序的jar文件添加到容器中
ADD ${WAR_FILE} app.war

# 运行jar文件
ENTRYPOINT ["java","-jar","/app.war","-l","8080"]