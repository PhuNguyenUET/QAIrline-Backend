package com.qairline.qairline_backend.client.user.model;

import com.qairline.qairline_backend.client.model.Client;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@Document("z_user")
public class User extends Client {
    @Id
    private String id;

    private String firstName;
    private String lastName;

    private String address;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dateOfBirth;

    private int failedAttempt;

    private String resetPasswordToken;
    private long resetPasswordTokenExpire;

    private boolean isEmailValidated;
    private String confirmEmailToken;
    private long confirmEmailTokenExpire;
}
