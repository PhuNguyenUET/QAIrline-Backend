package com.qairline.qairline_backend.mail.service.mail_template.impl;

import com.qairline.qairline_backend.mail.model.MailType;
import com.qairline.qairline_backend.mail.service.mail_template.MailTemplateService;
import com.qairline.qairline_backend.util.FileUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MailTemplateServiceImpl implements MailTemplateService {
    private final Map<MailType, String> templates = new HashMap<>();

    @PostConstruct
    private void init() {
        loadTemplate(MailType.RESET_PASSWORD, "reset-password.txt");
        loadTemplate(MailType.INVITE_MEMBER, "invite-member.txt");
        loadTemplate(MailType.CONFIRM_EMAIL, "confirm-email.txt");
        loadTemplate(MailType.REFUND, "refund.txt");
    }

    @Override
    public void loadTemplate(MailType mailType, String fileName) {
        try {
            String templatePath = System.getProperty("user.dir") + File.separator + "mail_template" + File.separator + fileName;

            templates.put(mailType, FileUtil.readFromFile(templatePath));
        } catch (Exception e) {
            log.error("Error loading mail template", e);
        }
    }

    @Override
    public String getTemplate(MailType mailType) {
        return templates.get(mailType);
    }
}
