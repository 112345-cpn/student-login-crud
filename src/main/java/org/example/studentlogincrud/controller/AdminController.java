package org.example.studentlogincrud.controller;

import org.example.studentlogincrud.dto.AdminLoginResponse;
import org.example.studentlogincrud.entity.Admin;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.service.AdminService;
import org.example.studentlogincrud.service.TokenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final TokenService tokenService;

    public AdminController(AdminService adminService, TokenService tokenService) {
        this.adminService = adminService;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody Admin admin) {
        if (admin == null || isBlank(admin.getUsername())) {
            return Result.error(400, "请输入管理员姓名");
        }
        if (isBlank(admin.getPassword())) {
            return Result.error(400, "请输入密码");
        }

        AdminLoginResponse response = adminService.login(admin);
        if (response == null) {
            return Result.error(401, "管理员姓名或密码错误");
        }
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<AdminLoginResponse> register(@RequestBody Admin admin) {
        if (admin == null || isBlank(admin.getUsername())) {
            return Result.error(400, "请输入管理员姓名");
        }
        if (isBlank(admin.getPassword())) {
            return Result.error(400, "请输入密码");
        }
        if (admin.getPassword().trim().length() < 6) {
            return Result.error(400, "密码至少需要 6 位");
        }

        AdminLoginResponse response = adminService.register(admin);
        if (response == null) {
            return Result.error(409, "该管理员姓名已存在");
        }
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Object> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        tokenService.remove(extractToken(authorization));
        return Result.success();
    }

    private String extractToken(String authorization) {
        if (authorization == null) {
            return null;
        }
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
