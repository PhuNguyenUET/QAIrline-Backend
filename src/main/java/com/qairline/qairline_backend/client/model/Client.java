package com.qairline.qairline_backend.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.mongodb.core.index.Indexed;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    private String email;

    @Indexed(background = true)
    private String username;
    private String password;
    private String phone;
    private String role;

    private boolean active;
}
