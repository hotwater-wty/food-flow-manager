# Knife4j/OpenAPI 使用方法对话记录

## 用户提问

怎么加注解才能在 Knife4j 中显示接口说明？

## 系统回答

原因基本就是：**你现在只接入了 Knife4j/OpenAPI 生成器，但 Controller、DTO、VO 上缺少 OpenAPI 注解，所以页面只能识别到路径和方法，缺少清晰的接口说明、参数说明、响应结构说明。**

你要用的是 `io.swagger.v3.oas.annotations` 这一套注解。

**常用注解**

Controller 类上：

```java
@Tag(name = "用户预约接口", description = "用户端预约创建、查询、取消")
```

Controller 方法上：

```java
@Operation(summary = "创建预约", description = "用户选择空闲桌位并创建预约")
```

DTO / VO 类上：

```java
@Schema(description = "创建预约请求参数")
```

字段上：

```java
@Schema(description = "桌位ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
```

**示例：Controller**

```java
@Tag(name = "用户预约接口")
@Slf4j
@RestController
@RequestMapping("/api/user/reservations")
@RequiredArgsConstructor
public class UserReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "创建预约")
    @PostMapping
    public Result<ReservationCreateVO> createReservation(
            @Validated @RequestBody ReservationDTO reservationDTO) {
        ReservationCreateVO reservationCreateVO = reservationService.createReservation(reservationDTO);
        return Result.success(reservationCreateVO);
    }

    @Operation(summary = "查看我的预约列表")
    @GetMapping
    public Result<List<ReservationVO>> getReservationList() {
        List<ReservationVO> reservationList = reservationService.getReservation();
        return Result.success(reservationList);
    }

    @Operation(summary = "查看我的预约详情")
    @GetMapping("/{reservationId}/detail")
    public Result<ReservationVO> getReservationDetail(
            @Parameter(description = "预约ID", required = true, example = "1")
            @PathVariable Long reservationId) {
        ReservationVO reservationVO = reservationService.getReservationDetail(reservationId);
        return Result.success(reservationVO);
    }

    @Operation(summary = "取消我的预约")
    @PostMapping("/{reservationId}/cancel")
    public Result<Void> cancelReservation(
            @Parameter(description = "预约ID", required = true, example = "1")
            @PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return Result.success();
    }
}
```

需要导入：

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
```

**示例：DTO**

```java
@Schema(description = "创建预约请求参数")
@Data
public class ReservationDTO {

    @Schema(description = "桌位ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "桌位ID不能为空")
    private Long tableId;

    @Schema(description = "预约人数", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "人数不能为空")
    @Min(value = 1, message = "人数必须大于等于1人")
    private Integer peopleCount;

    @Schema(description = "预约到店时间", example = "2026-06-01T18:30:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预约时间不能为空")
    @Future(message = "预约时间必须在当前时间之后")
    private LocalDateTime reserveTime;
}
```

需要导入：

```java
import io.swagger.v3.oas.annotations.media.Schema;
```

**示例：VO**

```java
@Schema(description = "预约信息")
@Data
@Builder
public class ReservationVO {

    @Schema(description = "预约ID", example = "1")
    private Long reservationId;

    @Schema(description = "预约编号", example = "20260531183000123")
    private String reservationNo;

    @Schema(description = "桌位ID", example = "1")
    private Long tableId;

    @Schema(description = "桌号", example = "A01")
    private String tableNo;

    @Schema(description = "预约人数", example = "4")
    private Integer peopleCount;

    @Schema(description = "预约到店时间", example = "2026-06-01T18:30:00")
    private LocalDateTime reserveTime;

    @Schema(description = "预约状态：0待到店，1已到店，2已取消，3已超时", example = "0")
    private Integer status;
}
```

**建议你现在怎么做**

先不要给全项目一次性补完，成本太高。按模块推进：

1. 先给 `UserReservationController`、`AdminReservationController` 补 `@Tag`、`@Operation`、`@Parameter`。
2. 给 `ReservationDTO`、`ReservationVO`、`ReservationCreateVO` 补 `@Schema`。
3. 重启项目，打开 `http://localhost:8080/doc.html` 检查显示效果。
4. 再到 Apifox 里通过 `http://localhost:8080/v3/api-docs` 重新导入。

后续每完成一个模块，就同步补这三类注解：

```text
Controller 类：@Tag
Controller 方法：@Operation / @Parameter
DTO / VO：@Schema
```

这样 Knife4j 页面和 Apifox 导入出来的接口，才会逐步接近“真正可用的接口文档”。