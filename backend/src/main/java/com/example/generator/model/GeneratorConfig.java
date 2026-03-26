package com.example.generator.model;

import lombok.Data;

@Data
public class GeneratorConfig {
    private String basePackage = "com.example.generator";
    private String author = "generator";
    private String outputDir;
    private String ormType = "mybatis-plus";
    
    private boolean enableLombok = true;
    private boolean enableSwagger = false;
    private boolean enableValidation = false;
    private boolean enableChain = false;
    private boolean serializable = true;
    private String dateType = "LocalDateTime";
    
    private String entityPrefix = "";
    private String entitySuffix = "";
    private String removeTablePrefix = "";
    private String namingStrategy = "camelCase";
}