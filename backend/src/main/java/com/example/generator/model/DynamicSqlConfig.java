package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class DynamicSqlConfig {
    private boolean enableDynamicWhere;
    private List<WhereCondition> whereConditions;
    
    private boolean enableJoin;
    private List<JoinConfig> joinConfigs;
    
    private boolean enableGroupBy;
    private List<String> groupByFields;
    private List<SelectFunction> selectFunctions;
    
    private String orderByField;
    private boolean orderAsc;
}