package com.campus.registrationservice.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 报名数据传输对象
 */
@Data
public class RegistrationDTO {
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String username;

    private String realName;

    private String studentId;

    private String email;

    private String phone;
}