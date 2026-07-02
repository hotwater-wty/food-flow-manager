# Redis 防重复提交 Token 实现方案

## 1. 解决什么问题

本文记录如何使用 Redis 实现“防重复提交 Token”。

它主要用于拦截用户在短时间内重复提交同一类写操作，例如：

- 连续点击“提交预约”。
- 连续点击“提交订单”。
- 网络抖动或前端重试导致同一个请求重复发送。
- 用户刷新或回退页面后重复提交同一份业务数据。

该方案不解决所有并发一致性问题。它更像请求入口处的一次性门闩：

```text
防重复提交 Token：挡住重复点击、重复请求
数据库条件更新 / 唯一约束：兜底业务状态一致性
Service 状态校验 / 事务：保证业务规则闭环
```

因此在当前项目中，它应作为 V2 并发与可靠性增强的补充，而不是替代已有的条件更新、唯一约束和业务状态校验。

## 2. 当前项目适用位置

当前项目适合优先接入防重复提交 Token 的接口是“会产生副作用”的写接口。

优先级较高：

- 创建预约。
- 创建堂食订单。
- 预约扫码开台。
- 未预约扫码占座。
- 店员清台释放桌位。

暂时不需要：

- 查询桌位列表。
- 查询菜品列表。
- 查询订单列表。
- 查询开台会话列表。

判断标准：

```text
如果接口会新增数据、修改状态、推动业务流程前进，就可以考虑加防重复提交 Token。
如果接口只是查询数据，一般不需要。
```

## 3. 核心流程

推荐流程如下：

```text
前端进入提交页面
  -> 请求后端生成提交 Token
  -> 后端生成随机 token，写入 Redis，设置短 TTL
  -> 前端提交业务请求时携带 token
  -> 后端先校验并消费 token
  -> token 消费成功：继续执行业务
  -> token 不存在、过期、已消费：拒绝请求
```

建议 token 使用一次后立即失效。

Redis key 推荐绑定用户、场景和 token：

```text
foodflow:submit-token:{loginType}:{loginId}:{scene}:{token}
```

示例：

```text
foodflow:submit-token:user:12:create-order:8f2d9a...
foodflow:submit-token:user:12:create-reservation:31ac77...
foodflow:submit-token:employee:3:clear-table:91bd0c...
```

这样可以避免：

- A 用户拿 B 用户的 token 提交。
- 创建预约 token 被拿去创建订单。
- 同一用户打开多个提交页面时 token 互相覆盖。

## 4. 为什么推荐直接删除 key

有两种常见设计。

第一种：

```text
key = foodflow:submit-token:user:12:create-order
value = token
```

校验时需要：

```text
GET key
比较 value
DEL key
```

这几步组合起来不是一个原子操作。高并发重复提交时，可能多个请求都在删除前读到 token 存在。如果采用这种设计，最好使用 Lua 脚本把“读取、比较、删除”合并成 Redis 原子逻辑。

第二种：

```text
key = foodflow:submit-token:user:12:create-order:{token}
value = 1
```

校验时只需要：

```text
DEL key
```

Redis 单条 `DEL` 命令本身具有原子性：

```text
第一次提交：key 存在，DEL 返回 true，放行
重复提交：key 已被删除，DEL 返回 false，拒绝
```

当前项目推荐先使用第二种方案。它简单、可读、容易调试，已经能覆盖 V2 阶段“防重复提交”的主要训练目标。

## 5. 核心代码示例

### 5.1 场景枚举

```java
package com.foodflow.common.enums;

import com.foodflow.common.exception.BusinessException;

public enum SubmitSceneEnum {

    CREATE_RESERVATION("create-reservation", "创建预约"),
    CREATE_ORDER("create-order", "创建订单"),
    OPEN_SESSION("open-session", "扫码开台"),
    CLEAR_TABLE("clear-table", "清台");

    private final String code;
    private final String description;

    SubmitSceneEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SubmitSceneEnum ofCode(String code) {
        for (SubmitSceneEnum scene : values()) {
            if (scene.code.equals(code)) {
                return scene;
            }
        }
        throw new BusinessException("提交场景不合法");
    }
}
```

### 5.2 Redis 常量

