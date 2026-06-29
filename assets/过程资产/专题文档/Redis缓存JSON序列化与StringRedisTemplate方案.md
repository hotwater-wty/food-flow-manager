# Redis 缓存 JSON 序列化与 StringRedisTemplate 方案

## 1. 解决什么问题

本文记录当前项目在 V2 阶段引入 Redis 缓存时形成的一套基础方案。

当前项目首先选择缓存用户端启售菜品列表：

```text
GET /api/user/dishes
GET /api/user/dishes?categoryId=1
```

这个接口具有典型缓存特征：

- 用户端查询频率高。
- 菜品数据读多写少。
- 查询条件简单，主要是全部启售菜品或按分类查询启售菜品。
- 管理端新增、修改、删除、上下架菜品时可以明确删除缓存。

开发过程中曾尝试使用 `RedisTemplate<String, Object>` 配合 `GenericJackson2JsonRedisSerializer` 直接缓存 `List<DishVO>`，写入 Redis 后能看到 JSON 数据，但第二次读取缓存时报错：

```text
Could not read JSON:
Unexpected token (START_OBJECT), expected VALUE_STRING:
need String, Number of Boolean value that contains type id
```

根因是缓存值属于泛型集合：

```java
List<DishVO>
```

`RedisTemplate<String, Object>` 在读取时只知道目标类型是 `Object`，无法稳定恢复 `List<DishVO>` 这种带泛型的复杂结构。

需要注意：即使业务代码中把返回值强转为 `List<DishVO>`，也不能解决这个问题。

```java
List<DishVO> cached = (List<DishVO>) redisTemplate.opsForValue().get(cacheKey);
```

实际流程是：

```text
RedisTemplate 从 Redis 取出 byte[]
  -> 调用 valueSerializer.deserialize(byte[])
  -> 序列化器按 Object 反序列化
  -> 返回给业务代码
  -> 业务代码再执行强转
```

也就是说，强转发生在反序列化之后，并没有把 `List<DishVO>` 这个目标类型传给序列化器。

因此当前项目采用更明确、更可控的方案：

```text
StringRedisTemplate + ObjectMapper
```

即 Redis 中只存普通 JSON 字符串，Java 中通过 `ObjectMapper` 显式指定目标类型完成序列化和反序列化。

## 2. 当前项目如何使用

当前项目的启售菜品缓存采用旁路缓存模式：

```text
查询 Redis
  -> 命中：JSON 字符串反序列化为 List<DishVO> 并返回
  -> 未命中：查询 MySQL
  -> 将查询结果转为 JSON 字符串写入 Redis
  -> 返回结果
```

缓存 key 采用业务前缀命名：

```text
foodflow:dish:on-sale:all
foodflow:dish:on-sale:category:{categoryId}
```

示例：

```text
foodflow:dish:on-sale:all
foodflow:dish:on-sale:category:1
```

菜品发生变化后，需要删除相关缓存：

- 新增菜品。
- 修改菜品。
- 删除菜品。
- 修改菜品状态，例如启售、停售、售罄。

当前 V2 阶段采用简单可靠的缓存失效策略：

```text
先更新数据库，再删除相关缓存。
```

## 3. 核心配置或代码示例

### 3.1 RedisTemplate 通用配置

当前项目保留了 `RedisTemplate<String, Object>` 的通用配置，用于简单对象或简单值缓存。

```java
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer jsonRedisSerializer =
                new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
```

但需要注意：这套配置不适合作为复杂泛型集合缓存的首选方案。

### 3.2 StringRedisTemplate + ObjectMapper 缓存列表

推荐在复杂对象列表、分页结果、泛型集合缓存中使用：

```java
private final StringRedisTemplate stringRedisTemplate;
private final ObjectMapper objectMapper;
```

构造缓存 key：

```java
private static final String DISH_ON_SALE_ALL_KEY = "foodflow:dish:on-sale:all";
private static final String DISH_ON_SALE_CATEGORY_PREFIX = "foodflow:dish:on-sale:category:";

private String buildOnSaleDishCacheKey(Long categoryId) {
    if (categoryId == null) {
        return DISH_ON_SALE_ALL_KEY;
    }
    return DISH_ON_SALE_CATEGORY_PREFIX + categoryId;
}
```

读取缓存：

```java
String cachedJson = stringRedisTemplate.opsForValue().get(cacheKey);
if (cachedJson != null) {
    try {
        return objectMapper.readValue(
                cachedJson,
                new TypeReference<List<DishVO>>() {}
        );
    } catch (JsonProcessingException e) {
        throw new BusinessException("菜品缓存解析失败");
    }
}
```

写入缓存：

```java
try {
    String json = objectMapper.writeValueAsString(result);
    stringRedisTemplate.opsForValue().set(cacheKey, json, 10, TimeUnit.MINUTES);
} catch (JsonProcessingException e) {
    throw new BusinessException("菜品缓存写入失败");
}
```

