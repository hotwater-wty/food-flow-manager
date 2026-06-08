# JavaWeb HTTP 请求与参数接收方式

本文档记录 JavaWeb / Spring Boot 项目中 HTTP 请求方式、RESTful API、参数传递方式和 Controller 接收方式的常用规则。它不是完整 HTTP 协议教材，而是面向当前项目和后续 Java 后端项目的可复用实践笔记。

## 1. 解决什么问题

后端接口设计时，经常会混淆以下问题：

- 查询接口应该用 `GET` 还是 `POST`。
- 查询条件应该放 query 参数还是请求体。
- 资源 ID 应该放路径里还是请求体里。
- `@PathVariable`、`@RequestParam`、`@RequestBody`、`@RequestHeader` 分别什么时候用。
- 请求头里的 token 和请求体里的业务参数有什么区别。

当前项目中，用户查询订单列表曾写成：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @Valid @RequestBody DiningOrderDTO diningOrderDTO) {
    // ...
}
```

这类写法能否运行取决于客户端和框架细节，但不符合常见 RESTful API 使用习惯。更推荐：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @Valid DiningOrderDTO diningOrderDTO) {
    // ...
}
```

请求形式：

```http
GET /api/user/orders?status=1&tableId=3
```

## 2. HTTP 请求由哪些部分组成

一个 HTTP 请求通常由四部分组成：

```text
请求方法 + URL + 请求头 + 请求体
```

示例：

```http
POST /api/user/sessions/1/orders HTTP/1.1
Authorization: Bearer xxx.yyy.zzz
Content-Type: application/json

{
  "items": [
    {
      "dishId": 1,
      "quantity": 2,
      "remark": "少辣"
    }
  ]
}
```

含义：

- 请求方法：`POST`，表示创建资源或提交业务动作。
- URL：`/api/user/sessions/1/orders`，表示操作哪个资源入口。
- 请求头：`Authorization` 表示登录身份，`Content-Type` 表示请求体格式。
- 请求体：JSON 数据，表示本次创建订单需要的复杂业务数据。

## 3. RESTful API 的基本思想

RESTful API 的核心不是固定格式，而是让接口围绕“资源”和“操作语义”组织。

常见理解：

- URL 表示资源。
- HTTP 方法表示对资源做什么。
- 状态码和响应体表示处理结果。

例如订单资源：

```text
GET    /api/user/orders               查询订单列表
GET    /api/user/orders/{orderId}     查询订单详情
POST   /api/user/sessions/{id}/orders 创建订单
DELETE /api/user/orders/{orderId}     取消或删除订单，具体看业务语义
```

实际项目中不必机械追求“纯 REST”。例如启用、禁用、取消这类业务动作，可以使用动作型路径：

```text
POST /api/admin/dishes/{dishId}/status
POST /api/user/reservations/{reservationId}/cancel
```

关键是保持团队和项目内部一致。

## 4. GET / POST / PUT / PATCH / DELETE 的使用时机

### GET

用于查询资源，不应修改服务器业务状态。

适合：

```http
GET /api/user/orders?status=1&tableId=3
GET /api/user/orders/10
GET /api/user/dishes?categoryId=2
```

