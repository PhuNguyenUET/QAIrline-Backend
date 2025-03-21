package com.qairline.qairline_backend.client.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminInviteDTO {
    private String role;
    private String email;

    private String lastName;
    private String firstName;
}
