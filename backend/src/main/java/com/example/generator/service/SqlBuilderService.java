package com.example.generator.service;

import cn.hutool.core.util.StrUtil;
import com.example.generator.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SqlBuilderService {
    
    private final DatabaseService databaseService;
    private final ConfigService configService;
    
    public SqlBuilderService(DatabaseService databaseService, ConfigService configService) {
        this.databaseService = databaseService;
        this.configService = configService;
    }
    
    public SqlPreviewResult preview(SqlBuilderConfig config) {
        SqlPreviewResult result = new SqlPreviewResult();
        result.setMapperMethod(generateMapperMethod(config));
        result.setXmlSql(generateXmlSql(config));
        
        if (config.getParamConfig() != null && 
            ParamConfig.TYPE_LIST_DTO.equals(config.getParamConfig().getParamType())) {
            result.setDtoClass(generateDtoClass(config));
        }
        
        result.setImports(collectImports(config));
        return result;
    }
    
    private String generateMapperMethod(SqlBuilderConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("    /**\n");
        sb.append("     * ").append(config.getMethodDesc()).append("\n");
        sb.append("     */\n");
        
        String returnType = config.getReturnType();
        if (returnType == null || returnType.isEmpty()) {
            returnType = "int";
        }
        sb.append("    ").append(returnType).append(" ").append(config.getMethodName()).append("(");
        
        ParamConfig paramConfig = config.getParamConfig();
        if (paramConfig != null) {
            String paramType = paramConfig.getParamType();
            if (ParamConfig.TYPE_LIST_ENTITY.equals(paramType)) {
                String className = toClassName(config.getTableName());
                sb.append("@Param(\"list\") List<").append(className).append("> list");
            } else if (ParamConfig.TYPE_LIST_DTO.equals(paramType)) {
                String dtoName = paramConfig.getDtoClassName();
                if (dtoName == null || dtoName.isEmpty()) {
                    dtoName = toClassName(config.getTableName()) + "DTO";
                }
                sb.append("@Param(\"list\") List<").append(dtoName).append("> list");
            } else if (ParamConfig.TYPE_BASIC_PARAMS.equals(paramType)) {
                List<BasicParam> params = paramConfig.getBasicParams();
                if (params != null && !params.isEmpty()) {
                    List<String> paramStrs = new ArrayList<>();
                    for (BasicParam param : params) {
                        paramStrs.add("@Param(\"" + param.getParamName() + "\") " + 
                                     param.getParamType() + " " + param.getParamName());
                    }
                    sb.append(String.join(", ", paramStrs));
                }
            }
        }
        
        sb.append(");\n");
        return sb.toString();
    }
    
    private String generateXmlSql(SqlBuilderConfig config) {
        SqlOperationType opType = config.getOperationType();
        if (opType == null) {
            opType = SqlOperationType.BATCH_INSERT;
        }
        
        switch (opType) {
            case BATCH_INSERT:
                return buildBatchInsertSql(config);
            case BATCH_INSERT_OR_UPDATE:
                return buildBatchInsertOrUpdateSql(config);
            case BATCH_UPDATE:
                return buildBatchUpdateSql(config);
            case BATCH_DELETE:
                return buildBatchDeleteSql(config);
            case SELECT_COMPLEX:
                return buildComplexSelectSql(config);
            default:
                return buildBatchInsertSql(config);
        }
    }
    
    private String buildBatchInsertSql(SqlBuilderConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <insert id=\"").append(config.getMethodName()).append("\" ");
        
        ParamConfig paramConfig = config.getParamConfig();
        if (paramConfig != null && ParamConfig.TYPE_LIST_DTO.equals(paramConfig.getParamType())) {
            String dtoName = paramConfig.getDtoClassName();
            if (dtoName == null || dtoName.isEmpty()) {
                dtoName = toClassName(config.getTableName()) + "DTO";
            }
            sb.append("parameterType=\"").append(config.getBasePackage()).append(".dto.").append(dtoName).append("\"");
        }
        sb.append(">\n");
        
        SqlDetailConfig sqlConfig = config.getSqlDetailConfig();
        List<String> insertFields = sqlConfig != null ? sqlConfig.getInsertFields() : null;
        
        if (insertFields == null || insertFields.isEmpty()) {
            insertFields = getAllColumnNames(config);
        }
        
        sb.append("        INSERT INTO ").append(config.getTableName()).append(" (");
        sb.append(String.join(", ", insertFields));
        sb.append(")\n");
        sb.append("        VALUES\n");
        sb.append("        <foreach collection=\"list\" item=\"item\" separator=\",\">\n");
        sb.append("            (");
        List<String> values = insertFields.stream()
                .map(f -> "#{item." + toCamelCase(f) + "}")
                .collect(Collectors.toList());
        sb.append(String.join(", ", values));
        sb.append(")\n");
        sb.append("        </foreach>\n");
        sb.append("    </insert>");
        
        return sb.toString();
    }
    
    private String buildBatchInsertOrUpdateSql(SqlBuilderConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <insert id=\"").append(config.getMethodName()).append("\">\n");
        
        SqlDetailConfig sqlConfig = config.getSqlDetailConfig();
        List<String> insertFields = sqlConfig != null ? sqlConfig.getInsertFields() : null;
        String uniqueKey = sqlConfig != null ? sqlConfig.getUniqueKeyField() : null;
        List<String> updateFields = sqlConfig != null ? sqlConfig.getUpdateOnDuplicateFields() : null;
        
        if (insertFields == null || insertFields.isEmpty()) {
            insertFields = getAllColumnNames(config);
        }
        
        sb.append("        INSERT INTO ").append(config.getTableName()).append(" (");
        sb.append(String.join(", ", insertFields));
        sb.append(")\n");
        sb.append("        VALUES\n");
        sb.append("        <foreach collection=\"list\" item=\"item\" separator=\",\">\n");
        sb.append("            (");
        List<String> values = insertFields.stream()
                .map(f -> "#{item." + toCamelCase(f) + "}")
                .collect(Collectors.toList());
        sb.append(String.join(", ", values));
        sb.append(")\n");
        sb.append("        </foreach>\n");
        
        if (updateFields != null && !updateFields.isEmpty()) {
            sb.append("        ON DUPLICATE KEY UPDATE\n");
            List<String> updates = updateFields.stream()
                    .map(f -> f + " = VALUES(" + f + ")")
                    .collect(Collectors.toList());
            sb.append("        ").append(String.join(",\n        ", updates)).append("\n");
        }
        
        sb.append("    </insert>");
        return sb.toString();
    }
    
    private String buildBatchUpdateSql(SqlBuilderConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <update id=\"").append(config.getMethodName()).append("\">\n");
        
        SqlDetailConfig sqlConfig = config.getSqlDetailConfig();
        String updateMode = sqlConfig != null ? sqlConfig.getUpdateMode() : SqlDetailConfig.UPDATE_MODE_CASE_WHEN;
        List<String> updateFields = sqlConfig != null ? sqlConfig.getUpdateFields() : null;
        List<String> whereFields = sqlConfig != null ? sqlConfig.getWhereFields() : null;
        
        if (updateFields == null || updateFields.isEmpty()) {
            updateFields = new ArrayList<>();
        }
        if (whereFields == null || whereFields.isEmpty()) {
            whereFields = new ArrayList<>();
            whereFields.add("id");
        }
        
        if (SqlDetailConfig.UPDATE_MODE_CASE_WHEN.equals(updateMode)) {
            sb.append("        UPDATE ").append(config.getTableName()).append("\n");
            sb.append("        <set>\n");
            
            for (String field : updateFields) {
                sb.append("            <trim prefix=\"").append(field).append(" = CASE\" suffix=\"END,\">\n");
                sb.append("                <foreach collection=\"list\" item=\"item\">\n");
                sb.append("                    WHEN ");
                for (int i = 0; i < whereFields.size(); i++) {
                    if (i > 0) sb.append(" AND ");
                    sb.append(whereFields.get(i)).append(" = #{item.").append(toCamelCase(whereFields.get(i))).append("}");
                }
                sb.append(" THEN #{item.").append(toCamelCase(field)).append("}\n");
                sb.append("                </foreach>\n");
                sb.append("            </trim>\n");
            }
            
            sb.append("        </set>\n");
            sb.append("        WHERE ");
            for (int i = 0; i < whereFields.size(); i++) {
                if (i > 0) sb.append(" AND ");
                sb.append(whereFields.get(i)).append(" IN\n");
                sb.append("            <foreach collection=\"list\" item=\"item\" open=\"(\" separator=\",\" close=\")\">\n");
                sb.append("                #{item.").append(toCamelCase(whereFields.get(i))).append("}\n");
                sb.append("            </foreach>\n");
            }
        } else {
            sb.append("        <foreach collection=\"list\" item=\"item\" separator=\",\">\n");
            sb.append("            UPDATE ").append(config.getTableName()).append("\n");
            sb.append("            <set>\n");
            for (String field : updateFields) {
                sb.append("                <if test=\"item.").append(toCamelCase(field)).append(" != null\">\n");
                sb.append("                    ").append(field).append(" = #{item.").append(toCamelCase(field)).append("},\n");
                sb.append("                </if>\n");
            }
            sb.append("            </set>\n");
            sb.append("            WHERE ");
            for (int i = 0; i < whereFields.size(); i++) {
                if (i > 0) sb.append(" AND ");
                sb.append(whereFields.get(i)).append(" = #{item.").append(toCamelCase(whereFields.get(i))).append("}");
            }
            sb.append("\n");
            sb.append("        </foreach>\n");
        }
        
        sb.append("    </update>");
        return sb.toString();
    }
    
    private String buildBatchDeleteSql(SqlBuilderConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <delete id=\"").append(config.getMethodName()).append("\">\n");
        
        SqlDetailConfig sqlConfig = config.getSqlDetailConfig();
        String conditionField = sqlConfig != null ? sqlConfig.getDeleteConditionField() : "id";
        
        sb.append("        DELETE FROM ").append(config.getTableName()).append("\n");
        sb.append("        WHERE ").append(conditionField).append(" IN\n");
        sb.append("        <foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">\n");
        sb.append("            #{id}\n");
        sb.append("        </foreach>\n");
        sb.append("    </delete>");
        
        return sb.toString();
    }
    
    private String buildComplexSelectSql(SqlBuilderConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("    <select id=\"").append(config.getMethodName()).append("\" resultType=\"\">\n");
        
        DynamicSqlConfig dynamicConfig = config.getDynamicSqlConfig();
        
        sb.append("        SELECT\n");
        
        if (dynamicConfig != null && dynamicConfig.getSelectFunctions() != null 
            && !dynamicConfig.getSelectFunctions().isEmpty()) {
            List<String> selects = new ArrayList<>();
            for (SelectFunction func : dynamicConfig.getSelectFunctions()) {
                String expr = func.getFunction() + "(" + func.getFieldName() + ")";
                if (func.getAlias() != null && !func.getAlias().isEmpty()) {
                    expr += " AS " + func.getAlias();
                }
                selects.add(expr);
            }
            sb.append("        ").append(String.join(",\n        ", selects)).append("\n");
        }
        
        sb.append("        FROM ").append(config.getTableName()).append("\n");
        
        if (dynamicConfig != null && dynamicConfig.isEnableJoin() 
            && dynamicConfig.getJoinConfigs() != null) {
            for (JoinConfig join : dynamicConfig.getJoinConfigs()) {
                sb.append("        ").append(join.getJoinType()).append(" JOIN ")
                  .append(join.getTargetTable());
                if (join.getAlias() != null && !join.getAlias().isEmpty()) {
                    sb.append(" ").append(join.getAlias());
                }
                sb.append(" ON ").append(join.getSourceColumn()).append(" = ").append(join.getTargetColumn()).append("\n");
            }
        }
        
        if (dynamicConfig != null && dynamicConfig.isEnableDynamicWhere() 
            && dynamicConfig.getWhereConditions() != null 
            && !dynamicConfig.getWhereConditions().isEmpty()) {
            sb.append("        <where>\n");
            for (WhereCondition cond : dynamicConfig.getWhereConditions()) {
                sb.append(buildWhereCondition(cond));
            }
            sb.append("        </where>\n");
        }
        
        if (dynamicConfig != null && dynamicConfig.isEnableGroupBy() 
            && dynamicConfig.getGroupByFields() != null 
            && !dynamicConfig.getGroupByFields().isEmpty()) {
            sb.append("        GROUP BY ").append(String.join(", ", dynamicConfig.getGroupByFields())).append("\n");
        }
        
        if (dynamicConfig != null && dynamicConfig.getOrderByField() != null 
            && !dynamicConfig.getOrderByField().isEmpty()) {
            sb.append("        ORDER BY ").append(dynamicConfig.getOrderByField());
            if (!dynamicConfig.isOrderAsc()) {
                sb.append(" DESC");
            }
            sb.append("\n");
        }
        
        sb.append("    </select>");
        return sb.toString();
    }
    
    private String buildWhereCondition(WhereCondition cond) {
        StringBuilder sb = new StringBuilder();
        String field = cond.getFieldName();
        String param = cond.getParamSource() != null ? cond.getParamSource() : toCamelCase(field);
        String op = cond.getOperator();
        
        sb.append("            <if test=\"").append(param).append(" != null");
        if (WhereCondition.OP_IS_NULL.equals(op) || WhereCondition.OP_IS_NOT_NULL.equals(op)) {
            sb.append("\">\n");
            sb.append("                AND ").append(field).append(" ").append(op).append("\n");
        } else if (WhereCondition.OP_IN.equals(op) || WhereCondition.OP_NOT_IN.equals(op)) {
            sb.append("\">\n");
            sb.append("                AND ").append(field).append(" ").append(op).append("\n");
            sb.append("                <foreach collection=\"").append(param).append("\" item=\"v\" open=\"(\" separator=\",\" close=\")\">#{v}</foreach>\n");
        } else if (WhereCondition.OP_BETWEEN.equals(op)) {
            sb.append("\">\n");
            sb.append("                AND ").append(field).append(" BETWEEN #{").append(param).append("Start} AND #{").append(param).append("End}\n");
        } else {
            sb.append("\">\n");
            sb.append("                AND ").append(field).append(" ").append(op);
            if (WhereCondition.OP_LIKE.equals(op)) {
                sb.append(" CONCAT('%', #{").append(param).append("}, '%')\n");
            } else if (WhereCondition.OP_LIKE_LEFT.equals(op)) {
                sb.append(" CONCAT('%', #{").append(param).append("})\n");
            } else if (WhereCondition.OP_LIKE_RIGHT.equals(op)) {
                sb.append(" CONCAT(#{").append(param).append("}, '%')\n");
            } else {
                sb.append(" #{").append(param).append("}\n");
            }
        }
        sb.append("            </if>\n");
        return sb.toString();
    }
    
    private String generateDtoClass(SqlBuilderConfig config) {
        ParamConfig paramConfig = config.getParamConfig();
        if (paramConfig == null || paramConfig.getSelectedFields() == null) {
            return "";
        }
        
        String className = paramConfig.getDtoClassName();
        if (className == null || className.isEmpty()) {
            className = toClassName(config.getTableName()) + "DTO";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(config.getBasePackage()).append(".dto;\n\n");
        sb.append("import lombok.Data;\n");
        sb.append("import java.io.Serializable;\n");
        sb.append("import java.util.List;\n\n");
        sb.append("@Data\n");
        sb.append("public class ").append(className).append(" implements Serializable {\n\n");
        sb.append("    private static final long serialVersionUID = 1L;\n\n");
        
        for (SelectedField field : paramConfig.getSelectedFields()) {
            if (field.isSelected()) {
                sb.append("    /**\n");
                sb.append("     * ").append(field.getColumnComment() != null ? field.getColumnComment() : field.getFieldName()).append("\n");
                sb.append("     */\n");
                sb.append("    private ").append(field.getJavaType()).append(" ").append(field.getFieldName()).append(";\n\n");
            }
        }
        
        sb.append("}\n");
        return sb.toString();
    }
    
    private List<String> collectImports(SqlBuilderConfig config) {
        List<String> imports = new ArrayList<>();
        imports.add("org.apache.ibatis.annotations.Param");
        imports.add("java.util.List");
        return imports;
    }
    
    private List<String> getAllColumnNames(SqlBuilderConfig config) {
        if (config.getConnectionId() != null && config.getTableName() != null) {
            ConnectionConfig conn = configService.getConnection(config.getConnectionId());
            if (conn != null) {
                List<ColumnInfo> columns = databaseService.listColumns(conn, config.getTableName());
                return columns.stream()
                        .map(ColumnInfo::getColumnName)
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
    
    private String toClassName(String tableName) {
        String[] parts = tableName.split("_");
        return java.util.Arrays.stream(parts)
                .map(part -> StrUtil.upperFirst(part.toLowerCase()))
                .collect(Collectors.joining());
    }
    
    private String toCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        
        for (char c : name.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        
        return result.toString();
    }
}