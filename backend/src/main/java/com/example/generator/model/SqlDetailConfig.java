package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class SqlDetailConfig {
    private List<String> insertFields;
    private boolean useSelectKey;
    
    private String uniqueKeyField;
    private List<String> updateOnDuplicateFields;
    
    private String updateMode;
    private List<String> updateFields;
    private List<String> whereFields;
    
    private String deleteConditionField;
    
    public static final String UPDATE_MODE_CASE_WHEN = "CASE_WHEN";
    public static final String UPDATE_MODE_FOREACH = "FOREACH";
}