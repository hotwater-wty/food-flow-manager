package com.foodflow.common.result;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResultTest {
    @Test
    void successWithoutDataRemainsSuccessful() {
        Result<Void> result = Result.success();
        assertThat(result.getCode()).isEqualTo(1);
        assertThat(result.getData()).isNull();
        assertThat(result.getErrorCode()).isNull();
    }

    @Test
    void errorCarriesStableCode() {
        Result<Void> result = Result.error("TABLE_IN_USE", "桌位已占用");
        assertThat(result.getCode()).isZero();
        assertThat(result.getErrorCode()).isEqualTo("TABLE_IN_USE");
        assertThat(result.getMsg()).isEqualTo("桌位已占用");
    }
}
