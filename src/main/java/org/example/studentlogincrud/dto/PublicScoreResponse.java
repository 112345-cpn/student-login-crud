package org.example.studentlogincrud.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PublicScoreResponse {
    private String name;
    private String studentNo;
    private BigDecimal score;
}
