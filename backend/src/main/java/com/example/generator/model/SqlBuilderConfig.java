package com.example.generator.model;

import lombok.Data;

@Data
public class SqlBuilderConfig {
    private String connectionId;
    private String tableName;
    private String methodName;
    private String methodDesc;
    private SqlOperationType operationType;
    private String returnType;
    private String basePackage;
    
    private ParamConfig paramConfig;
    private SqlDetailConfig sqlDetailConfig;
    private DynamicSqlConfig dynamicSqlConfig;
}