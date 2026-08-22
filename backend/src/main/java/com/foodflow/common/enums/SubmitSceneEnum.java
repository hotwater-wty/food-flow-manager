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