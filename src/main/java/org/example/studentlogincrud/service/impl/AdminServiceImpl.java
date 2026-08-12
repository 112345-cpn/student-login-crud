package org.example.studentlogincrud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.example.studentlogincrud.entity.Admin;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.mapper.AdminMapper;
import org.example.studentlogincrud.service.AdminService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<Admin> login(Admin admin) {
        if (admin == null || isBlank(admin.getUsername())) {
            return Result.error(400, "请输入管理员姓名");
        }
        if (isBlank(admin.getPassword())) {
            return Result.error(400, "请输入密码");
        }

        String username = admin.getUsername().trim();
        Admin storedAdmin = getOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (storedAdmin == null || !matchesPassword(admin.getPassword(), storedAdmin.getPassword())) {
            return Result.error(401, "管理员姓名或密码错误");
        }

        storedAdmin.setPassword(null);
        return Result.success(storedAdmin);
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null || storedPassword.trim().isEmpty()) {
            return false;
        }
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")
                ? passwordEncoder.matches(rawPassword, storedPassword)
                : storedPassword.equals(rawPassword);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
