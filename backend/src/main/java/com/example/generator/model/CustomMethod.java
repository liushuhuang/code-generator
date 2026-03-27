package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class CustomMethod {
    private String methodName;
    private String methodType;
    private List<MethodParam> params;
    private String description;
    
    public static final String TYPE_SINGLE = "SINGLE";
    public static final String TYPE_LIST = "LIST";
    public static final String TYPE_PAGE = "PAGE";
    public static final String TYPE_COUNT = "COUNT";
    public static final String TYPE_EXISTS = "EXISTS";
    public static final String TYPE_DELETE = "DELETE";
    public static final String TYPE_UPDATE = "UPDATE";
}