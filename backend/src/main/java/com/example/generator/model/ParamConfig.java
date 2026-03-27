package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class ParamConfig {
    private String paramType;
    private String dtoClassName;
    private List<SelectedField> selectedFields;
    private List<BasicParam> basicParams;
    
    public static final String TYPE_LIST_ENTITY = "LIST_ENTITY";
    public static final String TYPE_LIST_DTO = "LIST_DTO";
    public static final String TYPE_BASIC_PARAMS = "BASIC_PARAMS";
}