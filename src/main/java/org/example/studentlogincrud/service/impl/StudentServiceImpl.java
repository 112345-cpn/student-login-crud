package org.example.studentlogincrud.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.example.studentlogincrud.dto.PublicScoreResponse;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.entity.Student;
import org.example.studentlogincrud.mapper.StudentMapper;
import org.example.studentlogincrud.service.StudentService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    private static final String STUDENT_NUMBER_REGEX = "^2600\\d{6}$";
    private static final String PUBLIC_ID_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public Result<Object> check(String studentNo) {
        if (!isValidStudentNo(studentNo)) {
            return Result.error(400, "学号必须是10位数字，且以2600开头");
        }
        long count = count(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo.trim()));
        if (count > 0) {
            return Result.error(400, "该学号已存在");
        }
        return Result.success(true);
    }

    @Override
    public Result<Student> create(Student student) {
        Result<Object> validation = validateStudent(student, true);
        if (validation.getCode() != 200) {
            return Result.error(validation.getCode(), validation.getMessage());
        }

        String number = student.getStudentNo().trim();
        if (count(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, number)) > 0) {
            return Result.error(400, "该学号已存在");
        }

        student.setStudentNo(number);
        student.setName(student.getName().trim());
        if (student.getRegisterTime() == null) {
            student.setRegisterTime(LocalDateTime.now());
        }
        student.setPublicId(createPublicId());
        save(student);
        return Result.success(student);
    }

    private String createPublicId() {
        StringBuilder publicId = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            publicId.append(PUBLIC_ID_CHARS.charAt(RANDOM.nextInt(PUBLIC_ID_CHARS.length())));
        }
        return publicId.toString();
    }

    @Override
    public Result<Student> update(String studentNo, Student student) {
        if (!isValidStudentNo(studentNo)) {
            return Result.error(400, "学号必须是10位数字，且以2600开头");
        }
        Student existing = getOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo.trim()));
        if (existing == null) {
            return Result.error(404, "学生不存在");
        }

        Result<Object> validation = validateStudent(student, false);
        if (validation.getCode() != 200) {
            return Result.error(validation.getCode(), validation.getMessage());
        }

        if (student.getStudentNo() != null) {
            String newStudentNo = student.getStudentNo().trim();
            if (!newStudentNo.equals(existing.getStudentNo())
                    && count(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, newStudentNo)) > 0) {
                return Result.error(400, "该学号已存在");
            }
            existing.setStudentNo(newStudentNo);
        }
        if (student.getName() != null) {
            existing.setName(student.getName().trim());
        }
        if (student.getScore() != null) {
            existing.setScore(student.getScore());
        }
        if (student.getRegisterTime() != null) {
            existing.setRegisterTime(student.getRegisterTime());
        }
        updateById(existing);
        return Result.success(existing);
    }

    private Result<Object> validateStudent(Student student, boolean requireAll) {
        if (student == null) {
            return Result.error(400, "学生信息不能为空");
        }
        if (requireAll || student.getStudentNo() != null) {
            if (!isValidStudentNo(student.getStudentNo())) {
                return Result.error(400, "学号必须是10位数字，且以2600开头");
            }
        }
        if (requireAll || student.getName() != null) {
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                return Result.error(400, "请输入学生姓名");
            }
        }
        if (student.getScore() != null && student.getScore().signum() < 0) {
            return Result.error(400, "成绩不能为负数");
        }
        return Result.success();
    }

    private boolean isValidStudentNo(String studentNo) {
        return studentNo != null && studentNo.trim().matches(STUDENT_NUMBER_REGEX);
    }

    @Override
    public Result<Student> select(String studentNo) {
        Student student = getOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo.trim()));
        if (student == null) {
            return Result.error(404, "学生不存在");
        }
        return Result.success(student);
    }

    @Override
    public Result<Object> delete(String studentNo) {
        if (!isValidStudentNo(studentNo)) {
            return Result.error(400, "学号必须是10位数字，且以2600开头");
        }
        boolean removed = remove(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo.trim()));
        if (!removed) {
            return Result.error(404, "学生不存在");
        }
        return Result.success();
    }
    @Override
    public Result<PublicScoreResponse> queryPublicScore(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            return Result.error(400, "查询码不能为空");
        }

        Student student = getOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getPublicId, publicId.trim()));
        if (student == null) {
            return Result.error(404, "未找到对应学生成绩");
        }

        PublicScoreResponse response = new PublicScoreResponse();
        response.setName(student.getName());
        response.setStudentNo(student.getStudentNo());
        response.setScore(student.getScore());
        return Result.success(response);
    }
}