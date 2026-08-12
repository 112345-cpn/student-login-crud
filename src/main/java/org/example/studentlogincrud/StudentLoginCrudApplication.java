package org.example.studentlogincrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("org.example.studentlogincrud.mapper")
@SpringBootApplication
public class StudentLoginCrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentLoginCrudApplication.class, args);
	}

}