Spring Boot 示例：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(@Valid DiningOrderDTO query) {
    return Result.success(diningOrderService.getOrderList(query));
}
```

要点：

- 查询条件优先放 query 参数。
- 不建议使用 `@RequestBody`。
- 适合列表、详情、筛选、分页等场景。

### POST

用于创建资源，或提交一个会产生业务变化的动作。

适合：

```http
POST /api/user/sessions/{sessionId}/orders
POST /api/user/reservations
POST /api/admin/dishes
POST /api/admin/dishes/{dishId}/status
```

Spring Boot 示例：

```java
@PostMapping("/sessions/{sessionId}/orders")
public Result<DiningOrderCreateVO> createOrder(
        @PathVariable Long sessionId,
        @Valid @RequestBody OrderItemCreateDTO dto) {
    return Result.success(diningOrderService.createOrder(sessionId, dto));
}
```

要点：

- 创建资源时，复杂数据放请求体。
- 业务动作不容易用 `PUT` / `DELETE` 表达时，可以使用 `POST`。
- 例如“启用/禁用”“取消预约”“扫码占座”都可以使用 `POST`。

### PUT

用于整体更新资源。

适合：

```http
PUT /api/admin/dishes/{dishId}
PUT /api/admin/tables/{tableId}
```

Spring Boot 示例：

```java
@PutMapping("/dishes/{dishId}")
public Result<DishVO> updateDish(
        @PathVariable Long dishId,
        @Valid @RequestBody DishUpdateDTO dto) {
    return Result.success(dishService.updateDish(dishId, dto));
}
```

要点：

- 路径变量定位要改哪个资源。
- 请求体描述更新后的资源数据。
- 当前项目中，菜品资料、桌位资料、分类资料修改可使用 `PUT`。

### PATCH

用于局部更新资源。

示例：

```http
PATCH /api/admin/dishes/{dishId}
```

当前项目 V1 可以暂不主动使用 `PATCH`，原因是：

- 学习成本略高。
- 和 `PUT`、动作型 `POST` 容易混淆。
- 当前业务规模下，用 `PUT` 做资料更新，用 `POST` 做状态动作已经足够清晰。

后续如果需要支持“只更新某几个字段”，再考虑引入。

### DELETE

用于删除资源。

适合：

```http
DELETE /api/admin/tables/{tableId}
DELETE /api/admin/dishes/{dishId}
DELETE /api/admin/dish-categories/{categoryId}
```

Spring Boot 示例：

```java
@DeleteMapping("/tables/{tableId}")
public Result<Void> deleteTable(@PathVariable Long tableId) {
    diningTableService.deleteTable(tableId);
    return Result.success();
}
```

要点：

- 资源 ID 通常放路径变量。
- 如果是“取消订单”“取消预约”这类业务动作，不一定要用 `DELETE`，也可以用 `POST /cancel` 表达业务语义。

## 5. 路径变量、Query 参数、请求体的区别

### 路径变量 PathVariable

用于定位资源。

示例：

```http
GET /api/user/orders/10
PUT /api/admin/dishes/3
DELETE /api/admin/tables/5
```

Spring Boot：

```java
@GetMapping("/orders/{orderId}")
public Result<UserDiningOrderDetailVO> getOrderDetail(
        @PathVariable Long orderId) {
    // ...
}
```

判断规则：

```text
这个参数是否用于说明“操作哪一个资源”？
是 -> 放路径变量
```

### Query 参数

用于查询、筛选、排序、分页等条件。

示例：

```http
GET /api/user/orders?status=1&tableId=3
GET /api/user/dishes?categoryId=2
GET /api/admin/sessions?status=1&tableId=3
```

Spring Boot 可以用单个参数接收：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) Long tableId) {
    // ...
}
```

也可以用 DTO 接收：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @Valid DiningOrderDTO query) {
    // ...
}
```

DTO 示例：

```java
public class DiningOrderDTO {
    private Long tableId;
    private Long orderId;

    @Min(value = 1, message = "状态不能小于1")
    @Max(value = 5, message = "状态不能大于5")
    private Integer status;
}
```

判断规则：

```text
这个参数是否用于过滤一批资源？
是 -> 放 query 参数
```

### 请求体 RequestBody

用于提交复杂业务数据。

示例：

```http
POST /api/user/sessions/1/orders
Content-Type: application/json

