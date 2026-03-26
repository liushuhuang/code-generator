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
}