清理缓存：

```java
private void cleanDishCache() {
    stringRedisTemplate.delete(DISH_ON_SALE_ALL_KEY);

    Set<String> keys = stringRedisTemplate.keys(DISH_ON_SALE_CATEGORY_PREFIX + "*");
    if (keys != null && !keys.isEmpty()) {
        stringRedisTemplate.delete(keys);
    }
}
```

说明：`keys` 在数据量很大时不适合在线上高频使用。当前项目 V2 阶段数据量小，可以作为学习与验证方案。后续可改为维护分类 key 集合，或按确定条件删除指定 key。

## 4. 两类序列化方案的边界

### 4.1 GenericJackson2JsonRedisSerializer 的定位

`GenericJackson2JsonRedisSerializer` 是挂在 `RedisTemplate<String, Object>` 上的 value 序列化器。

当调用：

```java
redisTemplate.opsForValue().set(key, value);
```

它负责把 Java 对象序列化为 JSON。

当调用：

```java
redisTemplate.opsForValue().get(key);
```

它负责把 Redis 中的 JSON 反序列化为 Java 对象。

适合场景：

- 简单字符串、数字、布尔值。
- 简单业务对象。
- 简单 Map。
- 不包含复杂泛型嵌套的对象。

示例：

```java
redisTemplate.opsForValue().set("foodflow:user:enabled:1", true);
redisTemplate.opsForValue().set("foodflow:dish:detail:1", dishVO);
```

不推荐场景：

```java
List<DishVO>
PageResult<DishVO>
Map<Long, List<DishVO>>
```

这些结构包含泛型信息。读取时如果只以 `Object` 接收，反序列化器不一定知道集合元素应该恢复成什么类型。

更准确地说，问题不只是“Java 泛型会擦除”，而是 `RedisTemplate.opsForValue().get()` 这条自动反序列化链路没有把 `List<DishVO>` 这样的目标泛型类型传给序列化器。业务代码里的强转发生得太晚，无法影响前面的 JSON 反序列化过程。

### 4.2 ObjectMapper 的定位

`ObjectMapper` 是 Jackson 的核心 JSON 工具。

在当前方案中，它不依赖 `RedisTemplate` 的 value 序列化器自动推断类型，而是在业务代码中明确完成转换：

```java
List<DishVO> -> JSON 字符串
JSON 字符串 -> List<DishVO>
```

关键点在于：

```java
new TypeReference<List<DishVO>>() {}
```

它明确告诉 Jackson 目标类型是 `List<DishVO>`，避免泛型信息丢失。

因此当前项目形成如下约定：

```text
简单值或简单对象：RedisTemplate<String, Object>
复杂对象列表或泛型结果：StringRedisTemplate + ObjectMapper + TypeReference
```

## 5. Hash 类型存储对象数据

Redis 中除了 String 类型，也可以使用 Hash 类型存储对象。

String 类型更像：

```text
key -> 整个 JSON 对象
```

Hash 类型更像：

```text
key -> field -> value
```

例如缓存菜品详情：

```text
foodflow:dish:detail:1
  id          -> 1
  categoryId  -> 1
  name        -> 宫保鸡丁
  price       -> 3800
  status      -> 1
```

### 5.1 适合使用 Hash 的场景

Hash 适合：

- 缓存单个对象。
- 希望按字段查看或修改对象数据。
- 对象字段较稳定。
- 不需要缓存复杂嵌套结构。

不太适合：

- `List<DishVO>` 这种列表。
- 分页结果。
- 嵌套对象较多的结构。
- 需要整体反序列化为复杂泛型的结构。

### 5.2 使用 StringRedisTemplate 存 Hash

如果希望 Redis 中保持可读性，可以继续使用 `StringRedisTemplate`，将字段值都转为字符串：

```java
String key = "foodflow:dish:detail:" + dishVO.getId();

Map<String, String> map = new HashMap<>();
map.put("id", String.valueOf(dishVO.getId()));
map.put("categoryId", String.valueOf(dishVO.getCategoryId()));
map.put("name", dishVO.getName());
map.put("price", String.valueOf(dishVO.getPrice()));
map.put("image", dishVO.getImage());
map.put("description", dishVO.getDescription());
map.put("status", String.valueOf(dishVO.getStatus()));

stringRedisTemplate.opsForHash().putAll(key, map);
```

读取时需要手动转换字段类型：

```java
Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
if (entries.isEmpty()) {
    return null;
}

DishVO dishVO = DishVO.builder()
        .id(Long.valueOf((String) entries.get("id")))
        .categoryId(Long.valueOf((String) entries.get("categoryId")))
        .name((String) entries.get("name"))
        .price(Integer.valueOf((String) entries.get("price")))
        .image((String) entries.get("image"))
        .description((String) entries.get("description"))
        .status(Integer.valueOf((String) entries.get("status")))
        .build();
```

