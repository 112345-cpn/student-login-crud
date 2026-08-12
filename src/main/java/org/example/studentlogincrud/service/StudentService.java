package org.example.studentlogincrud.service;

import com.baomidou.mybatisplus.spring.service.IService;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.entity.Student;

public interface StudentService extends IService<Student> {
    Result<Object> check(String studentNo);

    Result<Student> create(Student student);

    Result<Student> update(String studentNo, Student student);

    Result<Student> select(String studentNo);

    Result<Object> delete(String studentNo);
}
