package org.example.studentlogincrud.controller;

import org.example.studentlogincrud.dto.PublicScoreResponse;
import org.example.studentlogincrud.entity.Result;
import org.example.studentlogincrud.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/scores")
public class PublicScoreController {
    private final StudentService studentService;

    public PublicScoreController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{publicId}")
    public Result<PublicScoreResponse> query(@PathVariable String publicId) {
        return studentService.queryPublicScore(publicId);
    }
}
