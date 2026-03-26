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
import java.util.*;

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
    
    public byte[] exportConfig() {
        Map<String, Object> exportData = new LinkedHashMap<>();
        exportData.put("version", "1.0");
        exportData.put("exportTime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        exportData.put("connections", appConfig.getConnections());
        exportData.put("settings", appConfig.getSettings());
        
        String json = JSONUtil.toJsonPrettyStr(exportData);
        return json.getBytes(StandardCharsets.UTF_8);
    }
    
    public void importConfig(String content) {
        Map<String, Object> importData = JSONUtil.toBean(content, Map.class);
        
        if (importData.containsKey("connections")) {
            List<Map<String, Object>> connections = (List<Map<String, Object>>) importData.get("connections");
            List<ConnectionConfig> connectionConfigs = new ArrayList<>();
            for (Map<String, Object> map : connections) {
                ConnectionConfig config = JSONUtil.toBean(JSONUtil.toJsonStr(map), ConnectionConfig.class);
                connectionConfigs.add(config);
            }
            appConfig.setConnections(connectionConfigs);
        }
        
        if (importData.containsKey("settings")) {
            Map<String, Object> settingsMap = (Map<String, Object>) importData.get("settings");
            GeneratorConfig settings = JSONUtil.toBean(JSONUtil.toJsonStr(settingsMap), GeneratorConfig.class);
            appConfig.setSettings(settings);
        }
        
        saveConfig();
    }
    
    public Map<String, Object> getSettingsMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        GeneratorConfig settings = appConfig.getSettings();
        result.put("basePackage", settings.getBasePackage());
        result.put("author", settings.getAuthor());
        result.put("outputDir", settings.getOutputDir());
        result.put("ormType", settings.getOrmType());
        result.put("enableLombok", settings.isEnableLombok());
        result.put("enableSwagger", settings.isEnableSwagger());
        result.put("enableValidation", settings.isEnableValidation());
        result.put("enableChain", settings.isEnableChain());
        result.put("serializable", settings.isSerializable());
        result.put("dateType", settings.getDateType());
        result.put("entityPrefix", settings.getEntityPrefix());
        result.put("entitySuffix", settings.getEntitySuffix());
        result.put("removeTablePrefix", settings.getRemoveTablePrefix());
        result.put("namingStrategy", settings.getNamingStrategy());
        return result;
    }
    
    public void saveSettingsMap(Map<String, Object> settings) {
        GeneratorConfig config = appConfig.getSettings();
        if (settings.containsKey("basePackage")) {
            config.setBasePackage((String) settings.get("basePackage"));
        }
        if (settings.containsKey("author")) {
            config.setAuthor((String) settings.get("author"));
        }
        if (settings.containsKey("outputDir")) {
            config.setOutputDir((String) settings.get("outputDir"));
        }
        if (settings.containsKey("ormType")) {
            config.setOrmType((String) settings.get("ormType"));
        }
        if (settings.containsKey("enableLombok")) {
            config.setEnableLombok((Boolean) settings.get("enableLombok"));
        }
        if (settings.containsKey("enableSwagger")) {
            config.setEnableSwagger((Boolean) settings.get("enableSwagger"));
        }
        if (settings.containsKey("enableValidation")) {
            config.setEnableValidation((Boolean) settings.get("enableValidation"));
        }
        if (settings.containsKey("enableChain")) {
            config.setEnableChain((Boolean) settings.get("enableChain"));
        }
        if (settings.containsKey("serializable")) {
            config.setSerializable((Boolean) settings.get("serializable"));
        }
        if (settings.containsKey("dateType")) {
            config.setDateType((String) settings.get("dateType"));
        }
        if (settings.containsKey("entityPrefix")) {
            config.setEntityPrefix((String) settings.get("entityPrefix"));
        }
        if (settings.containsKey("entitySuffix")) {
            config.setEntitySuffix((String) settings.get("entitySuffix"));
        }
        if (settings.containsKey("removeTablePrefix")) {
            config.setRemoveTablePrefix((String) settings.get("removeTablePrefix"));
        }
        if (settings.containsKey("namingStrategy")) {
            config.setNamingStrategy((String) settings.get("namingStrategy"));
        }
        saveConfig();
    }
}