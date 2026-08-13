package org.example.studentlogincrud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminLoginResponse {
    private String token;
    private AdminInfo admin;

    @Data
    @AllArgsConstructor
    public static class AdminInfo {
        private Long id;
        private String username;
    }
}
