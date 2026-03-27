package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class TableGenerateConfig {
    private String tableName;
    private List<String> selectedFields;
    private boolean generateVO;
    private boolean generateDTO;
    private boolean generateQuery;
    
    private List<QueryFieldConfig> queryFields;
    private boolean enablePage = true;
    private String defaultSortField;
    private String defaultSortOrder = "DESC";
    
    private List<CustomMethod> customMethods;
    
    private List<EnumConfig> enumConfigs;
    
    private List<TableRelation> relations;
}