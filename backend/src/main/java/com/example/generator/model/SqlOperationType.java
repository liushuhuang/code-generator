package com.example.generator.model;

public enum SqlOperationType {
    BATCH_INSERT("批量插入"),
    BATCH_INSERT_OR_UPDATE("批量插入或更新"),
    BATCH_UPDATE("批量更新"),
    BATCH_DELETE("批量删除"),
    SELECT_COMPLEX("复杂查询");
    
    private final String description;
    
    SqlOperationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}