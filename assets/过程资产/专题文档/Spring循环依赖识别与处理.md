# Spring 循环依赖识别与处理

本文档记录 Spring Boot 项目中循环依赖的识别、规避和处理方式。重点来自当前项目清台功能中的一次实际问题：`DiningOrderServiceImpl` 依赖 `DiningSessionService`，而 `DiningSessionServiceImpl` 又依赖 `DiningOrderService`，导致 Spring 容器启动失败。

## 1. 解决什么问题

循环依赖指两个或多个 Bean 在创建时互相依赖，形成闭环：

```text
AService -> BService -> AService
```

当前项目中出现过：

```text
AdminDiningOrderController
 -> DiningOrderServiceImpl
 -> DiningSessionServiceImpl
 -> DiningOrderServiceImpl
```

Spring Boot 默认不鼓励循环依赖，较新版本中通常会直接启动失败。错误现象通常类似：

```text
The dependencies of some of the beans in the application context form a cycle
Requested bean is currently in creation
```

循环依赖不是单纯的 Spring 注入问题，本质通常是模块职责边界不清晰。

## 2. 当前项目中的问题背景

订单模块需要读取开台会话：

```java
public class DiningOrderServiceImpl {
    private final DiningSessionService diningSessionService;
}
```

这在“用户创建订单”场景中是合理的，因为创建订单前需要校验：

- 会话是否存在。
- 会话是否属于当前用户。
- 会话状态是否为 `WAITING`。
- 下单后需要推进会话状态为 `DINING`。

后来实现“店员清台释放桌位”时，清台逻辑放在 `DiningSessionServiceImpl` 中，又需要更新该会话下的订单状态，于是写成：

```java
public class DiningSessionServiceImpl {
    private final DiningOrderService diningOrderService;
}
```

这样就形成了：

```text
DiningOrderServiceImpl -> DiningSessionServiceImpl -> DiningOrderServiceImpl
```

## 3. 如何尽量避免循环依赖

### 3.1 先判断业务主导对象

不要只看“这次操作用到了哪些表”，而要看“这次操作主要改变哪个核心业务对象”。

例如：

- 创建订单：主导对象是订单，但会联动会话和桌位。
- 清台：主导对象是开台会话，最终释放桌位，并完成订单状态。

因此：

```text
创建订单 -> DiningOrderService
清台关台 -> DiningSessionService
```

### 3.2 保持 Service 依赖方向稳定

Service 之间可以依赖，但依赖方向要尽量稳定，不要互相调用。

较好的方向：

```text
业务用例 Service -> 基础资料 Service / Mapper
```

较差的方向：

```text
OrderService -> SessionService
SessionService -> OrderService
```

一旦出现双向依赖，通常说明有一部分逻辑应该下沉、拆分或改成低层数据访问。

### 3.3 避免为了复用而跨模块调用整套业务方法

清台只需要“查询并更新当前会话下的订单状态”，不一定需要调用 `DiningOrderService` 的完整业务能力。

如果调用 `DiningOrderService`，可能把订单模块中的其他业务规则、依赖和事务边界也一起带进来，扩大耦合。

### 3.4 把共享能力下沉到更低层组件

常见下沉方式：

```text
Service -> Mapper
Service -> Repository
Service -> Domain Helper
Service -> 独立的内部协作组件
```

当前项目 V1 采用最简单方式：在 `DiningSessionServiceImpl` 中注入 `DiningOrderMapper`，只做清台所需的数据访问。

## 4. 循环依赖无法避免时如何处理

这里的“无法避免”通常不是指真的没有办法，而是指短期内不想大规模重构。处理时优先级如下。

### 方案一：重新划分职责

这是首选方案。

如果两个 Service 互相调用，先判断：

- 是否有一个业务用例放错了 Service。
- 是否可以把某个用例移动到更合适的业务中心。
- 是否可以把公共逻辑抽成独立组件。

例如清台属于开台会话的生命周期结束动作，应保留在 `DiningSessionService`。

### 方案二：使用 Mapper / Repository 打破 Service 双向依赖

当 AService 只需要对 B 模块数据做简单查询或更新，而不是复用 BService 的完整业务流程时，可以直接依赖 BMapper。

当前项目的处理：

```java
private final DiningOrderMapper diningOrderMapper;
```

代替：

```java
private final DiningOrderService diningOrderService;
```

这可以避免：

```text
DiningOrderServiceImpl -> DiningSessionServiceImpl -> DiningOrderServiceImpl
```

同时保留清台事务边界在 `DiningSessionService.closeSession()` 中。

### 方案三：抽取独立协作组件

如果 Mapper 写法开始变复杂，可以抽出内部组件，例如：

```text
DiningOrderStatusUpdater
DiningOrderInternalService
DiningSessionCloser
```

要求：

- 该组件不要再反向依赖调用方。
- 只暴露非常明确的内部能力。
- 不承载完整 Controller 对外业务语义。

例如：

```java
public class DiningOrderStatusUpdater {
    public void completeServedOrdersBySessionId(Long sessionId) {
        // 批量更新订单状态
    }
}
```

### 方案四：延迟注入或允许循环依赖

例如使用 `@Lazy`、`ObjectProvider`，或者配置：

```yaml
spring:
  main:
    allow-circular-references: true
```

这些都不推荐作为常规方案。

原因：

- 只是绕过容器启动问题，没有解决模块边界问题。
- 后续调用时仍可能出现业务顺序混乱。
- 会让依赖关系越来越难理解。

只有在临时救火、历史系统无法快速重构时才考虑，并且应尽快还债。

## 5. 当前项目中的推荐做法

清台功能采用：

```text
DiningSessionServiceImpl
 -> DiningOrderMapper
 -> DiningTableService
 -> ReservationService
```

其中：

- `DiningSessionServiceImpl` 负责清台事务和会话生命周期。
- `DiningOrderMapper` 只负责查询、批量更新当前会话下的订单。
- `DiningOrderServiceImpl` 不参与清台，避免和会话模块互相依赖。

清台流程：

```text
读取会话 -> 校验会话 DINING
读取桌位 -> 校验桌位 DINING
读取订单 -> 禁止 PLACED / COOKING
SERVED 订单置为 COMPLETED
会话置为 COMPLETED
桌位置为 FREE 并清空 currentSessionId
```

## 6. 判断规则

遇到 Service 互相调用时，按以下顺序判断：

1. 这次业务用例真正主导的对象是谁。
2. 另一个模块是提供完整业务能力，还是只提供数据读取/更新。
3. 如果只是数据读取/更新，优先使用 Mapper / Repository。
4. 如果是公共业务能力，考虑抽取独立协作组件。
5. 不要优先使用 `@Lazy` 或允许循环依赖配置。

## 7. 后续项目复用清单

- Service 可以依赖其他 Service，但不要形成双向依赖。
- 业务用例放在主导状态变化的 Service 中。
- 只需要简单数据访问时，可以依赖 Mapper。
- 复杂共享能力应抽成独立组件，而不是互相调用。
- 出现循环依赖报错时，先画出依赖链，再判断哪条依赖应被移除或下沉。
- 不把 `spring.main.allow-circular-references=true` 当成正常解决方案。

