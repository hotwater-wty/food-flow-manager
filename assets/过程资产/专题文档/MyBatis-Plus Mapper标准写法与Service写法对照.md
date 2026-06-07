# MyBatis-Plus Mapper 标准写法与 Service 写法对照

本文档记录 MyBatis-Plus 中 `BaseMapper` 的标准用法，并和项目中常用的 `IService` / `ServiceImpl` 写法做对照。它的重点使用场景之一是：为了避免 Service 循环依赖，在某个业务 Service 中只注入对方模块的 Mapper，完成必要的数据查询或更新。

## 1. 解决什么问题

当前项目清台功能中，`DiningSessionServiceImpl` 原本想调用 `DiningOrderService` 更新订单状态，但这会形成循环依赖：

```text
DiningOrderServiceImpl -> DiningSessionServiceImpl -> DiningOrderServiceImpl
```

因此改为：

```java
private final DiningOrderMapper diningOrderMapper;
```

但 Mapper 和 Service 的 MyBatis-Plus 写法不同。常见误区是把 Service 写法套到 Mapper 上：

```java
diningOrderMapper.query()          // 错误
diningOrderMapper.updateWrapper()  // 错误
```

`query()`、`saveOrUpdateBatch()` 这类是 Service 层便捷方法，不属于 `BaseMapper`。

## 2. Service 写法和 Mapper 写法的核心区别

### Service 写法

Service 继承或实现：

```java
public interface DiningOrderService extends IService<DiningOrder> {
}

public class DiningOrderServiceImpl
        extends ServiceImpl<DiningOrderMapper, DiningOrder>
        implements DiningOrderService {
}
```

常用方法：

```java
getById(id)
listByIds(ids)
query().eq("status", status).list()
updateById(entity)
save(entity)
saveOrUpdateBatch(list)
```

特点：

- 写法简洁。
- 适合本模块 Service 内部使用。
- 封装了 Mapper，提供很多便捷方法。

### Mapper 写法

Mapper 继承：

```java
@Mapper
public interface DiningOrderMapper extends BaseMapper<DiningOrder> {
}
```

常用方法：

```java
selectById(id)
selectList(wrapper)
insert(entity)
updateById(entity)
update(entity, wrapper)
deleteById(id)
```

特点：

- 更接近数据访问层。
- 没有 `query()`、`saveBatch()`、`saveOrUpdateBatch()`。
- 条件查询和更新需要显式传入 `Wrapper`。
- 适合在避免 Service 循环依赖时做轻量数据操作。

## 3. 查询写法对照

### 根据 ID 查询

Service 写法：

```java
DiningOrder order = diningOrderService.getById(orderId);
```

Mapper 写法：

```java
DiningOrder order = diningOrderMapper.selectById(orderId);
```

### 条件查询列表

Service 写法：

```java
List<DiningOrder> orderList = diningOrderService.query()
        .eq("session_id", sessionId)
        .list();
```

Mapper 写法：

```java
List<DiningOrder> orderList = diningOrderMapper.selectList(
        new LambdaQueryWrapper<DiningOrder>()
                .eq(DiningOrder::getSessionId, sessionId));
```

推荐优先使用 `LambdaQueryWrapper`，因为：

- 字段来自实体 getter，不容易写错列名。
- 重构字段名时更容易被 IDE 发现。
- 比字符串 `"session_id"` 更安全。

### 多条件查询

Service 写法：

```java
List<DiningOrder> orderList = diningOrderService.query()
        .eq("session_id", sessionId)
        .eq("status", OrderStatusEnum.SERVED)
        .list();
```

Mapper 写法：

```java
List<DiningOrder> orderList = diningOrderMapper.selectList(
        new LambdaQueryWrapper<DiningOrder>()
                .eq(DiningOrder::getSessionId, sessionId)
                .eq(DiningOrder::getStatus, OrderStatusEnum.SERVED));
```

### 动态条件查询

Service 写法：

```java
List<DiningOrder> orderList = diningOrderService.query()
        .eq(dto.getTableId() != null, "table_id", dto.getTableId())
        .eq(dto.getStatusEnum() != null, "status", dto.getStatusEnum())
        .list();
```

Mapper 写法：

```java
List<DiningOrder> orderList = diningOrderMapper.selectList(
        new LambdaQueryWrapper<DiningOrder>()
                .eq(dto.getTableId() != null, DiningOrder::getTableId, dto.getTableId())
                .eq(dto.getStatusEnum() != null, DiningOrder::getStatus, dto.getStatusEnum()));
```

`eq(condition, column, value)` 的第一个参数表示是否拼接该条件。

## 4. 更新写法对照

### 根据 ID 更新

Service 写法：

```java
order.setStatus(OrderStatusEnum.COMPLETED);
order.setUpdateTime(now);
diningOrderService.updateById(order);
```

Mapper 写法：

