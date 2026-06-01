# 用户提问

数据库中使用数字表示状态，比如 1男，2女，而在Java里可以用枚举类表示，当前代码中，实体类对象是直接用integer类型表示状态值，用枚举类直接getCode转数字存到对象里，然后存入数据库，而这时因为都是getCode存的数字，所以很可能用错枚举类，增加维护成本，那么问题是，如果实体类的状态数据类型直接使用枚举类型，数据在序列化转出Java时能否自动变成int类型？

# 系统回答

## MyBatis-Plus 枚举类型自动转换

### 核心答案

**可以自动转换**，但需要配置 MyBatis-Plus 的枚举类型处理器。

### 实现方式

#### 1. 实体类直接使用枚举类型

```java
@Data
@TableName("reservation")
public class Reservation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 直接使用枚举类型，而不是 Integer
    private ReservationStatusEnum status;
    
    private LocalDateTime createTime;
}
```

#### 2. 配置 MyBatis-Plus 枚举处理器

在 `application.yaml` 中添加配置：

```yaml
mybatis-plus:
  type-enums-package: com.foodflow.common.enums  # 枚举类所在包
```

### 工作原理

| 方向 | 转换过程 |
|------|----------|
| **Java → 数据库** | `ReservationStatusEnum.WAITING_CHECK_IN` → `0` |
| **数据库 → Java** | `0` → `ReservationStatusEnum.WAITING_CHECK_IN` |

MyBatis-Plus 会自动调用枚举的 `getCode()` 方法将枚举转换为数字存入数据库，读取时自动将数字转换回枚举对象。

### 优势

| 优势 | 说明 |
|------|------|
| **类型安全** | 编译期就能发现枚举使用错误 |
| **代码简洁** | 无需手动调用 `getCode()` 和 `ofCode()` |
| **自动转换** | 框架层面处理，业务代码无感知 |
| **IDE 提示** | 自动补全枚举值，减少拼写错误 |

### 注意事项

1. **枚举必须实现 `IEnum<Integer>` 接口**（MyBatis-Plus 要求）

```java
@Getter
public enum ReservationStatusEnum implements IEnum<Integer> {
    WAITING_CHECK_IN(0, "待到店"),
    CHECKED_IN(1, "已到店"),
    CANCELED(2, "已取消"),
    TIMEOUT(3, "已超时");
    
    private final Integer code;
    private final String description;
    
    // 实现 IEnum 接口的方法
    @Override
    public Integer getValue() {
        return this.code;
    }
}
```

2. **JSON 序列化问题**：如果实体类需要返回给前端，枚举会被序列化为对象，需要额外配置：

```java
// 方案1：使用 @JsonValue 注解
@Getter
public enum ReservationStatusEnum implements IEnum<Integer> {
    @JsonValue  // 序列化时只输出 code 值
    WAITING_CHECK_IN(0, "待到店");
    
    private final Integer code;
}

// 方案2：使用 DTO/VO 转换
public class ReservationVO {
    private Integer status;  // 返回数字给前端
    private String statusDesc;  // 返回描述给前端
}
```

### 总结

| 场景 | 推荐方案 |
|------|----------|
| **数据库存储** | 实体类直接用枚举类型 + MyBatis-Plus 自动转换 |
| **前端交互** | 使用 VO 对象，将枚举转换为数字或字符串 |
| **类型安全** | 编译期检查，避免枚举混用 |

这样既保证了数据库存储的是数字，又保证了 Java 代码中的类型安全，一举两得。