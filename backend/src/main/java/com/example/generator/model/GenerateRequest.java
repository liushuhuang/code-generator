package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class GenerateRequest {
    private String connectionId;
    private List<String> tableNames;
    private String basePackage;
    private String author;
    private String outputPath;
}