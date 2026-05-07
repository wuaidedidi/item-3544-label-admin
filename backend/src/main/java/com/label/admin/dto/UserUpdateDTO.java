package com.label.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdateDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String nickname;

    private String email;

    private String phone;

    @NotNull(message = "角色不能为空")
    private Long roleId;

    private Integer status;
}
