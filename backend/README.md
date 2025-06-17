# SCTAP Low-Code Tool 后端服务

本项目是智能城市低代码平台的后端模块，基于 Spring Boot 构建，整合了：

- **MySQL / MariaDB**（关系型数据库）
- **Neo4j**（图数据库）
- **Kafka / RabbitMQ**（消息队列）

---

## Java 22 运行时注意事项（必须添加 JVM 参数）

### 为什么需要添加 `--add-opens` 参数？

当你使用 **Java 22** 启动该项目时，如果包含了 **Neo4j 图数据库功能（如 `neo4jRepository`）**，Spring 在初始化 Neo4j 映射上下文（`Neo4jMappingContext`）时会通过 **反射访问 `java.lang.String` 的私有字段 `value`**。

从 Java 17 开始，JVM 默认关闭了对这些内部字段的访问。如果没有开放访问权限，会导致运行失败。

为了解决这个问题，必须添加如下 JVM 参数：

```bash
--add-opens java.base/java.lang=ALL-UNNAMED
```
## 如何添加 VM 参数（以 IntelliJ IDEA 为例）

1. 打开 IntelliJ IDEA。
2. 点击顶部菜单栏：Run > Edit Configurations...
3. 找到运行配置项，例如 `SctapLowCodeToolApplication`。
4. 在 VM options 栏添加： -add-opens java.base/java.lang=ALL-UNNAMED

5. 点击 Apply → OK，重新运行即可。

---

## 技术栈

- Java 22（需添加 VM 参数）
- Spring Boot 3.2.x
- Spring Data JPA + Mysql + Neo4j
- Kafka / RabbitMQ
- Lombok / Swagger