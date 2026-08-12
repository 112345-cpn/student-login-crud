package org.example.studentlogincrud.controller;

import org.example.studentlogincrud.entity.Admin;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @PostMapping("/login")
    public Result<Object> login(@RequestBody Admin admin){
        return adminService.login(admin);
    }
}
