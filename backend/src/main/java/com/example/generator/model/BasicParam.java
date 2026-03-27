package com.example.generator.model;

import lombok.Data;

@Data
public class BasicParam {
    private String paramName;
    private String paramType;
    private String description;
    
    public static final String TYPE_STRING = "String";
    public static final String TYPE_INTEGER = "Integer";
    public static final String TYPE_LONG = "Long";
    public static final String TYPE_DOUBLE = "Double";
    public static final String TYPE_BOOLEAN = "Boolean";
    public static final String TYPE_DATE = "Date";
    public static final String TYPE_LIST_STRING = "List<String>";
    public static final String TYPE_LIST_INTEGER = "List<Integer>";
    public static final String TYPE_LIST_LONG = "List<Long>";
}