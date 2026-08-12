package org.example.studentlogincrud.service;

import com.baomidou.mybatisplus.spring.service.IService;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.entity.Student;

public interface StudentService extends IService<Student> {
    // 学弟端：注册即登录
    Result<Student> login(Student student);

    Result<Object> check(String number);

    Result<Student> select(String id);

    Result<Object> delete(String id);
}