```java
public class CacheConstants {

    public static final String SUBMIT_TOKEN_PREFIX = "foodflow:submit-token:";

    private CacheConstants() {
    }
}
```

### 5.3 Token 服务

```java
package com.foodflow.common.service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.foodflow.common.constant.CacheConstants;
import com.foodflow.common.enums.LoginTypeEnum;
import com.foodflow.common.enums.SubmitSceneEnum;
import com.foodflow.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubmitTokenService {

    private static final long TOKEN_TTL_MINUTES = 5;

    private final StringRedisTemplate stringRedisTemplate;

    public String generateToken(LoginTypeEnum loginType, Long loginId, SubmitSceneEnum scene) {
        if (loginType == null || loginId == null || scene == null) {
            throw new BusinessException("生成提交令牌失败");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String key = buildKey(loginType, loginId, scene, token);

        stringRedisTemplate.opsForValue().set(key, "1", TOKEN_TTL_MINUTES, TimeUnit.MINUTES);
        return token;
    }

    public void validateAndConsume(LoginTypeEnum loginType, Long loginId,
            SubmitSceneEnum scene, String token) {
        if (loginType == null || loginId == null || scene == null) {
            throw new BusinessException("提交令牌校验失败");
        }
        if (token == null || token.isBlank()) {
            throw new BusinessException("请勿重复提交");
        }

        String key = buildKey(loginType, loginId, scene, token);
        Boolean deleted = stringRedisTemplate.delete(key);
        if (deleted == null || !deleted) {
            throw new BusinessException("请勿重复提交");
        }
    }

    private String buildKey(LoginTypeEnum loginType, Long loginId,
            SubmitSceneEnum scene, String token) {
        return CacheConstants.SUBMIT_TOKEN_PREFIX
                + loginType.name().toLowerCase()
                + ":"
                + loginId
                + ":"
                + scene.getCode()
                + ":"
                + token;
    }
}
```

## 6. 接口设计示例

### 6.1 获取提交 Token

请求 DTO：

```java
package com.foodflow.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmitTokenDTO {

    @NotBlank(message = "提交场景不能为空")
    private String scene;
}
```

响应 VO：

```java
package com.foodflow.common.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitTokenVO {

    private String token;

    private Long expiresInSeconds;
}
```

Controller 示例：

```java
@RestController
@RequiredArgsConstructor
public class SubmitTokenController {

    private final SubmitTokenService submitTokenService;

    @PostMapping("/api/user/submit-token")
    public Result<SubmitTokenVO> generateUserSubmitToken(
            @Valid @RequestBody SubmitTokenDTO submitTokenDTO) {
        LoginInfo loginInfo = LoginContext.get();
        SubmitSceneEnum scene = SubmitSceneEnum.ofCode(submitTokenDTO.getScene());

        String token = submitTokenService.generateToken(
                loginInfo.getLoginType(),
                loginInfo.getUserId(),
                scene);

        return Result.success(SubmitTokenVO.builder()
                .token(token)
                .expiresInSeconds(300L)
                .build());
    }
}
```

管理端如果也需要使用，可以增加：

```java
@PostMapping("/api/admin/submit-token")
```

并使用：

```java
loginInfo.getEmployeeId()
```

## 7. 在业务接口中使用

### 7.1 创建订单

```java
@PostMapping("/sessions/{sessionId}/orders")
public Result<DiningOrderVO> createOrder(
        @PathVariable Long sessionId,
        @RequestHeader("X-Submit-Token") String submitToken,
        @Valid @RequestBody DiningOrderCreateDTO diningOrderCreateDTO) {

    LoginInfo loginInfo = LoginContext.get();

    submitTokenService.validateAndConsume(
            loginInfo.getLoginType(),
            loginInfo.getUserId(),
            SubmitSceneEnum.CREATE_ORDER,
            submitToken);

    return Result.success(diningOrderService.createOrder(sessionId, diningOrderCreateDTO));
}
```

请求示例：

```http
POST /api/user/sessions/1/orders
Authorization: Bearer xxx
X-Submit-Token: 8f2d9a...
Content-Type: application/json
```

### 7.2 创建预约

