package com.qairline.qairline_backend.client.admin.repository;

import com.qairline.qairline_backend.client.admin.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {
    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);
}
