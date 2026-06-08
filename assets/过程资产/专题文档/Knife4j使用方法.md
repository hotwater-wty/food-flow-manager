# Knife4j / OpenAPI 使用方法

本文档记录当前项目中 Knife4j / OpenAPI 的使用方式。它不是完整官方教程，而是面向本项目和后续 Java 后端项目的可复用实践笔记。

## 1. 解决什么问题

Knife4j / OpenAPI 用于根据 Spring Boot 代码生成接口文档，并提供可视化调试页面。

在本项目中，它主要解决：

- 后端接口没有前端页面时，仍能查看和调试接口。
- 接口路径、参数、响应结构可以从代码中生成。
- Apifox 可以通过 `/v3/api-docs` 导入接口定义，减少手动录入。

需要注意：接入依赖后只能自动识别基础路径和请求方法。要让接口说明、参数说明、响应结构完整显示，仍需要在 Controller、DTO、VO 中补充 OpenAPI 注解。

## 2. 当前项目如何使用

当前项目使用 Knife4j 页面和 OpenAPI JSON 两个入口：

```text
http://localhost:8080/doc.html
http://localhost:8080/v3/api-docs
```

推荐用法：

- 日常查看接口：访问 `doc.html`。
- 导入 Apifox：使用 `/v3/api-docs`。
- 每完成一个模块，同步补充该模块的接口注解。

不建议一次性给全项目补完注解。这样成本高，也容易在业务尚未稳定时反复修改。

## 3. 常用注解

Controller 类上使用 `@Tag`：

```java
@Tag(name = "用户预约接口", description = "用户端预约创建、查询、取消")
```

Controller 方法上使用 `@Operation`：

```java
@Operation(summary = "创建预约", description = "用户选择空闲桌位并创建预约")
```

路径参数或请求参数使用 `@Parameter`：

```java
@Parameter(description = "预约ID", required = true, example = "1")
@PathVariable Long reservationId
```

DTO / VO 类和字段使用 `@Schema`：

```java
@Schema(description = "创建预约请求参数")
public class ReservationDTO {

    @Schema(description = "桌位ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tableId;
}
```

常用导入：

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
```

## 4. 推荐补充顺序

按模块推进：

1. 先给 Controller 类补 `@Tag`。
2. 给 Controller 方法补 `@Operation`。
3. 给路径参数补 `@Parameter`。
4. 给请求 DTO 和响应 VO 补 `@Schema`。
5. 重启项目查看 `doc.html`。
6. 需要同步到 Apifox 时，重新导入 `/v3/api-docs`。

当前项目建议遵循：

```text
完成一个业务模块 -> 补该模块注解 -> Knife4j 检查 -> Apifox 导入
```

这样接口文档会随着代码逐步完善，不会变成额外的大型文档维护任务。

## 5. Apifox 导入方式

在 Apifox 中选择导入 OpenAPI / Swagger 数据，地址使用：

```text
http://localhost:8080/v3/api-docs
```

导入前确认：

- 后端服务已经启动。
- `/v3/api-docs` 可以正常访问。
- 相关 Controller、DTO、VO 已补充必要注解。

如果 Apifox 中只有路径和方法，没有清晰说明，通常不是 Apifox 问题，而是代码中的 OpenAPI 注解不完整。

## 6. 常见问题

### 页面能打开，但接口信息很少

原因通常是只接入了 Knife4j / OpenAPI 依赖，但没有写注解。生成器只能根据 Spring MVC 映射推断基础接口，不能自动知道业务语义。

处理方式：

- Controller 类补 `@Tag`。
- Controller 方法补 `@Operation`。
- DTO / VO 补 `@Schema`。
- 路径参数补 `@Parameter`。

### DTO 字段没有说明

检查 DTO 字段是否加了 `@Schema`，以及是否使用了正确的导入包：

```java
import io.swagger.v3.oas.annotations.media.Schema;
```

### Apifox 导入后不是最新接口

先确认后端服务已重启，再重新导入 `/v3/api-docs`。如果接口路径或 DTO 修改后没有重启，Apifox 看到的仍可能是旧内容。

## 7. 后续项目复用清单

新项目使用 Knife4j / OpenAPI 时，按以下清单执行：

- 确认依赖和访问地址可用。
- 按模块补 `@Tag`、`@Operation`、`@Parameter`、`@Schema`。
- DTO 字段写清楚含义、示例和必填状态。
- VO 字段写清楚响应含义和状态码含义。
- 用 `/v3/api-docs` 同步到 Apifox。
- 不手写大而全接口文档，优先让代码注解驱动接口文档。

