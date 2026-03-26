package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class GenerateRequest {
    private String connectionId;
    private String ormType;
    private List<String> tableNames;
    private List<TableGenerateConfig> tableConfigs;
    private String basePackage;
    private String author;
    private String outputPath;
    
    private Boolean enableLombok;
    private Boolean enableSwagger;
    private Boolean enableValidation;
    private Boolean enableChain;
    private Boolean serializable;
    private String dateType;
    
    private String entityPrefix;
    private String entitySuffix;
    private String removeTablePrefix;
    private String namingStrategy;
}