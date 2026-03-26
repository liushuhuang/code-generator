package com.example.generator.controller;

import com.example.generator.common.Result;
import com.example.generator.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {
    
    private final ConfigService configService;
    
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportConfig() {
        byte[] content = configService.exportConfig();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generator-config.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(content);
    }
    
    @PostMapping("/import")
    public Result<Void> importConfig(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            configService.importConfig(content);
            return Result.success();
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/settings")
    public Result<Map<String, Object>> getSettings() {
        return Result.success(configService.getSettingsMap());
    }
    
    @PostMapping("/settings")
    public Result<Void> saveSettings(@RequestBody Map<String, Object> settings) {
        configService.saveSettingsMap(settings);
        return Result.success();
    }
}