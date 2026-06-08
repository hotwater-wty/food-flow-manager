# Knife4j 接口文档不可用排查与修复

本文档记录本项目中 `doc.html` 能打开但没有接口信息、下载 OpenAPI 文件返回业务错误 JSON 的排查和修复过程。它定位为问题排查与修复专题，不替代 `Knife4j使用方法.md`。

Knife4j 注解的具体写法见：

```text
assets/过程资产/专题文档/Knife4j使用方法.md
```

## 1. 解决什么问题

当前项目已经接入 Knife4j，并且补充了 Controller、DTO、VO 上的 OpenAPI 注解，但启动后访问：

```text
http://localhost:8080/doc.html
```

页面仍然没有展示接口信息。

在 Knife4j 页面下载 OpenAPI 文件时，文件内容不是 OpenAPI JSON，而是项目统一响应格式：

```json
{
  "code": 0,
  "msg": "Handler dispatch failed: java.lang.NoSuchMethodError: ...",
  "data": null
}
```

同时日志中还可能出现：

```text
系统全局异常 org.springframework.web.servlet.resource.NoResourceFoundException: No static resource favicon.ico.
```

这类问题说明 Knife4j 前端页面本身可以加载，但后端 OpenAPI 数据接口没有正常返回。

## 2. 当前项目如何修复

### 2.1 修复依赖兼容问题

项目使用：

```text
Spring Boot 3.5.14
Spring Framework 6.2.18
```

原依赖为：

```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>
```

该版本传递的 springdoc 版本是：

```text
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0
```

Spring Framework 6.2 中移除了旧 springdoc 依赖的 `ControllerAdviceBean(Object)` 构造器，因此生成 `/v3/api-docs` 时会触发 `NoSuchMethodError`。

修复方式是升级到 Knife4j Next：

```xml
<properties>
    <knife4j.version>5.0.8</knife4j.version>
</properties>

<dependency>
    <groupId>com.baizhukui</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>${knife4j.version}</version>
</dependency>
```

升级后依赖树中 springdoc 版本变为：

```text
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9
```

该版本可以正常适配当前 Spring Boot 3.5.x / Spring Framework 6.2.x。

### 2.2 增加 OpenAPI 分组配置