```java
order.setStatus(OrderStatusEnum.COMPLETED);
order.setUpdateTime(now);
diningOrderMapper.updateById(order);
```

适合已经查出完整实体，且明确更新这条记录。

### 按条件批量更新

Service 写法：

```java
diningOrderService.update()
        .set("status", OrderStatusEnum.COMPLETED)
        .set("update_time", now)
        .eq("session_id", sessionId)
        .eq("status", OrderStatusEnum.SERVED)
        .update();
```

Mapper 写法：

```java
DiningOrder orderUpdate = DiningOrder.builder()
        .status(OrderStatusEnum.COMPLETED)
        .updateTime(now)
        .build();

diningOrderMapper.update(orderUpdate,
        new LambdaUpdateWrapper<DiningOrder>()
                .eq(DiningOrder::getSessionId, sessionId)
                .eq(DiningOrder::getStatus, OrderStatusEnum.SERVED));
```

含义：

```text
把 sessionId = ? 且 status = SERVED 的订单
更新为 status = COMPLETED, updateTime = now
```

注意：

- `orderUpdate` 中非空字段会作为更新值。
- `LambdaUpdateWrapper` 中是更新条件。
- 不要把待更新实体和条件实体混在一起理解。

## 5. 当前项目清台功能中的 Mapper 写法

清台时先查询当前会话下所有订单：

```java
List<DiningOrder> orderList = diningOrderMapper.selectList(
        new LambdaQueryWrapper<DiningOrder>()
                .eq(DiningOrder::getSessionId, sessionId));
```

再做业务校验：

```java
for (DiningOrder order : orderList) {
    if (order.getStatus() == OrderStatusEnum.PLACED
     || order.getStatus() == OrderStatusEnum.COOKING) {
        throw new BusinessException("订单未完成，不能关闭会话");
    }
}
```

最后把已上齐的订单批量完成：

```java
DiningOrder orderUpdate = DiningOrder.builder()
        .status(OrderStatusEnum.COMPLETED)
        .updateTime(now)
        .build();

diningOrderMapper.update(orderUpdate,
        new LambdaUpdateWrapper<DiningOrder>()
                .eq(DiningOrder::getSessionId, sessionId)
                .eq(DiningOrder::getStatus, OrderStatusEnum.SERVED));
```

这样做的目的：

- 不依赖 `DiningOrderService`，避免循环依赖。
- 清台事务仍然由 `DiningSessionService.closeSession()` 统一控制。
- 订单状态更新只暴露为数据层操作，不把订单模块完整业务流程引入会话模块。

## 6. 使用 Mapper 时需要注意什么

### 6.1 Mapper 不负责业务语义

Mapper 只负责数据访问。业务校验仍要写在 Service 中。

例如不能只写：

```java
diningOrderMapper.update(orderUpdate, wrapper);
```

还必须先校验：

- 是否存在订单。
- 是否存在未制作或制作中的订单。
- 当前会话和桌位状态是否允许清台。

### 6.2 批量更新前先确认条件足够严格

错误风险：

```java
new LambdaUpdateWrapper<DiningOrder>()
        .eq(DiningOrder::getStatus, OrderStatusEnum.SERVED)
```

这会更新全系统所有已上齐订单。

正确做法：

```java
new LambdaUpdateWrapper<DiningOrder>()
        .eq(DiningOrder::getSessionId, sessionId)
        .eq(DiningOrder::getStatus, OrderStatusEnum.SERVED)
```

批量更新必须带足业务范围条件。

### 6.3 不要为了省事绕过 Service 边界

使用 Mapper 的前提是：

```text
当前业务只需要简单数据访问或局部状态更新
```

如果需要复用复杂订单业务规则，应该考虑：

- 调整业务归属。
- 抽取独立协作组件。
- 拆出内部领域服务。

而不是到处直接操作 Mapper。

## 7. 常用对照速查

```text
Service getById(id)
Mapper  selectById(id)

Service listByIds(ids)
Mapper  selectBatchIds(ids)

Service query().eq(...).list()
Mapper  selectList(new LambdaQueryWrapper<T>().eq(...))

Service updateById(entity)
Mapper  updateById(entity)

Service update().set(...).eq(...).update()
Mapper  update(updateEntity, new LambdaUpdateWrapper<T>().eq(...))

Service save(entity)
Mapper  insert(entity)
```

## 8. 后续项目复用清单

- 本模块常规业务优先使用 Service 写法。
- 为避免循环依赖、且只需要轻量数据操作时，可以注入对方 Mapper。
- Mapper 查询用 `selectList + LambdaQueryWrapper`。
- Mapper 条件更新用 `update(updateEntity, LambdaUpdateWrapper)`。
- Wrapper 条件必须包含明确业务范围。
- Mapper 只负责数据访问，业务校验仍放在当前 Service。
- 不要把 `query()`、`saveOrUpdateBatch()` 当成 Mapper 方法。

