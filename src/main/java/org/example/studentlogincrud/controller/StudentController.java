package org.example.studentlogincrud.controller;

import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.entity.Student;
import org.example.studentlogincrud.service.StudentService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /** 学生不再登录；此接口由管理员维护学生资料。 */
    @PostMapping
    public Result<Student> create(@RequestBody Student student) {
        return studentService.create(student);
    }


    @PutMapping("/{studentNo}")
    public Result<Student> update(@PathVariable String studentNo, @RequestBody Student student) {
        return studentService.update(studentNo, student);
    }

    @GetMapping("/check")
    public Result<Object> check(@RequestParam String studentNo) {
        return studentService.check(studentNo);
    }

    @GetMapping("/{studentNo}")
    public Result<Student> select(@PathVariable String studentNo) {
        return studentService.select(studentNo);
    }

    @DeleteMapping("/{studentNo}")
    public Result<Object> delete(@PathVariable String studentNo) {
        return studentService.delete(studentNo);
    }
}
