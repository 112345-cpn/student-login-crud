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
    @Override
    public Result<Object> login(Admin admin) {
        if (admin == null || admin.getUsername() == null || admin.getUsername().trim().isEmpty()) {
            return Result.error(400, "请输入用户名");
        }
        if (admin.getPassword() == null || admin.getPassword().isEmpty()) {
            return Result.error(400, "请输入密码");
        }


    }
}
