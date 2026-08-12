package org.example.studentlogincrud.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.entity.Student;
import org.example.studentlogincrud.mapper.StudentMapper;
import org.example.studentlogincrud.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper,Student> implements StudentService {
    @Override
    public Result<Object> check(String number) {
        if (number == null || !number.matches("^26\\d{8}$")) {
            return Result.error(400, "学号必须是10位数字，且以26开头");
        }
        Long count = studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, number));//利用条件构造器查找有无相等
        if (count > 0) {
            return Result.error(400, "该学号已存在");
        }
        return Result.success(true);
    }

    @Autowired
    private StudentMapper studentMapper;
    @Override
    public Result<Student> login(Student student) {
        if (student == null || student.getStudentNo() == null || student.getStudentNo().trim().isEmpty()) {
            return Result.error(400, "请输入学号");
        }
        String number = student.getStudentNo().trim();
        if (!number.matches("^26\\d{8}$")) {
            return Result.error(400, "学号必须是10位数字，且以26开头");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            return Result.error(400, "请输入姓名");
        }
        // 已注册过：直接进入
        Student exist = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, number));
        if (exist != null) {
            return Result.success(exist);
        }
        // 首次进入：保存（注册）后进入
        student.setStudentNo(number);
        this.save(student);
        return Result.success(student);
    }

    @Override
    public Result<Student> select(String id) {
        Student s = this.getById(id);
        if(s==null){
            return Result.error(400,"学生不存在");
        }
        return Result.success(s);
    }

    @Override
    public Result<Object> delete(String id) {
        boolean b = this.removeById(id);
        if(!b){
            return Result.error(400,"该学生不存在");
        }
        return Result.success();
    }
}
