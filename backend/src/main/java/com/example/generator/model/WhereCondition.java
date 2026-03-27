package com.example.generator.model;

import lombok.Data;

@Data
public class WhereCondition {
    private String fieldName;
    private String operator;
    private String paramSource;
    private boolean required;
    
    public static final String OP_EQ = "=";
    public static final String OP_NE = "!=";
    public static final String OP_GT = ">";
    public static final String OP_GTE = ">=";
    public static final String OP_LT = "<";
    public static final String OP_LTE = "<=";
    public static final String OP_LIKE = "LIKE";
    public static final String OP_LIKE_LEFT = "LIKE_LEFT";
    public static final String OP_LIKE_RIGHT = "LIKE_RIGHT";
    public static final String OP_IN = "IN";
    public static final String OP_NOT_IN = "NOT_IN";
    public static final String OP_BETWEEN = "BETWEEN";
    public static final String OP_IS_NULL = "IS NULL";
    public static final String OP_IS_NOT_NULL = "IS NOT NULL";
}