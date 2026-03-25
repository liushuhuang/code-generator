package com.example.generator.model;

import lombok.Data;

@Data
public class GenerateResult {
    private Boolean success;
    private String message;
    private String filePath;
}