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

    public void validateAndConsume(LoginTypeEnum loginType, Long loginId, SubmitSceneEnum scene, String token) {
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

    private String buildKey(LoginTypeEnum loginType, Long loginId, SubmitSceneEnum scene, String token) {
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
