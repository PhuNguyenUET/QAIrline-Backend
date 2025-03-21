package com.qairline.qairline_backend.mail.service.mail_template;

import com.qairline.qairline_backend.mail.model.MailType;

public interface MailTemplateService {
    void loadTemplate(MailType mailType, String fileName);

    String getTemplate(MailType mailType);
}
