package com.foodflow.common.result;

import com.foodflow.common.enums.CacheStatusEnum;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "缓存命中结果")
public class CacheResult<T> {

    @Schema(description = "缓存状态，HIT-命中，MISS-未命中，EMPTY-空值")
    private final CacheStatusEnum status;

    @Schema(description = "缓存数据")
    private final T data;

    private CacheResult(CacheStatusEnum status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> CacheResult<T> hit(T data) {
        return new CacheResult<>(CacheStatusEnum.HIT, data);
    }

    public static <T> CacheResult<T> miss() {
        return new CacheResult<>(CacheStatusEnum.MISS, null);
    }

    public static <T> CacheResult<T> empty() {
        return new CacheResult<>(CacheStatusEnum.EMPTY, null);
    }

    public boolean isHit() {
        return status == CacheStatusEnum.HIT;
    }

    public boolean isMiss() {
        return status == CacheStatusEnum.MISS;
    }

    public boolean isEmpty() {
        return status == CacheStatusEnum.EMPTY;
    }

    public T getData() {
        return data;
    }
}