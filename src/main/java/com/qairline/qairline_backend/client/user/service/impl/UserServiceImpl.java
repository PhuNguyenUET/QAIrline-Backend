package com.qairline.qairline_backend.client.user.service.impl;


import com.qairline.qairline_backend.client.dto.ChangePasswordRequest;
import com.qairline.qairline_backend.client.dto.CreateNewPasswordRequest;
import com.qairline.qairline_backend.client.model.Client;
import com.qairline.qairline_backend.client.user.dto.*;
import com.qairline.qairline_backend.common.exception.BusinessException;
import com.qairline.qairline_backend.mail.model.MailType;
import com.qairline.qairline_backend.mail.service.mail_template.MailTemplateService;
import com.qairline.qairline_backend.mail.service.send_mail.SendMailService;
import com.qairline.qairline_backend.client.model.CustomUserDetails;
import com.qairline.qairline_backend.client.model.Role;
import com.qairline.qairline_backend.client.user.model.User;
import com.qairline.qairline_backend.client.user.repository.UserRepository;
import com.qairline.qairline_backend.client.user.service.UserService;
import com.qairline.qairline_backend.util.RandomUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MailTemplateService mailTemplateService;
    private final SendMailService sendMailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${app.user-new-password-url}")
    private String newPasswordBaseUrl;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user, true, true, true, true);
    }

    @Override
    public void register(UserRegisterDTO dto) {
        String password = dto.getPassword();
        String email = dto.getEmail();
        String username = dto.getUsername();
        userRepository.findByUsername(dto.getUsername()).ifPresent(user -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "User already existed");
        });
        User user = new User();
        user.setUsername(username);
        user.setRole(Role.USER.toString());
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setActive(true);
        userRepository.save(user);
    }


    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    static final String RESET_PASSWORD_SUBJECT = "QAirline - Reset Password";

    @Override
    public void sendResetPasswordEmail(String email) {
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isEmpty()) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User does not exist!")
                    .build();
        }
        User user = u.get();
        String token = user.getId() + "-" + UUID.randomUUID() + "-" + System.currentTimeMillis();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpire(System.currentTimeMillis() + 15 * 60 * 1000);
        userRepository.save(user);
        sendMail(email, token);
    }

    private void sendMail(String email, String token) {
        String resetPasswordUrl = newPasswordBaseUrl + "?token=" + token;
        String template = mailTemplateService.getTemplate(MailType.RESET_PASSWORD);
        String content = template.replace("{NEW_PASSWORD_URL}", resetPasswordUrl);
        sendMailService.addToQueue(email, RESET_PASSWORD_SUBJECT, content);
    }

    private void sendEmailConfirmation(String email, String token) {
        String template = mailTemplateService.getTemplate(MailType.CONFIRM_EMAIL);
        String content = template.replace("{EMAIL_CODE}", token);
        sendMailService.addToQueue(email, CONFIRM_EMAIL_SUBJECT, content);
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = getCurrentUser();
        if (!bCryptPasswordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw BusinessException.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Wrong current password")
                    .build();
        } else {
            user.setPassword(bCryptPasswordEncoder.encode(changePasswordRequest.getNewPassword()));
            userRepository.save(user);
        }
    }

    @Override
    public void changeEmail(String newEmail) {
        User user = getCurrentUser();
        user.setEmail(newEmail);
        user.setEmailValidated(false);
        user.setConfirmEmailToken(null);
        user.setConfirmEmailTokenExpire(0);
        userRepository.save(user);
    }

    static final String CONFIRM_EMAIL_SUBJECT = "QAirline - Confirm Email";

    @Override
    public void sendConfirmEmail() {
        User user = getCurrentUser();
        String email = user.getEmail();
        String token = RandomUtils.generateRandomString(6);
        user.setConfirmEmailToken(token);
        user.setConfirmEmailTokenExpire(System.currentTimeMillis() + 30 * 60 * 1000);
        userRepository.save(user);
        sendEmailConfirmation(email, token);
    }

    @Override
    public void confirmEmail(String token) {
        User user = getCurrentUser();
        if (!StringUtils.equals(user.getConfirmEmailToken(), token)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token does not exist!");
        }
        if (System.currentTimeMillis() > user.getConfirmEmailTokenExpire()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token has expired.");
        }

        user.setEmailValidated(true);
        userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User getCurrentUser() {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Client client = customUserDetails.getClient();

        User user = null;

        if(client instanceof User) {
            user = (User) client;
        }
        return user;
    }

    @Override
    public void editUser(UserEditDTO dto) throws ParseException {
        User user = getCurrentUser();

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());

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
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
        if (!StringUtils.equals(user.getResetPasswordToken(), token)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token doesn't exist!");
        }
        if (System.currentTimeMillis() > user.getResetPasswordTokenExpire()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Token has expired.");
        }

        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpire(-1);
        userRepository.save(user);
    }

    private static String extractUserId(String token) {
        return StringUtils.substring(token, 0, StringUtils.indexOf(token, "-"));
    }
}