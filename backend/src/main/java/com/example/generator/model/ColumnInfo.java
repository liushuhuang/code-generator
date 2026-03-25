package com.example.generator.model;

import lombok.Data;

@Data
public class ColumnInfo {
    private String columnName;
    private String columnType;
    private String columnComment;
    private String javaType;
    private String fieldName;
    private Boolean primaryKey;
    private Boolean nullable;
    private String defaultValue;
}