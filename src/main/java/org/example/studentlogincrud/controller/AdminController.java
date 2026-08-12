package org.example.studentlogincrud.controller;

import jakarta.servlet.http.HttpSession;
import org.example.studentlogincrud.config.AdminAuthInterceptor;
import org.example.studentlogincrud.entity.Admin;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.service.AdminService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public Result<Admin> login(@RequestBody Admin admin, HttpSession session) {
        Result<Admin> result = adminService.login(admin);
        if (result.getCode() == 200 && result.getData() != null) {
            session.setAttribute(AdminAuthInterceptor.SESSION_ADMIN_ID, result.getData().getId());
        }
        return result;
    }

    @PostMapping("/logout")
    public Result<Object> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }
}
