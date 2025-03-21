package com.qairline.qairline_backend.mail.service.send_mail;

public interface SendMailService {
    void addToQueue(String to, String subject, String body);

}
