package com.example.generator.service;

import com.example.generator.model.ColumnInfo;
import com.example.generator.model.ConnectionConfig;
import com.example.generator.model.TableInfo;
import com.example.generator.model.TableRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DatabaseService {
    
    public boolean testConnection(ConnectionConfig config) {
        String url = buildUrl(config);
        try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
            return conn.isValid(5);
        } catch (SQLException e) {
            log.error("测试连接失败", e);
            return false;
        }
    }
    
    public List<TableInfo> listTables(ConnectionConfig config) {
        String url = buildUrl(config);
        List<TableInfo> tables = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(config.getDatabase(), null, "%", new String[]{"TABLE"});
            
            while (rs.next()) {
                TableInfo table = new TableInfo();
                table.setTableName(rs.getString("TABLE_NAME"));
                table.setTableComment(rs.getString("REMARKS"));
                table.setColumns(listColumns(config, table.getTableName()));
                tables.add(table);
            }
        } catch (SQLException e) {
            log.error("获取表列表失败", e);
            throw new RuntimeException("获取表列表失败: " + e.getMessage());
        }
        
        return tables;
    }
    
    public TableInfo getTableInfo(ConnectionConfig config, String tableName) {
        String url = buildUrl(config);
        
        try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(config.getDatabase(), null, tableName, new String[]{"TABLE"});
            
            if (rs.next()) {
                TableInfo table = new TableInfo();
                table.setTableName(rs.getString("TABLE_NAME"));
                table.setTableComment(rs.getString("REMARKS"));
                table.setColumns(listColumns(config, tableName));
                return table;
            }
        } catch (SQLException e) {
            log.error("获取表信息失败", e);
            throw new RuntimeException("获取表信息失败: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<ColumnInfo> listColumns(ConnectionConfig config, String tableName) {
        String url = buildUrl(config);
        List<ColumnInfo> columns = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            ResultSet primaryKeys = metaData.getPrimaryKeys(config.getDatabase(), null, tableName);
            String primaryKey = null;
            while (primaryKeys.next()) {
                primaryKey = primaryKeys.getString("COLUMN_NAME");
            }
            
            ResultSet rs = metaData.getColumns(config.getDatabase(), null, tableName, "%");
            
            while (rs.next()) {
                ColumnInfo column = new ColumnInfo();
                String columnName = rs.getString("COLUMN_NAME");
                column.setColumnName(columnName);
                column.setColumnType(rs.getString("TYPE_NAME"));
                column.setColumnComment(rs.getString("REMARKS"));
                column.setJavaType(mapJavaType(rs.getInt("DATA_TYPE")));
                column.setFieldName(toCamelCase(columnName));
                column.setPrimaryKey(columnName.equals(primaryKey));
                column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                column.setDefaultValue(rs.getString("COLUMN_DEF"));
                columns.add(column);
            }
        } catch (SQLException e) {
            log.error("获取列信息失败", e);
            throw new RuntimeException("获取列信息失败: " + e.getMessage());
        }
        
        return columns;
    }
    
    public List<TableRelation> getTableRelations(ConnectionConfig config, String tableName) {
        String url = buildUrl(config);
        List<TableRelation> relations = new ArrayList<>();
        
        try (Connection conn = DriverManager.getConnection(url, config.getUsername(), config.getPassword())) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            ResultSet importedKeys = metaData.getImportedKeys(config.getDatabase(), null, tableName);
            while (importedKeys.next()) {
                TableRelation relation = new TableRelation();
                relation.setSourceColumn(importedKeys.getString("FKCOLUMN_NAME"));
                relation.setTargetTable(importedKeys.getString("PKTABLE_NAME"));
                relation.setTargetColumn(importedKeys.getString("PKCOLUMN_NAME"));
                relation.setRelationType(TableRelation.TYPE_ONE_TO_ONE);
                relation.setFieldName(toCamelCase(relation.getTargetTable()));
                relations.add(relation);
            }
            
        } catch (SQLException e) {
            log.error("获取外键关系失败", e);
        }
        
        return relations;
    }
    
    private String buildUrl(ConnectionConfig config) {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8",
                config.getHost(), config.getPort(), config.getDatabase());
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
    
    private String mapJavaType(int sqlType) {
        switch (sqlType) {
            case Types.BIT:
            case Types.BOOLEAN:
                return "Boolean";
            case Types.TINYINT:
                return "Byte";
            case Types.SMALLINT:
                return "Short";
            case Types.INTEGER:
                return "Integer";
            case Types.BIGINT:
                return "Long";
            case Types.FLOAT:
            case Types.REAL:
                return "Float";
            case Types.DOUBLE:
                return "Double";
            case Types.NUMERIC:
            case Types.DECIMAL:
                return "java.math.BigDecimal";
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
                return "String";
            case Types.DATE:
                return "java.time.LocalDate";
            case Types.TIME:
                return "java.time.LocalTime";
            case Types.TIMESTAMP:
                return "java.time.LocalDateTime";
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return "byte[]";
            case Types.CLOB:
                return "String";
            default:
                return "Object";
        }
    }
}