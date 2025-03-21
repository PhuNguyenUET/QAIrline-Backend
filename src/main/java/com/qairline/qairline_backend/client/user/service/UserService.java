package com.qairline.qairline_backend.client.user.service;


import com.qairline.qairline_backend.client.dto.CreateNewPasswordRequest;
import com.qairline.qairline_backend.client.service.ClientService;
import com.qairline.qairline_backend.client.user.dto.*;
import com.qairline.qairline_backend.client.user.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.text.ParseException;
import java.util.List;

public interface UserService extends ClientService {
    void changeEmail(String newEmail);

    void register (UserRegisterDTO dto);

    void sendConfirmEmail();

    void confirmEmail(String token);

    User getUserByUsername(String username);

    List<User> findAll();

    User getCurrentUser();

    void editUser(UserEditDTO dto) throws ParseException;
}
