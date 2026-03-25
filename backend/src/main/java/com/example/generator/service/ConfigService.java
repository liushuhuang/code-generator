package com.example.generator.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.example.generator.model.ConnectionConfig;
import com.example.generator.model.AppConfig;
import com.example.generator.model.GeneratorConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ConfigService {
    
    @Value("${generator.config.path:./config.json}")
    private String configPath;
    
    private AppConfig appConfig;
    
    @PostConstruct
    public void init() {
        loadConfig();
    }
    
    public void loadConfig() {
        File file = new File(configPath);
        if (file.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String content = new String(bytes, StandardCharsets.UTF_8);
                appConfig = JSONUtil.toBean(content, AppConfig.class);
                if (appConfig.getConnections() == null) {
                    appConfig.setConnections(new ArrayList<>());
                }
                if (appConfig.getSettings() == null) {
                    appConfig.setSettings(new GeneratorConfig());
                }
            } catch (IOException e) {
                log.error("加载配置文件失败", e);
                appConfig = new AppConfig();
                appConfig.setConnections(new ArrayList<>());
                appConfig.setSettings(new GeneratorConfig());
            }
        } else {
            appConfig = new AppConfig();
            appConfig.setConnections(new ArrayList<>());
            appConfig.setSettings(new GeneratorConfig());
        }
    }
    
    public void saveConfig() {
        try {
            String content = JSONUtil.toJsonPrettyStr(appConfig);
            Files.write(Paths.get(configPath), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("保存配置文件失败", e);
            throw new RuntimeException("保存配置文件失败", e);
        }
    }
    
    public List<ConnectionConfig> listConnections() {
        return appConfig.getConnections();
    }
    
    public ConnectionConfig getConnection(String id) {
        return appConfig.getConnections().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    public ConnectionConfig saveConnection(ConnectionConfig config) {
        if (config.getId() == null || config.getId().isEmpty()) {
            config.setId(IdUtil.simpleUUID());
        }
        
        Optional<ConnectionConfig> existing = appConfig.getConnections().stream()
                .filter(c -> c.getId().equals(config.getId()))
                .findFirst();
        
        if (existing.isPresent()) {
            appConfig.getConnections().remove(existing.get());
        }
        
        appConfig.getConnections().add(config);
        saveConfig();
        return config;
    }
    
    public void deleteConnection(String id) {
        appConfig.getConnections().removeIf(c -> c.getId().equals(id));
        saveConfig();
    }
    
    public GeneratorConfig getSettings() {
        return appConfig.getSettings();
    }
    
    public void saveSettings(GeneratorConfig settings) {
        appConfig.setSettings(settings);
        saveConfig();
    }
}