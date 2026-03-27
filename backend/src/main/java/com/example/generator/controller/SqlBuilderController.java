package com.example.generator.controller;

import com.example.generator.model.ColumnInfo;
import com.example.generator.model.ConnectionConfig;
import com.example.generator.common.Result;
import com.example.generator.model.SqlBuilderConfig;
import com.example.generator.model.SqlPreviewResult;
import com.example.generator.service.ConfigService;
import com.example.generator.service.DatabaseService;
import com.example.generator.service.SqlBuilderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sql-builder")
public class SqlBuilderController {
    
    private final SqlBuilderService sqlBuilderService;
    private final DatabaseService databaseService;
    private final ConfigService configService;
    
    public SqlBuilderController(SqlBuilderService sqlBuilderService, 
                                DatabaseService databaseService,
                                ConfigService configService) {
        this.sqlBuilderService = sqlBuilderService;
        this.databaseService = databaseService;
        this.configService = configService;
    }
    
    @PostMapping("/preview")
    public Result<SqlPreviewResult> preview(@RequestBody SqlBuilderConfig config) {
        try {
            SqlPreviewResult result = sqlBuilderService.preview(config);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("预览失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/table-fields")
    public Result<List<ColumnInfo>> getTableFields(@RequestParam String connectionId,
                                                    @RequestParam String tableName) {
        try {
            ConnectionConfig conn = configService.getConnection(connectionId);
            if (conn == null) {
                return Result.error("连接不存在");
            }
            List<ColumnInfo> columns = databaseService.listColumns(conn, tableName);
            return Result.success(columns);
        } catch (Exception e) {
            return Result.error("获取字段失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/connections")
    public Result<List<ConnectionConfig>> getConnections() {
        return Result.success(configService.listConnections());
    }
}