优点：

- Redis 中字段清晰可读。
- 可以单独更新某个字段。
- 不依赖复杂反序列化。

缺点：

- 类型转换代码较多。
- 字段变更时维护成本较高。
- 不适合复杂对象和列表。

### 5.3 使用 RedisTemplate 存 Hash

也可以使用当前配置好的 `RedisTemplate<String, Object>`：

```java
String key = "foodflow:dish:detail:" + dishVO.getId();

redisTemplate.opsForHash().put(key, "id", dishVO.getId());
redisTemplate.opsForHash().put(key, "name", dishVO.getName());
redisTemplate.opsForHash().put(key, "price", dishVO.getPrice());
redisTemplate.opsForHash().put(key, "status", dishVO.getStatus());
```

这种方式可以保留数字类型，但仍要注意 value 序列化和反序列化的一致性。

当前项目如果只是做启售菜品列表缓存，不需要使用 Hash；继续使用 String JSON 更合适。

## 6. 推荐抽取 CacheClient

当前业务代码中直接使用：

```java
StringRedisTemplate
ObjectMapper
try-catch
TypeReference
```

能跑通，也方便学习。但如果后续多个模块都要缓存复杂对象，会产生重复代码。

后续可以抽取一个缓存工具类，例如：

```java
@Component
@RequiredArgsConstructor
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            stringRedisTemplate.opsForValue().set(key, json, timeout, unit);
        } catch (JsonProcessingException e) {
            throw new BusinessException("缓存写入失败");
        }
    }

    public <T> T get(String key, TypeReference<T> typeReference) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new BusinessException("缓存解析失败");
        }
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
```

业务代码可简化为：

```java
List<DishVO> cached = cacheClient.get(
        cacheKey,
        new TypeReference<List<DishVO>>() {}
);
if (cached != null) {
    return cached;
}

List<DishVO> result = queryFromDatabase();
cacheClient.set(cacheKey, result, 10, TimeUnit.MINUTES);
return result;
```

抽取时机：

- 第二个以上模块开始使用 `StringRedisTemplate + ObjectMapper`。
- 多处出现相同的 JSON 读写 `try-catch`。
- 需要统一处理缓存解析失败、缓存写入失败、TTL、空值缓存等策略。

当前项目可以先保持业务代码直写，等 Redis 使用场景增加后再抽取。

## 7. 常见问题

### 7.1 为什么 Redis 中已经有 JSON，读取还会报错

因为 Redis 中有 JSON 只说明写入成功，不代表 Java 能按预期类型读回来。

读取时还需要满足：

- 使用相同或兼容的序列化方式。
- Java 端明确知道目标类型。
- 泛型集合需要使用 `TypeReference` 之类的方式保留类型信息。

### 7.2 为什么不用 GenericJackson2JsonRedisSerializer 缓存 List

不是完全不能用，而是对初学和当前项目来说不够直观。

它依赖类型元数据和自动反序列化，遇到复杂泛型时排错成本较高。当前项目更看重可读、可控、容易复用，所以优先使用 `StringRedisTemplate + ObjectMapper`。

### 7.3 ObjectMapper 和 Redis 序列化器是不是重复了

作用相似，都是 Java 对象和 JSON 之间的转换工具，但使用位置不同：

- `GenericJackson2JsonRedisSerializer` 挂在 `RedisTemplate` 上，自动参与读写。
- `ObjectMapper` 在业务代码或工具类中手动调用，显式控制目标类型。

当前复杂缓存场景选择后者，是为了避免泛型类型不明确。

### 7.4 缓存解析失败应该怎么办

当前项目直接抛业务异常，便于开发阶段暴露问题。

后续更稳妥的方式可以是：

```text
记录错误日志
删除坏缓存
回源数据库
重新写入缓存
```

这样即使缓存中有旧格式或脏数据，也不会影响主流程查询。

## 8. 后续项目复用清单

引入 Redis 缓存复杂对象时，按以下顺序判断：

1. 缓存的数据是简单值、简单对象，还是泛型集合。
2. 简单对象可使用 `RedisTemplate<String, Object>`。
3. `List<T>`、`PageResult<T>`、复杂嵌套结构优先使用 `StringRedisTemplate + ObjectMapper`。
4. 反序列化泛型集合时使用 `TypeReference<T>`。
5. Redis key 按项目、模块、业务范围、条件命名。
6. 写缓存时设置 TTL。
7. 数据变更后删除相关缓存。
8. 多处重复使用后抽取 `CacheClient`。
9. 单个对象字段需要独立读写时，再考虑 Hash 类型。
10. 缓存只是性能优化，数据库仍然是业务真实数据来源。
