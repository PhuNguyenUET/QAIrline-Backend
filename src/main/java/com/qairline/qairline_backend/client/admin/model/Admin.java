package com.qairline.qairline_backend.client.admin.model;

import com.qairline.qairline_backend.client.model.Client;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document("z_admin")
public class Admin extends Client {
    @Id
    private String id;

    private String firstName;
    private String lastName;

    private String resetPasswordToken;
    private long resetPasswordTokenExpire;
}