{
  "items": [
    {
      "dishId": 1,
      "quantity": 2,
      "remark": "少辣"
    }
  ]
}
```

Spring Boot：

```java
@PostMapping("/sessions/{sessionId}/orders")
public Result<DiningOrderCreateVO> createOrder(
        @PathVariable Long sessionId,
        @Valid @RequestBody OrderItemCreateDTO dto) {
    // ...
}
```

判断规则：

```text
这个参数是否是复杂业务对象，且主要用于创建或更新？
是 -> 放请求体
```

## 6. 请求头的作用与常见场景

请求头用于传递和业务数据不同的“请求元信息”。

常见请求头：

```text
Authorization: Bearer xxx.yyy.zzz
Content-Type: application/json
Accept: application/json
```

### Authorization

用于传递登录凭证。

当前项目中，JWT token 通常放在：

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

服务端一般由拦截器统一解析，不需要每个 Controller 都写。

如果某个接口确实要读取请求头，可以使用：

```java
@GetMapping("/profile")
public Result<Void> profile(
        @RequestHeader("Authorization") String authorization) {
    // ...
}
```

但当前项目更推荐在拦截器里解析 token，然后通过 `LoginContext` 获取登录用户信息：

```java
Long userId = LoginContext.getUserId();
```

### Content-Type

说明请求体格式。

提交 JSON 时：

```http
Content-Type: application/json
```

如果前端或 Apifox 没有设置正确的 `Content-Type`，后端的 `@RequestBody` 可能无法正常解析 JSON。

### Accept

说明客户端希望接收什么格式的响应。

常见：

```http
Accept: application/json
```

普通 Spring Boot 项目中，这个字段通常不需要手动处理。

## 7. Spring Boot 中如何接收这些参数

### 接收路径变量

```java
@GetMapping("/orders/{orderId}")
public Result<UserDiningOrderDetailVO> getOrderDetail(
        @PathVariable Long orderId) {
    // ...
}
```

### 接收 query 参数

单个参数：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @RequestParam(required = false) Integer status) {
    // ...
}
```

多个参数建议使用 DTO：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @Valid DiningOrderDTO query) {
    // ...
}
```

注意：DTO 接收 query 参数时，不要加 `@RequestBody`。

### 接收请求体

```java
@PostMapping("/orders")
public Result<DiningOrderCreateVO> createOrder(
        @Valid @RequestBody OrderCreateDTO dto) {
    // ...
}
```

### 接收请求头

```java
@GetMapping("/demo")
public Result<Void> demo(
        @RequestHeader("Authorization") String authorization) {
    // ...
}
```

### 校验参数

请求体 DTO：

```java
@PostMapping("/orders")
public Result<DiningOrderCreateVO> createOrder(
        @Valid @RequestBody OrderCreateDTO dto) {
    // ...
}
```

Query DTO：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @Valid DiningOrderDTO query) {
    // ...
}
```

DTO 字段：

```java
public class DiningOrderDTO {
    @Min(value = 1, message = "状态不能小于1")
    @Max(value = 5, message = "状态不能大于5")
    private Integer status;
}
```

## 8. 当前项目中的推荐写法

### 用户查询订单列表

推荐：

```http
GET /api/user/orders?status=1&tableId=3
```

Controller：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @Valid DiningOrderDTO diningOrderDTO) {
    List<UserDiningOrderVO> list = diningOrderService.getOrderList(diningOrderDTO);
    return Result.success(list);
}
```

不推荐：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @Valid @RequestBody DiningOrderDTO diningOrderDTO) {
    // ...
}
```

原因：

- `GET` 语义是查询，查询条件适合放 query 参数。
- 很多客户端、代理或工具不会稳定处理 GET 请求体。
- Apifox、Knife4j、浏览器地址栏都更适合表达 query 参数。

### 用户创建订单

推荐：

```http
POST /api/user/sessions/{sessionId}/orders
```

请求体：

```json
{
  "items": [
    {
      "dishId": 1,
      "quantity": 2,
      "remark": "少辣"
    }
  ]
}
```

Controller：

