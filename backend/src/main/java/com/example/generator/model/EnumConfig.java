package com.example.generator.model;

import lombok.Data;
import java.util.Map;

@Data
public class EnumConfig {
    private String columnName;
    private String enumName;
    private Map<String, String> values;
    private Map<String, String> descriptions;
}