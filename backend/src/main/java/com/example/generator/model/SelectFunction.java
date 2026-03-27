package com.example.generator.model;

import lombok.Data;

@Data
public class SelectFunction {
    private String function;
    private String fieldName;
    private String alias;
    
    public static final String FUNC_COUNT = "COUNT";
    public static final String FUNC_SUM = "SUM";
    public static final String FUNC_AVG = "AVG";
    public static final String FUNC_MAX = "MAX";
    public static final String FUNC_MIN = "MIN";
}