在 `application.yaml` 中增加：

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  group-configs:
    - group: default
      paths-to-match: /api/**
      packages-to-scan: com.foodflow.module

knife4j:
  enable: true
  setting:
    language: zh_cn
```

这样可以明确告诉 springdoc：

- 只生成 `/api/**` 下的业务接口。
- 只扫描 `com.foodflow.module` 下的业务 Controller。
- Knife4j 页面使用默认分组 `default`。

### 2.3 处理 favicon.ico 噪音异常

浏览器访问页面时会自动请求：

```text
/favicon.ico
```

如果项目没有 favicon 静态资源，Spring MVC 会抛出 `NoResourceFoundException`。由于项目中有全局异常处理器：

```java
@ExceptionHandler(Exception.class)
public Result<?> handleException(Exception ex)
```

该异常会被包装成业务错误 JSON，并记录为“系统全局异常”。

这个异常不是 Knife4j 无接口数据的根因，但会干扰排查。当前项目通过增加一个隐藏控制器处理：

```java
@Hidden
@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}
```

处理后 `/favicon.ico` 返回 `204`，不会再被全局异常处理器记录为系统错误。

## 3. 核心排查命令

### 3.1 查看 Knife4j 和 springdoc 实际版本

```powershell
.\mvnw.cmd dependency:tree "-Dincludes=org.springdoc,com.baizhukui,com.github.xiaoymin,org.springframework:spring-web,org.springframework:spring-webmvc"
```

正常结果应能看到类似：

```text
com.baizhukui:knife4j-openapi3-jakarta-spring-boot-starter:5.0.8
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9
org.springframework:spring-webmvc:6.2.18
```

如果仍看到：

```text
com.github.xiaoymin:knife4j-openapi3-jakarta-spring-boot-starter:4.5.0
org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0
```

说明依赖没有真正升级，或者 IDE / Maven 没有重新加载。

### 3.2 编译验证

```powershell
.\mvnw.cmd -DskipTests clean compile
```

编译通过只能说明依赖和代码没有编译错误，不能完全说明 Knife4j 页面可用。还需要运行项目后访问 OpenAPI 接口。

### 3.3 运行后验证 OpenAPI JSON

项目启动后访问：

```text
http://localhost:8080/v3/api-docs
```

正常结果：

- HTTP 状态码为 `200`。
- 返回内容是 OpenAPI JSON。
- JSON 中包含 `openapi`、`paths`、`components` 等字段。
- `paths` 中能看到 `/api/**` 接口。

异常结果：

- 返回项目统一错误 JSON。
- `msg` 中包含 `NoSuchMethodError`。
- Knife4j 页面没有接口分组或接口列表。

### 3.4 验证 Knife4j 页面

访问：

```text
http://localhost:8080/doc.html
```

正常结果：

- 左侧能看到按 `@Tag` 生成的接口分组。
- 每个分组下能看到 Controller 方法对应的接口。
- Swagger Models 中能看到 DTO / VO 模型。

如果 `/v3/api-docs` 正常，但 `doc.html` 仍显示旧内容，可以尝试：

- 停止旧服务后重新启动。
- IDE 重新加载 Maven。
- 浏览器强制刷新。
- 清理浏览器缓存。

## 4. 常见问题

### 4.1 doc.html 能打开，但没有接口

优先访问：

```text
http://localhost:8080/v3/api-docs
```

判断逻辑：

- `/v3/api-docs` 正常，但接口很少：检查 Controller 注解和扫描路径。
- `/v3/api-docs` 返回业务错误 JSON：优先看异常信息和依赖兼容性。
- `/v3/api-docs` 404：检查 springdoc 依赖和配置是否生效。

### 4.2 下载 OpenAPI 文件是业务错误 JSON

说明 Knife4j 前端下载的是后端接口返回值，而后端接口已经被全局异常处理器包装了。

处理顺序：

1. 打开下载的 JSON，看 `msg` 字段。
2. 如果包含 `NoSuchMethodError`，优先检查 springdoc 与 Spring Boot 版本兼容。
3. 如果包含鉴权错误，检查 JWT 拦截器是否拦截了 `/v3/api-docs`。
4. 如果包含业务异常，检查是否有全局 ControllerAdvice 或模型解析异常影响 OpenAPI 生成。

当前项目 JWT 拦截器只拦截 `/api/**`，不会拦截 `/v3/api-docs` 和 `/doc.html`。

### 4.3 favicon.ico 异常是否会导致接口文档不可用

通常不会。

`favicon.ico` 是浏览器自动请求站点图标导致的静态资源缺失异常。它会污染日志，但不是 `/v3/api-docs` 无法生成的根因。

当前项目单独处理 `/favicon.ico`，是为了减少误导性异常日志。

### 4.4 注解补完后仍然没有字段说明

这属于接口文档内容不完整问题，不是 Knife4j 不可用问题。

检查方向：

- Controller 类是否有 `@Tag`。
- Controller 方法是否有 `@Operation`。
- 路径参数和查询参数是否有 `@Parameter`。
- DTO / VO 类和字段是否有 `@Schema`。

具体写法见：

```text
assets/过程资产/专题文档/Knife4j使用方法.md
```

## 5. 后续项目复用清单

新项目或后续项目遇到 Knife4j 页面无接口数据时，按以下顺序排查：

1. 先访问 `/v3/api-docs`，不要只看 `doc.html` 页面。
2. 如果 `/v3/api-docs` 返回业务错误 JSON，先看 `msg` 中的真实异常。
3. 检查 Spring Boot、Spring Framework、Knife4j、springdoc 的版本组合。
4. 使用 `dependency:tree` 确认实际生效的 springdoc 版本。
5. 明确配置 `springdoc.group-configs`，限定扫描包和路径。
6. 检查 JWT 拦截器是否误拦截 `/v3/api-docs`、`/doc.html`、`/webjars/**`。
7. 处理 `/favicon.ico` 等非业务静态资源异常，避免干扰日志判断。
8. 最后再检查注解是否完整。

经验结论：

- `doc.html` 能打开，只说明 Knife4j 前端资源可访问。
- `/v3/api-docs` 正常返回，才说明 OpenAPI 数据生成可用。
- 接口数据为空或下载报错时，优先排查依赖兼容和 `/v3/api-docs`，不要一开始就怀疑注解。
