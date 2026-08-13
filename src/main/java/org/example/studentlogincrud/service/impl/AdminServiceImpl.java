package org.example.studentlogincrud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.example.studentlogincrud.dto.AdminLoginResponse;
import org.example.studentlogincrud.entity.Admin;
import org.example.studentlogincrud.mapper.AdminMapper;
import org.example.studentlogincrud.service.AdminService;
import org.example.studentlogincrud.service.TokenService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final TokenService tokenService;

    public AdminServiceImpl(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public AdminLoginResponse login(Admin admin) {
        if (admin == null || isBlank(admin.getUsername()) || isBlank(admin.getPassword())) {
            return null;
        }

        String username = admin.getUsername().trim();
        Admin storedAdmin = getOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (storedAdmin == null || !matchesPassword(admin.getPassword(), storedAdmin.getPassword())) {
            return null;
        }

        return createLoginResponse(storedAdmin);
    }

    @Override
    public AdminLoginResponse register(Admin admin) {
        if (admin == null || isBlank(admin.getUsername()) || isBlank(admin.getPassword())) {
            return null;
        }

        String username = admin.getUsername().trim();
        if (getOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)) != null) {
            return null;
        }

        Admin newAdmin = new Admin();
        newAdmin.setUsername(username);
        newAdmin.setPassword(passwordEncoder.encode(admin.getPassword()));
        save(newAdmin);
        return createLoginResponse(newAdmin);
    }

    private AdminLoginResponse createLoginResponse(Admin admin) {
        String token = tokenService.create(admin.getId());
        return new AdminLoginResponse(token,
                new AdminLoginResponse.AdminInfo(admin.getId(), admin.getUsername()));
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (isBlank(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
