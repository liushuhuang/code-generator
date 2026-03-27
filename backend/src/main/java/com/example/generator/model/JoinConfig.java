package com.example.generator.model;

import lombok.Data;

@Data
public class JoinConfig {
    private String joinType;
    private String targetTable;
    private String alias;
    private String sourceColumn;
    private String targetColumn;
    
    public static final String TYPE_LEFT = "LEFT";
    public static final String TYPE_RIGHT = "RIGHT";
    public static final String TYPE_INNER = "INNER";
}