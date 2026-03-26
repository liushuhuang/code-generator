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
    
    public List<PreviewResult> preview(GenerateRequest request) {
        List<PreviewResult> results = new ArrayList<>();
        
        String ormType = request.getOrmType();
        if (ormType == null || ormType.isEmpty()) {
            ormType = TemplateService.ORM_MYBATIS_PLUS;
        }
        
        Map<String, TableGenerateConfig> configMap = new HashMap<>();
        if (request.getTableConfigs() != null) {
            for (TableGenerateConfig config : request.getTableConfigs()) {
                configMap.put(config.getTableName(), config);
            }
        }
        
        for (String tableName : request.getTableNames()) {
            ConnectionConfig conn = templateService.getConfigService().getConnection(request.getConnectionId());
            if (conn == null) {
                throw new RuntimeException("连接配置不存在");
            }
            
            TableInfo tableInfo = databaseService.getTableInfo(conn, tableName);
            if (tableInfo == null) {
                continue;
            }
            
            String className = toClassName(tableName, request);
            
            List<ColumnInfo> allColumns = tableInfo.getColumns();
            Map<String, Object> dataModel = buildDataModel(tableInfo, className, request, allColumns);
            
            List<String> templateTypes = templateService.getTemplateTypes(ormType);
            
            for (String templateName : templateTypes) {
                try {
                    String content = templateService.render(ormType, templateName, dataModel);
                    String fileName = getFileName(templateName, className);
                    
                    PreviewResult result = new PreviewResult();
                    result.setFileName(fileName);
                    result.setContent(content);
                    result.setType(templateName);
                    result.setTemplateGroup(ormType);
                    results.add(result);
                } catch (Exception e) {
                    log.error("渲染模板失败: {}", templateName, e);
                }
            }
            
            TableGenerateConfig config = configMap.get(tableName);
            if (config != null) {
                List<ColumnInfo> selectedColumns = filterColumns(allColumns, config.getSelectedFields());
                
                if (config.isGenerateVO()) {
                    generateExtraFile(results, className, "vo", "VO", request, selectedColumns, tableInfo);
                }
                if (config.isGenerateDTO()) {
                    generateExtraFile(results, className, "dto", "DTO", request, selectedColumns, tableInfo);
                }
                if (config.isGenerateQuery()) {
                    generateExtraFile(results, className, "query", "Query", request, selectedColumns, tableInfo);
                }
            }
        }
        
        return results;
    }
    
    private void generateExtraFile(List<PreviewResult> results, String className, 
                                    String templateName, String suffix,
                                    GenerateRequest request,
                                    List<ColumnInfo> columns, TableInfo tableInfo) {
        try {
            Map<String, Object> dataModel = buildDataModel(tableInfo, className, request, columns);
            String content = templateService.renderExtraTemplate(templateName, dataModel);
            String fileName = className + suffix + ".java";
            
            PreviewResult result = new PreviewResult();
            result.setFileName(fileName);
            result.setContent(content);
            result.setType(templateName);
            result.setTemplateGroup(TemplateService.TEMPLATE_GROUP_COMMON);
            results.add(result);
        } catch (Exception e) {
            log.error("渲染模板失败: {}", templateName, e);
        }
    }
    
    private List<ColumnInfo> filterColumns(List<ColumnInfo> allColumns, List<String> selectedFields) {
        if (selectedFields == null || selectedFields.isEmpty()) {
            return allColumns;
        }
        return allColumns.stream()
                .filter(col -> selectedFields.contains(col.getColumnName()))
                .collect(Collectors.toList());
    }
    
    public byte[] exportZip(GenerateRequest request) {
        List<PreviewResult> previews = preview(request);
        
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "codegen_" + IdUtil.simpleUUID());
        tempDir.mkdirs();
        
        try {
            for (PreviewResult preview : previews) {
                String filePath = getPackagePath(preview.getType(), request.getBasePackage());
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
    
    public GenerateResult generateToDir(GenerateRequest request) {
        List<PreviewResult> previews = preview(request);
        
        try {
            for (PreviewResult preview : previews) {
                String filePath = getPackagePath(preview.getType(), request.getBasePackage());
                File dir = new File(request.getOutputPath(), filePath);
                dir.mkdirs();
                
                File file = new File(dir, preview.getFileName());
                Files.write(file.toPath(), preview.getContent().getBytes(StandardCharsets.UTF_8));
            }
            
            GenerateResult result = new GenerateResult();
            result.setSuccess(true);
            result.setMessage("生成成功");
            result.setFilePath(request.getOutputPath());
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
                                                GenerateRequest request,
                                                List<ColumnInfo> columns) {
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("packageName", request.getBasePackage());
        dataModel.put("className", className);
        dataModel.put("tableName", tableInfo.getTableName());
        dataModel.put("tableComment", tableInfo.getTableComment());
        dataModel.put("author", request.getAuthor());
        dataModel.put("date", new java.text.SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        dataModel.put("columns", columns);
        
        // 策略配置
        dataModel.put("enableLombok", Boolean.TRUE.equals(request.getEnableLombok()));
        dataModel.put("enableSwagger", Boolean.TRUE.equals(request.getEnableSwagger()));
        dataModel.put("enableValidation", Boolean.TRUE.equals(request.getEnableValidation()));
        dataModel.put("enableChain", Boolean.TRUE.equals(request.getEnableChain()));
        dataModel.put("serializable", Boolean.TRUE.equals(request.getSerializable()));
        dataModel.put("dateType", request.getDateType() != null ? request.getDateType() : "LocalDateTime");
        
        dataModel.put("entityPrefix", request.getEntityPrefix() != null ? request.getEntityPrefix() : "");
        dataModel.put("entitySuffix", request.getEntitySuffix() != null ? request.getEntitySuffix() : "");
        dataModel.put("namingStrategy", request.getNamingStrategy() != null ? request.getNamingStrategy() : "camelCase");
        
        List<String> imports = columns.stream()
                .map(ColumnInfo::getJavaType)
                .filter(type -> type.contains("."))
                .distinct()
                .collect(Collectors.toList());
        dataModel.put("imports", imports);
        
        // 根据日期类型添加导入
        String dateType = (String) dataModel.get("dateType");
        if ("Date".equals(dateType)) {
            imports.add("java.util.Date");
        } else if ("LocalDateTime".equals(dateType)) {
            if (!imports.contains("java.time.LocalDateTime")) {
                imports.add("java.time.LocalDateTime");
            }
        }
        
        Optional<ColumnInfo> pk = columns.stream()
                .filter(ColumnInfo::getPrimaryKey)
                .findFirst();
        dataModel.put("primaryKey", pk.orElse(null));
        
        return dataModel;
    }
    
    private String toClassName(String tableName, GenerateRequest request) {
        String name = tableName;
        
        String removePrefix = request.getRemoveTablePrefix();
        if (removePrefix != null && !removePrefix.isEmpty()) {
            String[] prefixes = removePrefix.split(",");
            for (String prefix : prefixes) {
                prefix = prefix.trim();
                if (!prefix.isEmpty() && name.toLowerCase().startsWith(prefix.toLowerCase())) {
                    name = name.substring(prefix.length());
                    break;
                }
            }
        }
        
        String[] parts = name.split("_");
        String baseClassName = Arrays.stream(parts)
                .map(part -> StrUtil.upperFirst(part.toLowerCase()))
                .collect(Collectors.joining());
        
        String prefix = request.getEntityPrefix();
        String suffix = request.getEntitySuffix();
        if (prefix == null) prefix = "";
        if (suffix == null) suffix = "";
        
        return prefix + baseClassName + suffix;
    }
    
    private String getFileName(String type, String className) {
        switch (type) {
            case "entity":
                return className + ".java";
            case "mapper":
                return className + "Mapper.java";
            case "mapperXml":
                return className + "Mapper.xml";
            case "service":
                return className + "Service.java";
            case "serviceImpl":
                return className + "ServiceImpl.java";
            case "controller":
                return className + "Controller.java";
            case "vo":
                return className + "VO.java";
            case "dto":
                return className + "DTO.java";
            case "query":
                return className + "Query.java";
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
            case "mapperXml":
                return "resources/mapper";
            case "service":
                return pkgPath + "/service";
            case "serviceImpl":
                return pkgPath + "/service/impl";
            case "controller":
                return pkgPath + "/controller";
            case "vo":
                return pkgPath + "/vo";
            case "dto":
                return pkgPath + "/dto";
            case "query":
                return pkgPath + "/query";
            default:
                return pkgPath;
        }
    }
}