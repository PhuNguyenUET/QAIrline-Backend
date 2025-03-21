package com.qairline.qairline_backend.mail.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMailTask {
    private String to;
    private String subject;
    private String body;
}
