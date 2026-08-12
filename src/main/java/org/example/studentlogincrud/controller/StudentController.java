package org.example.studentlogincrud.controller;

import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.entity.Student;
import org.example.studentlogincrud.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

    // 学弟端入口：注册即登录，成功直接进入
    @PostMapping("/login")
    public Result<Student> login(@RequestBody Student student) {
        return studentService.login(student);
    }

    @GetMapping("/check")
    public Result<Object> check(@RequestParam String number) {
        return studentService.check(number);
    }

    @GetMapping("/{id}")
    public Result<Student> select(@PathVariable String id) {
        return studentService.select(id);
    }

    @DeleteMapping("/{id}")
    public Result<Object> delete(@PathVariable String id) {
        return studentService.delete(id);
    }
}

