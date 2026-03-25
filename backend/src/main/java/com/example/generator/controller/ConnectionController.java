package com.example.generator.controller;

import com.example.generator.common.Result;
import com.example.generator.model.ConnectionConfig;
import com.example.generator.service.ConfigService;
import com.example.generator.service.DatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connection")
@RequiredArgsConstructor
public class ConnectionController {
    
    private final ConfigService configService;
    private final DatabaseService databaseService;
    
    @GetMapping("/list")
    public Result<List<ConnectionConfig>> list() {
        return Result.success(configService.listConnections());
    }
    
    @GetMapping("/{id}")
    public Result<ConnectionConfig> get(@PathVariable String id) {
        ConnectionConfig config = configService.getConnection(id);
        if (config == null) {
            return Result.error(404, "连接配置不存在");
        }
        return Result.success(config);
    }
    
    @PostMapping("/save")
    public Result<ConnectionConfig> save(@RequestBody ConnectionConfig config) {
        return Result.success(configService.saveConnection(config));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        configService.deleteConnection(id);
        return Result.success();
    }
    
    @PostMapping("/test")
    public Result<Boolean> test(@RequestBody ConnectionConfig config) {
        return Result.success(databaseService.testConnection(config));
    }
}