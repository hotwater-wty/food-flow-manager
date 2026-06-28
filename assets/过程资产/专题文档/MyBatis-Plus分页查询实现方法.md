# MyBatis-Plus 分页查询实现方法

本文档记录在当前 Spring Boot + MyBatis-Plus 项目中实现分页查询的基本方式。重点说明 `MyBatisConfig`、`PageResult`、`Page<T>` 的职责，以及如何把一个普通列表接口改造成分页接口。

## 1. 解决什么问题

V1 阶段很多列表接口直接返回全量数据，例如订单列表、预约列表、开台会话列表、菜品列表、桌位列表、员工列表等。

这种写法在数据量小时能工作，但数据增长后会出现几个问题：

- 单次接口返回数据过多，响应变慢。
- 数据库查询压力变大。
- 前端或 Apifox 查看结果不方便。
- 管理端列表缺少页码、总数、每页数量等基础信息。

V2 阶段的目标是：优先把管理端主要列表接口改造成统一分页查询。

## 2. 当前项目如何使用

当前项目已经有两个分页基础类：

```text
src/main/java/com/foodflow/config/MyBatisConfig.java
src/main/java/com/foodflow/common/result/PageResult.java
```

它们对应分页实现的两端：

```text
MyBatisConfig：配置 MyBatis-Plus 分页插件，让框架能够改写 SQL 并查询 total。
PageResult：定义接口对外返回的统一分页响应结构。
```

分页查询时，通常还会用到 MyBatis-Plus 提供的：

```java
com.baomidou.mybatisplus.extension.plugins.pagination.Page
```

三者职责可以这样理解：

```text
Page<T>：Service 内部使用的 MyBatis-Plus 分页查询对象。
PaginationInnerInterceptor：MyBatis-Plus 的 SQL 分页拦截器。
PageResult<T>：Controller 最终返回给调用方的分页结果。
```

## 3. MyBatisConfig 的作用

当前配置示例：

```java
@Configuration
public class MyBatisConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setDbType(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L);
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }
}
```

这段配置的作用是启用 MyBatis-Plus 分页插件。

当 Service 中执行：

```java
Page<DiningOrder> page = new Page<>(pageNo, pageSize);
page(page, queryWrapper);
```

MyBatis-Plus 会根据 MySQL 方言自动生成分页 SQL，例如：

```sql
limit ?, ?
```

同时会查询总记录数，填充到 `page.getTotal()`。

`setMaxLimit(500L)` 表示单页最多返回 500 条，用于防止调用方传入过大的 `pageSize`。

## 4. PageResult 的作用

当前分页返回结构：

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "分页响应结果")
public class PageResult<T> implements Serializable {
    private Long total;
    private Integer pageNo;
    private Integer pageSize;
    private List<T> records;
}
```

它是接口响应层的结构，不直接参与数据库查询。

典型响应形式：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 100,
    "pageNo": 1,
    "pageSize": 10,
    "records": []
  }
}
```

其中：

- `total`：符合条件的总记录数。
- `pageNo`：当前页码。
- `pageSize`：每页数量。
- `records`：当前页的数据列表。

## 5. 基本实现步骤

把一个普通列表接口改造成分页接口，通常按以下步骤：

```text
1. DTO 增加 pageNo、pageSize。
2. Service 返回值从 List<VO> 改成 PageResult<VO>。
3. Controller 返回值从 Result<List<VO>> 改成 Result<PageResult<VO>>。
4. Service 内部创建 Page<Entity>。
5. 使用 MyBatis-Plus 的 page(...) 执行分页查询。
6. 将 Entity records 转换成 VO records。
7. 封装 PageResult 返回。
```

## 6. 示例：管理端订单列表分页

管理端订单列表适合作为分页样板，因为它已经有动态查询条件：

```text
orderId
tableId
status
```

### 6.1 DTO 增加分页参数

可以先在 `DiningOrderDTO` 中增加：

