package com.foodflow.common.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.foodflow.common.context.LoginContext;
import com.foodflow.common.context.LoginInfo;
import com.foodflow.common.dto.SubmitTokenDTO;
import com.foodflow.common.enums.SubmitSceneEnum;
import com.foodflow.common.result.Result;
import com.foodflow.common.service.SubmitTokenService;
import com.foodflow.common.vo.SubmitTokenVO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SubmitTokenController {

    private final SubmitTokenService submitTokenService;

    @PostMapping("/api/user/submit-token")
    public Result<SubmitTokenVO> generateUserSubmitToken(@Valid @RequestBody SubmitTokenDTO submitTokenDTO) {
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