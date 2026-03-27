package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class SqlPreviewResult {
    private String mapperMethod;
    private String xmlSql;
    private String dtoClass;
    private List<String> imports;
}