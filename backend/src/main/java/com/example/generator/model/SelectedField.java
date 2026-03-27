package com.example.generator.model;

import lombok.Data;

@Data
public class SelectedField {
    private String columnName;
    private String fieldName;
    private String javaType;
    private String columnComment;
    private boolean selected = true;
}