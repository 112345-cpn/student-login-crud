package org.example.studentlogincrud.service;

import com.baomidou.mybatisplus.spring.service.IService;
import org.example.studentlogincrud.dto.AdminLoginResponse;
import org.example.studentlogincrud.entity.Admin;

public interface AdminService extends IService<Admin> {
    AdminLoginResponse login(Admin admin);

    AdminLoginResponse register(Admin admin);
}
