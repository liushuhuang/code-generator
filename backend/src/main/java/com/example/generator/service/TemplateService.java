package com.example.generator.service;

import cn.hutool.json.JSONUtil;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TemplateService {
    
    @Value("${generator.template.path:./templates/}")
    private String templatePath;
    
    private Configuration cfg;
    private final ConfigService configService;
    private Map<String, String> customTemplates = new HashMap<>();
    
    private static final Map<String, String> DEFAULT_TEMPLATES = new HashMap<>();
    
    static {
        DEFAULT_TEMPLATES.put("entity", 
                "package ${packageName}.entity;\n" +
                "\n" +
                "import com.baomidou.mybatisplus.annotation.*;\n" +
                "import lombok.Data;\n" +
                "<#list imports as import>\n" +
                "import ${import};\n" +
                "</#list>\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment}\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Data\n" +
                "@TableName(\"${tableName}\")\n" +
                "public class ${className} {\n" +
                "<#list columns as column>\n" +
                "\n" +
                "    /**\n" +
                "     * ${column.columnComment!column.fieldName}\n" +
                "     */\n" +
                "    <#if column.primaryKey>\n" +
                "    @TableId(type = IdType.AUTO)\n" +
                "    </#if>\n" +
                "    private ${column.javaType} ${column.fieldName};\n" +
                "</#list>\n" +
                "}\n");
        
        DEFAULT_TEMPLATES.put("mapper",
                "package ${packageName}.mapper;\n" +
                "\n" +
                "import com.baomidou.mybatisplus.core.mapper.BaseMapper;\n" +
                "import ${packageName}.entity.${className};\n" +
                "import org.apache.ibatis.annotations.Mapper;\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Mapper 接口\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Mapper\n" +
                "public interface ${className}Mapper extends BaseMapper<${className}> {\n" +
                "}\n");
        
        DEFAULT_TEMPLATES.put("service",
                "package ${packageName}.service;\n" +
                "\n" +
                "import com.baomidou.mybatisplus.extension.service.IService;\n" +
                "import ${packageName}.entity.${className};\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Service 接口\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "public interface ${className}Service extends IService<${className}> {\n" +
                "}\n");
        
        DEFAULT_TEMPLATES.put("serviceImpl",
                "package ${packageName}.service.impl;\n" +
                "\n" +
                "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.mapper.${className}Mapper;\n" +
                "import ${packageName}.service.${className}Service;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Service 实现类\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Service\n" +
                "public class ${className}ServiceImpl extends ServiceImpl<${className}Mapper, ${className}> implements ${className}Service {\n" +
                "}\n");
        
        DEFAULT_TEMPLATES.put("controller",
                "package ${packageName}.controller;\n" +
                "\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.service.${className}Service;\n" +
                "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Controller\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@RestController\n" +
                "@RequestMapping(\"/${className?lower_case}\")\n" +
                "@RequiredArgsConstructor\n" +
                "public class ${className}Controller {\n" +
                "    \n" +
                "    private final ${className}Service ${className?uncap_first}Service;\n" +
                "    \n" +
                "    /**\n" +
                "     * 分页查询列表\n" +
                "     *\n" +
                "     * @param page 分页参数\n" +
                "     * @return 分页结果\n" +
                "     */\n" +
                "    @GetMapping(\"/list\")\n" +
                "    public Page<${className}> list(Page<${className}> page) {\n" +
                "        return ${className?uncap_first}Service.page(page);\n" +
                "    }\n" +
                "    \n" +
                "    /**\n" +
                "     * 根据ID查询详情\n" +
                "     *\n" +
                "     * @param id 主键ID\n" +
                "     * @return 实体对象\n" +
                "     */\n" +
                "    @GetMapping(\"/{id}\")\n" +
                "    public ${className} getById(@PathVariable Long id) {\n" +
                "        return ${className?uncap_first}Service.getById(id);\n" +
                "    }\n" +
                "    \n" +
                "    /**\n" +
                "     * 新增数据\n" +
                "     *\n" +
                "     * @param entity 实体对象\n" +
                "     * @return 是否成功\n" +
                "     */\n" +
                "    @PostMapping\n" +
                "    public boolean save(@RequestBody ${className} entity) {\n" +
                "        return ${className?uncap_first}Service.save(entity);\n" +
                "    }\n" +
                "    \n" +
                "    /**\n" +
                "     * 更新数据\n" +
                "     *\n" +
                "     * @param entity 实体对象\n" +
                "     * @return 是否成功\n" +
                "     */\n" +
                "    @PutMapping\n" +
                "    public boolean update(@RequestBody ${className} entity) {\n" +
                "        return ${className?uncap_first}Service.updateById(entity);\n" +
                "    }\n" +
                "    \n" +
                "    /**\n" +
                "     * 删除数据\n" +
                "     *\n" +
                "     * @param id 主键ID\n" +
                "     * @return 是否成功\n" +
                "     */\n" +
                "    @DeleteMapping(\"/{id}\")\n" +
                "    public boolean delete(@PathVariable Long id) {\n" +
                "        return ${className?uncap_first}Service.removeById(id);\n" +
                "    }\n" +
                "}\n");
    }
    
    public TemplateService(ConfigService configService) {
        this.configService = configService;
    }
    
    public ConfigService getConfigService() {
        return configService;
    }
    
    @PostConstruct
    public void init() throws IOException {
        File templateDir = new File(templatePath);
        if (!templateDir.exists()) {
            templateDir.mkdirs();
            createDefaultTemplates();
        }
        
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDirectoryForTemplateLoading(templateDir);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        loadCustomTemplates();
    }
    
    private void createDefaultTemplates() {
        try {
            for (Map.Entry<String, String> entry : DEFAULT_TEMPLATES.entrySet()) {
                String fileName = entry.getKey() + ".ftl";
                Files.write(new File(templatePath, fileName).toPath(), 
                        entry.getValue().getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            log.error("创建默认模板失败", e);
        }
    }
    
    private void loadCustomTemplates() {
        File customFile = new File(templatePath, "custom_templates.json");
        if (customFile.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(customFile.toPath());
                String content = new String(bytes, StandardCharsets.UTF_8);
                customTemplates = JSONUtil.toBean(content, Map.class);
            } catch (IOException e) {
                log.error("加载自定义模板失败", e);
            }
        }
    }
    
    public List<String> listTemplates() {
        return Arrays.asList("entity", "mapper", "service", "serviceImpl", "controller");
    }
    
    public String getTemplate(String name) {
        if (customTemplates.containsKey(name)) {
            return customTemplates.get(name);
        }
        
        // 优先从默认模板获取
        if (DEFAULT_TEMPLATES.containsKey(name)) {
            return DEFAULT_TEMPLATES.get(name);
        }
        
        try {
            File file = new File(templatePath, name + ".ftl");
            if (file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("读取模板失败: {}", name, e);
        }
        return null;
    }
    
    public void saveTemplate(String name, String content) {
        customTemplates.put(name, content);
        
        try {
            File customFile = new File(templatePath, "custom_templates.json");
            Files.write(customFile.toPath(), JSONUtil.toJsonPrettyStr(customTemplates).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("保存模板失败: {}", name, e);
            throw new RuntimeException("保存模板失败", e);
        }
    }
    
    public void resetTemplate(String name) {
        // 从自定义模板中移除
        customTemplates.remove(name);
        
        // 重新写入默认模板到文件
        if (DEFAULT_TEMPLATES.containsKey(name)) {
            try {
                File file = new File(templatePath, name + ".ftl");
                Files.write(file.toPath(), DEFAULT_TEMPLATES.get(name).getBytes(StandardCharsets.UTF_8));
                
                // 保存自定义模板配置
                File customFile = new File(templatePath, "custom_templates.json");
                Files.write(customFile.toPath(), JSONUtil.toJsonPrettyStr(customTemplates).getBytes(StandardCharsets.UTF_8));
                
                log.info("模板 {} 已重置为默认", name);
            } catch (IOException e) {
                log.error("重置模板失败: {}", name, e);
                throw new RuntimeException("重置模板失败", e);
            }
        }
    }
    
    public String render(String templateName, Map<String, Object> dataModel) throws Exception {
        String templateContent = getTemplate(templateName);
        if (templateContent == null) {
            throw new RuntimeException("模板不存在: " + templateName);
        }
        
        freemarker.template.Template template = new freemarker.template.Template(templateName, new StringReader(templateContent), cfg);
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        return writer.toString();
    }
}