```java
@PostMapping("/sessions/{sessionId}/orders")
public Result<DiningOrderCreateVO> createOrder(
        @PathVariable Long sessionId,
        @Valid @RequestBody OrderItemCreateDTO dto) {
    return Result.success(diningOrderService.createOrder(sessionId, dto));
}
```

这里：

- `sessionId` 放路径变量，因为它定位在哪个开台会话下创建订单。
- `items` 放请求体，因为它是复杂业务数据。

### 管理端修改菜品

推荐：

```http
PUT /api/admin/dishes/{dishId}
```

Controller：

```java
@PutMapping("/dishes/{dishId}")
public Result<DishVO> updateDish(
        @PathVariable Long dishId,
        @Valid @RequestBody DishUpdateDTO dto) {
    return Result.success(dishService.updateDish(dishId, dto));
}
```

### 管理端修改菜品状态

推荐：

```http
POST /api/admin/dishes/{dishId}/status
```

或者：

```http
POST /api/admin/dishes/{dishId}/enable
POST /api/admin/dishes/{dishId}/disable
```

这里用 `POST` 是因为“启用/禁用”是业务动作，不是简单资料更新。

### 管理端删除桌位

推荐：

```http
DELETE /api/admin/tables/{tableId}
```

Controller：

```java
@DeleteMapping("/tables/{tableId}")
public Result<Void> deleteTable(@PathVariable Long tableId) {
    diningTableService.deleteTable(tableId);
    return Result.success();
}
```

## 9. 常见错误与判断规则

### 错误一：GET 使用请求体传查询条件

不推荐：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        @RequestBody DiningOrderDTO dto) {
    // ...
}
```

推荐：

```java
@GetMapping("/orders")
public Result<List<UserDiningOrderVO>> getOrderList(
        DiningOrderDTO dto) {
    // ...
}
```

### 错误二：资源 ID 放请求体里

不推荐：

```http
PUT /api/admin/dishes
```

```json
{
  "dishId": 3,
  "name": "番茄炒蛋"
}
```

推荐：

```http
PUT /api/admin/dishes/3
```

```json
{
  "name": "番茄炒蛋"
}
```

资源 ID 放路径里，更新内容放请求体里。

### 错误三：把 token 放请求体

不推荐：

```json
{
  "token": "xxx.yyy.zzz",
  "name": "番茄炒蛋"
}
```

推荐：

```http
Authorization: Bearer xxx.yyy.zzz
```

token 是认证信息，不是业务数据。

### 错误四：所有操作都用 POST

能跑，但接口语义会混乱。

推荐最低标准：

```text
查询：GET
创建：POST
整体修改：PUT
删除：DELETE
业务动作：POST
```

### 快速判断规则

```text
要查数据 -> GET + query 参数
要查某一条 -> GET + 路径变量
要创建复杂数据 -> POST + 请求体
要整体更新某个资源 -> PUT + 路径变量 + 请求体
要删除某个资源 -> DELETE + 路径变量
要执行业务动作 -> POST + 动作路径
要传登录凭证 -> 请求头 Authorization
```

## 10. 后续项目复用清单

设计接口时按以下顺序判断：

1. 这次操作是在查询、创建、修改、删除，还是执行业务动作。
2. 根据操作语义选择 `GET`、`POST`、`PUT`、`DELETE`。
3. 用路径变量定位资源 ID。
4. 用 query 参数表达筛选、分页、排序条件。
5. 用请求体表达复杂创建或更新数据。
6. 用请求头表达 token、内容类型等请求元信息。
7. 在 Spring Boot 中：
   - 路径变量用 `@PathVariable`
   - 单个 query 参数用 `@RequestParam`
   - 多个 query 参数可用普通 DTO
   - JSON 请求体用 `@RequestBody`
   - 请求头用 `@RequestHeader`
8. 不确定时先问一句：这个参数是在定位资源、筛选资源、提交业务数据，还是传递请求元信息。

这套规则不要求接口设计完全符合理论上的 RESTful，只要求在项目内语义稳定、调用方便、文档清晰。
