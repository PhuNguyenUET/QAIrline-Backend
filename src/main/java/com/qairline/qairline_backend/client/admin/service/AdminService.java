package com.qairline.qairline_backend.client.admin.service;

import com.qairline.qairline_backend.client.admin.model.Admin;
import com.qairline.qairline_backend.client.service.ClientService;
import com.qairline.qairline_backend.client.admin.dto.AdminInviteDTO;

import java.util.List;

public interface AdminService extends ClientService {
    void inviteAdmin(AdminInviteDTO dto);

    Admin getAdminByUsername(String username);

    List<Admin> findAll();

    Admin getCurrentAdmin();

    void unlockAdmin(String email);

    void lockAdmin(String email);

    void unlockUser(String username);

    void lockUser(String username);
}