```java
@PostMapping("/reservations")
public Result<ReservationVO> createReservation(
        @RequestHeader("X-Submit-Token") String submitToken,
        @Valid @RequestBody ReservationCreateDTO reservationCreateDTO) {

    LoginInfo loginInfo = LoginContext.get();

    submitTokenService.validateAndConsume(
            loginInfo.getLoginType(),
            loginInfo.getUserId(),
            SubmitSceneEnum.CREATE_RESERVATION,
            submitToken);

    return Result.success(reservationService.createReservation(reservationCreateDTO));
}
```

## 8. 放在 Controller 还是 Service

当前项目建议先放在 Controller 层显式调用。

原因：

- 防重复提交 Token 属于请求入口级校验。
- Controller 能直接读取请求头 `X-Submit-Token`。
- 对初期学习更直观，方便接口测试和断点调试。
- 不影响 Service 原有业务状态校验和事务逻辑。

后续如果大量接口都要接入，可以考虑升级为：

```text
自定义注解 + 拦截器 / AOP
```

例如：

```java
@SubmitLimit(scene = SubmitSceneEnum.CREATE_ORDER)
```

但 V2 阶段不建议一开始就抽象到注解层。先用手动方式跑通 1-2 个关键接口，再判断是否值得抽象。

## 9. 和幂等、锁、数据库约束的关系

防重复提交 Token 主要解决“同一个用户同一次操作重复提交”的问题。

它能解决：

- 用户连续点击提交按钮。
- 同一个 token 被重复使用。
- 前端重试同一份提交请求。

它不能替代：

- 数据库唯一约束。
- 条件更新。
- 乐观锁 / 悲观锁。
- Redis 分布式锁。
- Service 层业务状态校验。

当前项目的理解方式：

```text
Token：挡在请求入口，减少重复提交进入业务层
Service 状态校验：保证业务规则正确
条件更新：处理状态流转并发
唯一约束：数据库层兜底业务唯一性
```

因此，创建预约、创建订单这类接口即使加了防重复提交 Token，也仍然需要保留已有的条件更新、唯一约束和状态校验。

## 10. 常见问题

### 10.1 token 过期了怎么办

拒绝提交，并提示用户重新获取提交令牌。

前端可以在进入页面时获取 token，也可以在用户点击提交前临时获取 token。当前项目为了便于接口测试，可以先采用手动获取 token 的方式。

### 10.2 token 应该放请求头还是请求体

推荐放请求头：

```text
X-Submit-Token: xxx
```

原因是它不是业务表单字段，而是请求控制信息，和 `Authorization` token 类似，都属于请求级元数据。

### 10.3 一个页面能生成多个 token 吗

可以。

因为当前推荐方案把 token 放进 Redis key 中：

```text
foodflow:submit-token:user:12:create-order:{token}
```

同一用户、同一场景可以同时存在多个 token。每个 token 只能消费一次。

### 10.4 为什么不用固定 key 覆盖旧 token

固定 key 设计类似：

```text
foodflow:submit-token:user:12:create-order -> token
```

这样同一用户同一场景只能保留一个 token。用户打开多个页面或重复获取 token 时，旧 token 会被覆盖，容易造成页面 A 的 token 因页面 B 刷新而失效。

当前项目优先选择“token 参与 key”的方案，减少这种页面互相影响。

### 10.5 是否需要 Lua 脚本

当前推荐方案不强制需要。

因为校验消费只依赖一条 Redis `DEL` 命令，单命令具备原子性。

如果后续改成“固定 key + token value”的设计，或校验逻辑需要同时判断多个条件，再考虑 Lua 脚本。

## 11. 后续项目复用清单

引入 Redis 防重复提交 Token 时，可以按以下顺序落地：

1. 找出会产生副作用的写接口。
2. 定义提交场景枚举，例如创建预约、创建订单、清台。
3. 设计 Redis key，绑定登录类型、登录 ID、业务场景和 token。
4. 提供生成 token 接口。
5. 前端提交业务请求时携带 `X-Submit-Token`。
6. 后端在业务执行前调用 `validateAndConsume`。
7. 使用 Redis `DEL` 一次性消费 token。
8. token TTL 设置为短时间，例如 5 分钟。
9. 保留数据库约束、条件更新和业务状态校验。
10. 当多个接口都接入后，再评估是否抽取注解或 AOP。

