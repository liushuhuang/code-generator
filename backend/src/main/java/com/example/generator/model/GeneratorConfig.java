package com.example.generator.model;

import lombok.Data;

@Data
public class GeneratorConfig {
    private String basePackage = "com.example.generator";
    private String author = "generator";
    private String outputDir;
}