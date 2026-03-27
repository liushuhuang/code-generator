package com.example.generator.model;

import lombok.Data;

@Data
public class QueryFieldConfig {
    private String fieldName;
    private String queryType = "EQ";
    private boolean required = false;
    
    public static final String TYPE_EQ = "EQ";
    public static final String TYPE_LIKE = "LIKE";
    public static final String TYPE_LIKE_LEFT = "LIKE_LEFT";
    public static final String TYPE_LIKE_RIGHT = "LIKE_RIGHT";
    public static final String TYPE_IN = "IN";
    public static final String TYPE_NOT_IN = "NOT_IN";
    public static final String TYPE_BETWEEN = "BETWEEN";
    public static final String TYPE_GT = "GT";
    public static final String TYPE_GTE = "GTE";
    public static final String TYPE_LT = "LT";
    public static final String TYPE_LTE = "LTE";
    public static final String TYPE_IS_NULL = "IS_NULL";
    public static final String TYPE_IS_NOT_NULL = "IS_NOT_NULL";
}