package com.qairline.qairline_backend.client.admin.service.impl;

import com.qairline.qairline_backend.client.admin.model.Admin;
import com.qairline.qairline_backend.client.admin.repository.AdminRepository;
import com.qairline.qairline_backend.client.admin.service.AdminService;
import com.qairline.qairline_backend.client.dto.ChangePasswordRequest;
import com.qairline.qairline_backend.client.dto.CreateNewPasswordRequest;
import com.qairline.qairline_backend.client.model.Client;
import com.qairline.qairline_backend.client.model.CustomUserDetails;
import com.qairline.qairline_backend.client.model.Role;
import com.qairline.qairline_backend.client.admin.dto.AdminInviteDTO;
import com.qairline.qairline_backend.client.user.model.User;
import com.qairline.qairline_backend.client.user.repository.UserRepository;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.mail.model.MailType;
import com.qairline.qairline_backend.mail.service.mail_template.MailTemplateService;
import com.qairline.qairline_backend.mail.service.send_mail.SendMailService;
import com.qairline.qairline_backend.util.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;

    private final MailTemplateService mailTemplateService;
    private final SendMailService sendMailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${app.admin-new-password-url}")
    private String newPasswordBaseUrl;

    @Override
    public void inviteAdmin(AdminInviteDTO dto) {
        String role = dto.getRole();
        String password = dto.getEmail() + RandomUtils.generateRandomString(12);
        String username = dto.getEmail();
        adminRepository.findByUsername(dto.getEmail()).ifPresent(admin -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Admin already existed");
        });
        Admin requestAdmin = getCurrentAdmin();
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setEmail(dto.getEmail());
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setRole(requestAdmin.getRole().equals(Role.ADMIN.toString()) ? role : Role.ADMIN.toString());
        admin.setPassword(bCryptPasswordEncoder.encode(password));
        admin.setActive(true);
        admin = adminRepository.save(admin);
        String token = admin.getId() + "-" + UUID.randomUUID() + "-" + System.currentTimeMillis();
        admin.setResetPasswordToken(token);
        admin.setResetPasswordTokenExpire(System.currentTimeMillis() + 24 * 60 * 1000);
        adminRepository.save(admin);
        sendInviteMemberMail(dto.getEmail(), token);
    }

    @Override
    public Admin getAdminByUsername(String username) {
        return adminRepository.findByUsername(username).orElse(null);
    }

    @Override
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Override
    public Admin getCurrentAdmin() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Client client = customUserDetails.getClient();

        Admin admin = null;

        if (client instanceof Admin) {
            admin = (Admin) client;
        }
        return admin;
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Admin admin = getCurrentAdmin();
        if (!bCryptPasswordEncoder.matches(changePasswordRequest.getOldPassword(), admin.getPassword())) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Wrong current password")
                    .build();
        } else {
            admin.setPassword(bCryptPasswordEncoder.encode(changePasswordRequest.getNewPassword()));
            adminRepository.save(admin);
        }
    }

    @Override
    public void sendResetPasswordEmail(String email) {
        Optional<Admin> a = adminRepository.findByEmail(email);
        if (a.isEmpty()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Account does not exist!")
                    .build();
        }
        Admin admin = a.get();
        String token = admin.getId() + "-" + UUID.randomUUID() + "-" + System.currentTimeMillis();
        admin.setResetPasswordToken(token);
        admin.setResetPasswordTokenExpire(System.currentTimeMillis() + 15 * 60 * 1000);
        adminRepository.save(admin);
        sendMail(email, token);
    }

    static final String INVITE_MEMBER_SUBJECT = "You was invited to QAirline Management as an admin";

    static final String RESET_PASSWORD_SUBJECT = "QAirline - Reset Password";

    private void sendMail(String email, String token) {
        String resetPasswordUrl = newPasswordBaseUrl + "?token=" + token;
        String template = mailTemplateService.getTemplate(MailType.RESET_PASSWORD);
        String content = template.replace("{RESET_PASSWORD_URL}", resetPasswordUrl);
        sendMailService.addToQueue(email, RESET_PASSWORD_SUBJECT, content);
    }

    private void sendInviteMemberMail(String email, String token) {
        String resetPasswordUrl = newPasswordBaseUrl + "?token=" + token;
        String template = mailTemplateService.getTemplate(MailType.INVITE_MEMBER);
        String content = template.replace("{NEW_PASSWORD_URL}", resetPasswordUrl);
        sendMailService.addToQueue(email, INVITE_MEMBER_SUBJECT, content);
    }

    @Override
    public void unlockAdmin(String email) {
        Optional<Admin> a = adminRepository.findByEmail(email);
        if (a.isEmpty()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Admin not found")
                    .build();
        }
        Admin admin = a.get();
        admin.setActive(true);
        adminRepository.save(admin);
    }

    @Override
    public void lockAdmin(String email) {
        Optional<Admin> a = adminRepository.findByEmail(email);
        if (a.isEmpty()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Admin not found")
                    .build();
        }
        Admin admin = a.get();
        admin.setActive(false);
        adminRepository.save(admin);
    }

    @Override
    public void unlockUser(String username) {
        Optional<User> u = userRepository.findByUsername(username);
        if (u.isEmpty()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User not found")
                    .build();
        }
        User user = u.get();
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    public void lockUser(String username) {
        Optional<User> u = userRepository.findByUsername(username);
        if (u.isEmpty()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User not found")
                    .build();
        }
        User user = u.get();
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void createNewPassword(CreateNewPasswordRequest request) {
        String token = request.getToken();
        String password = request.getPassword();

        String userId = extractUserId(token);
        if (userId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
        Admin admin = adminRepository.findById(userId).orElse(null);
        if (admin == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
        if (!StringUtils.equals(admin.getResetPasswordToken(), token)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
        if (System.currentTimeMillis() > admin.getResetPasswordTokenExpire()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token has expired.");
        }
        admin.setPassword(bCryptPasswordEncoder.encode(password));
        admin.setResetPasswordToken(null);
        admin.setResetPasswordTokenExpire(-1);
        adminRepository.save(admin);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        return new CustomUserDetails(admin, true, true, true, true);
    }

    private static String extractUserId(String token) {
        return StringUtils.substring(token, 0, StringUtils.indexOf(token, "-"));
    }
}