```java
@Schema(description = "页码", example = "1")
private Integer pageNo = 1;

@Schema(description = "每页数量", example = "10")
private Integer pageSize = 10;
```

后续如果多个 DTO 都需要分页，可以再抽取公共分页 DTO 或分页参数基类。V2 初期可以先从单个接口做通。

### 6.2 Service 接口修改

原来：

```java
List<AdminDiningOrderVO> getAdminOrderList(DiningOrderDTO diningOrderDTO);
```

改成：

```java
PageResult<AdminDiningOrderVO> getAdminOrderList(DiningOrderDTO diningOrderDTO);
```

### 6.3 Controller 修改

原来：

```java
@GetMapping
public Result<List<AdminDiningOrderVO>> getAdminOrderList(
        @ParameterObject @Valid DiningOrderDTO diningOrderDTO) {
    return Result.success(diningOrderService.getAdminOrderList(diningOrderDTO));
}
```

改成：

```java
@GetMapping
public Result<PageResult<AdminDiningOrderVO>> getAdminOrderList(
        @ParameterObject @Valid DiningOrderDTO diningOrderDTO) {
    return Result.success(diningOrderService.getAdminOrderList(diningOrderDTO));
}
```

### 6.4 Service 实现示例

示例写法：

```java
@Override
public PageResult<AdminDiningOrderVO> getAdminOrderList(DiningOrderDTO dto) {
    Page<DiningOrder> pageParam = new Page<>(dto.getPageNo(), dto.getPageSize());

    LambdaQueryWrapper<DiningOrder> wrapper = new LambdaQueryWrapper<DiningOrder>()
            .eq(dto.getTableId() != null, DiningOrder::getTableId, dto.getTableId())
            .eq(dto.getOrderId() != null, DiningOrder::getId, dto.getOrderId())
            .eq(dto.getStatusEnum() != null, DiningOrder::getStatus, dto.getStatusEnum())
            .orderByDesc(DiningOrder::getCreateTime);

    Page<DiningOrder> orderPage = page(pageParam, wrapper);

    List<DiningOrder> orderList = orderPage.getRecords();
    if (orderList.isEmpty()) {
        return new PageResult<>(
                orderPage.getTotal(),
                dto.getPageNo(),
                dto.getPageSize(),
                List.of());
    }

    List<Long> tableIds = orderList.stream()
            .map(DiningOrder::getTableId)
            .distinct()
            .toList();

    Map<Long, DiningTable> tableMap = diningTableService.listByIds(tableIds)
            .stream()
            .collect(Collectors.toMap(DiningTable::getId, Function.identity()));

    List<AdminDiningOrderVO> records = orderList.stream()
            .map(order -> AdminDiningOrderVO.builder()
                    .orderId(order.getId())
                    .orderNo(order.getOrderNo())
                    .sessionId(order.getSessionId())
                    .tableId(order.getTableId())
                    .tableNo(tableMap.get(order.getTableId()) == null
                            ? "已删除桌位"
                            : tableMap.get(order.getTableId()).getTableNo())
                    .totalAmount(order.getTotalAmount())
                    .status(order.getStatus().getCode())
                    .createTime(order.getCreateTime())
                    .build())
            .toList();

    return new PageResult<>(
            orderPage.getTotal(),
            dto.getPageNo(),
            dto.getPageSize(),
            records);
}
```

这段代码中：

- `new Page<>(pageNo, pageSize)` 表示要查询第几页、每页多少条。
- `LambdaQueryWrapper` 表示筛选条件和排序规则。
- `page(pageParam, wrapper)` 执行分页查询。
- `orderPage.getRecords()` 获取当前页数据。
- `orderPage.getTotal()` 获取符合条件的总数。
- 最终将实体列表转换为 VO 列表，再封装成 `PageResult`。

## 7. Page 与 PageResult 的区别

不要把 `Page<T>` 直接返回给前端。

