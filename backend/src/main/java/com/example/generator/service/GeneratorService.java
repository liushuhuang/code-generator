package com.example.generator.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.example.generator.model.*;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeneratorService {
    
    @Value("${generator.template.path:./templates/}")
    private String templatePath;
    
    private Configuration cfg;
    
    private final DatabaseService databaseService;
    private final TemplateService templateService;
    
    public GeneratorService(DatabaseService databaseService, TemplateService templateService) {
        this.databaseService = databaseService;
        this.templateService = templateService;
    }
    
    @PostConstruct
    public void init() throws IOException {
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDirectoryForTemplateLoading(new File(templatePath));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }
    
    public List<PreviewResult> preview(String connectionId, List<String> tableNames, String basePackage, String author) {
        List<PreviewResult> results = new ArrayList<>();
        
        for (String tableName : tableNames) {
            ConnectionConfig conn = templateService.getConfigService().getConnection(connectionId);
            if (conn == null) {
                throw new RuntimeException("连接配置不存在");
            }
            
            TableInfo tableInfo = databaseService.getTableInfo(conn, tableName);
            if (tableInfo == null) {
                continue;
            }
            
            String className = toClassName(tableName);
            Map<String, Object> dataModel = buildDataModel(tableInfo, className, basePackage, author);
            
            for (String templateName : Arrays.asList("entity", "mapper", "service", "controller")) {
                try {
                    String content = templateService.render(templateName, dataModel);
                    String fileName = getFileName(templateName, className);
                    
                    PreviewResult result = new PreviewResult();
                    result.setFileName(fileName);
                    result.setContent(content);
                    result.setType(templateName);
                    results.add(result);
                } catch (Exception e) {
                    log.error("渲染模板失败: {}", templateName, e);
                }
            }
        }
        
        return results;
    }
    
    public byte[] exportZip(String connectionId, List<String> tableNames, String basePackage, String author) {
        List<PreviewResult> previews = preview(connectionId, tableNames, basePackage, author);
        
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "codegen_" + IdUtil.simpleUUID());
        tempDir.mkdirs();
        
        try {
            for (PreviewResult preview : previews) {
                String filePath = getPackagePath(preview.getType(), basePackage);
                File dir = new File(tempDir, filePath);
                dir.mkdirs();
                
                File file = new File(dir, preview.getFileName());
                Files.write(file.toPath(), preview.getContent().getBytes(StandardCharsets.UTF_8));
            }
            
            File zipFile = ZipUtil.zip(tempDir);
            return Files.readAllBytes(zipFile.toPath());
        } catch (IOException e) {
            log.error("生成ZIP失败", e);
            throw new RuntimeException("生成ZIP失败: " + e.getMessage());
        } finally {
            FileUtil.del(tempDir);
        }
    }
    
    public GenerateResult generateToDir(String connectionId, List<String> tableNames, 
                                         String basePackage, String author, String outputDir) {
        List<PreviewResult> previews = preview(connectionId, tableNames, basePackage, author);
        
        try {
            for (PreviewResult preview : previews) {
                String filePath = getPackagePath(preview.getType(), basePackage);
                File dir = new File(outputDir, filePath);
                dir.mkdirs();
                
                File file = new File(dir, preview.getFileName());
                Files.write(file.toPath(), preview.getContent().getBytes(StandardCharsets.UTF_8));
            }
            
            GenerateResult result = new GenerateResult();
            result.setSuccess(true);
            result.setMessage("生成成功");
            result.setFilePath(outputDir);
            return result;
        } catch (IOException e) {
            log.error("生成代码失败", e);
            GenerateResult result = new GenerateResult();
            result.setSuccess(false);
            result.setMessage("生成失败: " + e.getMessage());
            return result;
        }
    }
    
    private Map<String, Object> buildDataModel(TableInfo tableInfo, String className, 
                                                String basePackage, String author) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", basePackage);
        dataModel.put("className", className);
        dataModel.put("tableName", tableInfo.getTableName());
        dataModel.put("tableComment", tableInfo.getTableComment());
        dataModel.put("author", author);
        dataModel.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        dataModel.put("columns", tableInfo.getColumns());
        
        List<String> imports = tableInfo.getColumns().stream()
                .map(ColumnInfo::getJavaType)
                .filter(type -> type.contains("."))
                .distinct()
                .collect(Collectors.toList());
        dataModel.put("imports", imports);
        
        Optional<ColumnInfo> pk = tableInfo.getColumns().stream()
                .filter(ColumnInfo::getPrimaryKey)
                .findFirst();
        dataModel.put("primaryKey", pk.orElse(null));
        
        return dataModel;
    }
    
    private String toClassName(String tableName) {
        String[] parts = tableName.split("_");
        return Arrays.stream(parts)
                .map(part -> StrUtil.upperFirst(part.toLowerCase()))
                .collect(Collectors.joining());
    }
    
    private String getFileName(String type, String className) {
        switch (type) {
            case "entity":
                return className + ".java";
            case "mapper":
                return className + "Mapper.java";
            case "service":
                return className + "Service.java";
            case "controller":
                return className + "Controller.java";
            default:
                return className + ".java";
        }
    }
    
    private String getPackagePath(String type, String basePackage) {
        String pkgPath = basePackage.replace('.', '/');
        switch (type) {
            case "entity":
                return pkgPath + "/entity";
            case "mapper":
                return pkgPath + "/mapper";
            case "service":
                return pkgPath + "/service";
            case "controller":
                return pkgPath + "/controller";
            default:
                return pkgPath;
        }
    }
}