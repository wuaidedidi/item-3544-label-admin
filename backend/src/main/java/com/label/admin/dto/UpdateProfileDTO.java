package com.label.admin.dto;

import lombok.Data;

@Data
public class UpdateProfileDTO {

    private String nickname;

    private String email;

    private String phone;

    private String avatar;
}
