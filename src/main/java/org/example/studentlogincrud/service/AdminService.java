package org.example.studentlogincrud.service;

import com.baomidou.mybatisplus.spring.service.IService;
import org.example.studentlogincrud.entity.Admin;
import org.example.studentlogincrud.entity.Result;

public interface AdminService extends IService<Admin> {
    Result<Object> login(Admin admin);
}
