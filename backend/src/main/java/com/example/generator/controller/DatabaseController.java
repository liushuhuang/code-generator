package com.example.generator.controller;

import com.example.generator.common.Result;
import com.example.generator.model.TableInfo;
import com.example.generator.model.ConnectionConfig;
import com.example.generator.service.ConfigService;
import com.example.generator.service.DatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseController {
    
    private final DatabaseService databaseService;
    private final ConfigService configService;
    
    @GetMapping("/tables")
    public Result<List<TableInfo>> listTables(@RequestParam String connectionId) {
        ConnectionConfig config = configService.getConnection(connectionId);
        if (config == null) {
            return Result.error(404, "连接配置不存在");
        }
        return Result.success(databaseService.listTables(config));
    }
    
    @GetMapping("/table")
    public Result<TableInfo> getTable(@RequestParam String connectionId, @RequestParam String tableName) {
        ConnectionConfig config = configService.getConnection(connectionId);
        if (config == null) {
            return Result.error(404, "连接配置不存在");
        }
        return Result.success(databaseService.getTableInfo(config, tableName));
    }
}