推荐分层：

```text
Service 内部：
Page<Entity>

接口响应：
PageResult<VO>
```

原因：

- `Page<T>` 是 MyBatis-Plus 的内部对象，字段较多，不适合作为项目统一响应契约。
- `PageResult<T>` 更干净，只保留当前项目需要的分页字段。
- 返回 VO 可以避免实体字段直接暴露给接口调用方。

## 8. 动态查询条件怎么写

MyBatis-Plus 支持条件参数：

```java
.eq(condition, column, value)
```

其中 `condition` 为 `true` 时才拼接该条件。

例如：

```java
.eq(dto.getTableId() != null, DiningOrder::getTableId, dto.getTableId())
.eq(dto.getStatusEnum() != null, DiningOrder::getStatus, dto.getStatusEnum())
```

含义：

```text
如果 tableId 不为空，则拼接 table_id = ?
如果 statusEnum 不为空，则拼接 status = ?
```

这样可以避免写大量 `if`。

## 9. 常见问题

### 9.1 忘记配置分页插件

如果没有配置 `PaginationInnerInterceptor`，`Page<T>` 可能无法按预期分页。

当前项目已有 `MyBatisConfig`，因此后续实现分页时不需要重复配置。

### 9.2 pageNo 和 pageSize 没有默认值

如果 DTO 中 `pageNo`、`pageSize` 是 `null`，创建 `Page<>(null, null)` 会导致分页行为不清晰。

建议 DTO 提供默认值：

```java
private Integer pageNo = 1;
private Integer pageSize = 10;
```

也可以增加校验：

```java
@Min(value = 1, message = "页码必须大于等于1")
private Integer pageNo = 1;

@Min(value = 1, message = "每页数量必须大于等于1")
private Integer pageSize = 10;
```

### 9.3 没有排序

分页查询如果没有排序，数据顺序可能不稳定。

管理端列表通常建议按创建时间倒序：

```java
.orderByDesc(DiningOrder::getCreateTime)
```

### 9.4 N+1 查询问题

分页查出订单后，如果循环中每条订单都查询一次桌位，会造成 N+1 查询。

不推荐：

```java
for (DiningOrder order : orderList) {
    DiningTable table = diningTableService.getById(order.getTableId());
}
```

推荐批量查询并转 Map：

```java
List<Long> tableIds = orderList.stream()
        .map(DiningOrder::getTableId)
        .distinct()
        .toList();

Map<Long, DiningTable> tableMap = diningTableService.listByIds(tableIds)
        .stream()
        .collect(Collectors.toMap(DiningTable::getId, Function.identity()));
```

### 9.5 直接返回实体

分页结果应返回 VO，不要直接返回实体。

实体偏数据库模型，VO 偏接口契约。分页只是查询方式变化，不改变接口层应返回 VO 的原则。

## 10. 当前项目推荐改造顺序

V2 中建议按以下顺序做分页：

```text
1. 管理端订单列表
2. 管理端预约列表
3. 管理端开台会话列表
4. 管理端菜品列表
5. 管理端桌位列表
6. 管理端员工列表
```

先用订单列表做样板，确认：

- DTO 参数。
- Service 返回结构。
- Controller 返回结构。
- Knife4j 展示效果。
- Apifox 调用方式。

样板跑通后，再复制到其他列表接口。

## 11. 后续项目复用清单

- 分页插件只需要统一配置一次。
- 内部查询用 `Page<Entity>`。
- 对外响应用 `PageResult<VO>`。
- DTO 中统一提供 `pageNo`、`pageSize`。
- 管理端列表分页优先按 `createTime desc` 排序。
- 动态条件使用 `.eq(condition, column, value)`。
- 关联数据批量查询后转 `Map`，避免 N+1 查询。
- 不直接返回实体，分页 records 中仍然放 VO。
- 单页上限通过 `PaginationInnerInterceptor#setMaxLimit` 控制。
