package com.example.generator.model;

import lombok.Data;

@Data
public class TableRelation {
    private String sourceColumn;
    private String targetTable;
    private String targetColumn;
    private String relationType;
    private String fieldName;
    private String fetchType = "EAGER";
    
    public static final String TYPE_ONE_TO_ONE = "ONE_TO_ONE";
    public static final String TYPE_ONE_TO_MANY = "ONE_TO_MANY";
    public static final String FETCH_EAGER = "EAGER";
    public static final String FETCH_LAZY = "LAZY";
}