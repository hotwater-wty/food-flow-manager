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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "用户端-防重复提交令牌", description = "用户端写操作前获取一次性防重复提交令牌")
public class SubmitTokenController {

    private final SubmitTokenService submitTokenService;

    /**
     * 生成用户防重复提交令牌
     * 
     * @param submitTokenDTO 防重复提交令牌请求参数
     * @return 防重复提交令牌响应数据
     */
    @Operation(
            summary = "生成用户防重复提交令牌",
            description = "根据提交场景生成一次性防重复提交令牌，后续创建预约、创建订单等写操作需通过 X-Submit-Token 请求头携带该令牌")
    @PostMapping("/api/user/submit-token")
    public Result<SubmitTokenVO> generateUserSubmitToken(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "防重复提交令牌生成参数",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{\"scene\":\"create-reservation\"}")))
            SubmitTokenDTO submitTokenDTO) {
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