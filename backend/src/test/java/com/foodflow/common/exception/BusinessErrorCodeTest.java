package com.foodflow.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class BusinessErrorCodeTest {

    @Test
    void registryContainsUniqueCodesWithRequiredMetadata() {
        assertThat(Arrays.stream(BusinessErrorCode.values())
                .map(BusinessErrorCode::getCode)
                .distinct()
                .count()).isEqualTo(BusinessErrorCode.values().length);
        assertThat(Arrays.stream(BusinessErrorCode.values())
                .allMatch(code -> !code.getDefaultMessage().isBlank()
                        && !code.getDomain().isBlank()
                        && code.getHttpStatus() >= 400))
                .isTrue();
    }

    @Test
    void exceptionConstructorUsesRegistryCodeAndMessage() {
        BusinessException exception = new BusinessException(BusinessErrorCode.TABLE_IN_USE);

        assertThat(exception.getErrorCode()).isEqualTo("TABLE_IN_USE");
        assertThat(exception.getMessage()).isEqualTo("桌位已被占用");
    }
}
