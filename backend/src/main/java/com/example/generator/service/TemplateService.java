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
import java.util.*;

@Slf4j
@Service
public class TemplateService {
    
    @Value("${generator.template.path:./templates/}")
    private String templatePath;
    
    private Configuration cfg;
    private final ConfigService configService;
    private Map<String, Map<String, String>> customTemplates = new HashMap<>();
    
    private static final Map<String, Map<String, String>> DEFAULT_TEMPLATES = new HashMap<>();
    
    public static final String ORM_MYBATIS_PLUS = "mybatis-plus";
    public static final String ORM_MYBATIS = "mybatis";
    
    public static final List<String> TEMPLATE_TYPES_MYBATIS_PLUS = Arrays.asList(
            "entity", "mapper", "service", "serviceImpl", "controller"
    );
    
    public static final List<String> TEMPLATE_TYPES_MYBATIS = Arrays.asList(
            "entity", "mapper", "mapperXml", "service", "serviceImpl", "controller"
    );
    
    public static final String TEMPLATE_GROUP_COMMON = "common";
    public static final List<String> TEMPLATE_TYPES_EXTRA = Arrays.asList("vo", "dto", "query");
    
    static {
        Map<String, String> mybatisPlusTemplates = new HashMap<>();
        mybatisPlusTemplates.put("entity", 
                "package ${packageName}.entity;\n" +
                "\n" +
                "import com.baomidou.mybatisplus.annotation.*;\n" +
                "<#if enableLombok>\n" +
                "import lombok.Data;\n" +
                "<#if enableChain>\n" +
                "import lombok.experimental.Accessors;\n" +
                "</#if>\n" +
                "</#if>\n" +
                "<#if enableSwagger>\n" +
                "import io.swagger.annotations.ApiModel;\n" +
                "import io.swagger.annotations.ApiModelProperty;\n" +
                "</#if>\n" +
                "<#if serializable>\n" +
                "import java.io.Serializable;\n" +
                "</#if>\n" +
                "<#list imports as import>\n" +
                "import ${import};\n" +
                "</#list>\n" +
                "<#if enumConfigs?? && enumConfigs?size gt 0>\n" +
                "<#list enumConfigs as ec>\n" +
                "import ${packageName}.enums.${ec.enumName};\n" +
                "</#list>\n" +
                "</#if>\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment}\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "<#if enableSwagger>\n" +
                "@ApiModel(description = \"${tableComment}\")\n" +
                "</#if>\n" +
                "<#if enableLombok>\n" +
                "@Data\n" +
                "<#if enableChain>\n" +
                "@Accessors(chain = true)\n" +
                "</#if>\n" +
                "</#if>\n" +
                "@TableName(\"${tableName}\")\n" +
                "public class ${className}<#if serializable> implements Serializable</#if> {\n" +
                "\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "<#list columns as column>\n" +
                "    /**\n" +
                "     * ${column.columnComment!column.fieldName}\n" +
                "     */\n" +
                "    <#if enableSwagger>\n" +
                "    @ApiModelProperty(value = \"${column.columnComment!column.fieldName}\")\n" +
                "    </#if>\n" +
                "    <#if column.primaryKey>\n" +
                "    @TableId(type = IdType.AUTO)\n" +
                "    </#if>\n" +
                "    <#if enumMap?? && enumMap[column.columnName]??>\n" +
                "    private ${enumMap[column.columnName]} ${column.fieldName};\n" +
                "    <#else>\n" +
                "    private ${column.javaType} ${column.fieldName};\n" +
                "    </#if>\n" +
                "</#list>\n" +
                "<#if relations?? && relations?size gt 0>\n" +
                "\n" +
                "    // 关联对象\n" +
                "<#list relations as rel>\n" +
                "    /**\n" +
                "     * 关联${rel.targetTable}\n" +
                "     */\n" +
                "    <#if rel.relationType == 'ONE_TO_ONE'>\n" +
                "    @TableField(exist = false)\n" +
                "    private ${rel.targetTable?cap_first} ${rel.fieldName};\n" +
                "    </#if>\n" +
                "    <#if rel.relationType == 'ONE_TO_MANY'>\n" +
                "    @TableField(exist = false)\n" +
                "    private java.util.List<${rel.targetTable?cap_first}> ${rel.fieldName};\n" +
                "    </#if>\n" +
                "</#list>\n" +
                "</#if>\n" +
                "}\n");
        
        mybatisPlusTemplates.put("mapper",
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
        
        mybatisPlusTemplates.put("service",
                "package ${packageName}.service;\n" +
                "\n" +
                "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
                "import com.baomidou.mybatisplus.extension.service.IService;\n" +
                "import ${packageName}.entity.${className};\n" +
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "import ${packageName}.query.${className}Query;\n" +
                "</#if>\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Service 接口\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "public interface ${className}Service extends IService<${className}> {\n" +
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "\n" +
                "    /**\n" +
                "     * 分页查询\n" +
                "     */\n" +
                "    Page<${className}> queryPage(${className}Query query);\n" +
                "</#if>\n" +
                "<#if customMethods?? && customMethods?size gt 0>\n" +
                "<#list customMethods as method>\n" +
                "\n" +
                "    /**\n" +
                "     * ${method.description!method.methodName}\n" +
                "     */\n" +
                "    <#if method.methodType == 'SINGLE'>${className} ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'LIST'>java.util.List<${className}> ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'PAGE'>Page<${className}> ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'COUNT'>int ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'EXISTS'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'DELETE'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'UPDATE'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "</#list>\n" +
                "</#if>\n" +
                "}\n");
        
        mybatisPlusTemplates.put("serviceImpl",
                "package ${packageName}.service.impl;\n" +
                "\n" +
                "import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;\n" +
                "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
                "import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.mapper.${className}Mapper;\n" +
                "import ${packageName}.service.${className}Service;\n" +
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "import ${packageName}.query.${className}Query;\n" +
                "import cn.hutool.core.util.StrUtil;\n" +
                "</#if>\n" +
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
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "\n" +
                "    @Override\n" +
                "    public Page<${className}> queryPage(${className}Query query) {\n" +
                "        LambdaQueryWrapper<${className}> wrapper = new LambdaQueryWrapper<>();\n" +
                "<#list queryFields as qf>\n" +
                "        <#if qf.queryType == 'EQ'>\n" +
                "        if (query.get${qf.fieldName?cap_first}() != null) {\n" +
                "            wrapper.eq(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "        <#if qf.queryType == 'LIKE'>\n" +
                "        if (StrUtil.isNotBlank(query.get${qf.fieldName?cap_first}())) {\n" +
                "            wrapper.like(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "        <#if qf.queryType == 'LIKE_LEFT'>\n" +
                "        if (StrUtil.isNotBlank(query.get${qf.fieldName?cap_first}())) {\n" +
                "            wrapper.likeLeft(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "        <#if qf.queryType == 'LIKE_RIGHT'>\n" +
                "        if (StrUtil.isNotBlank(query.get${qf.fieldName?cap_first}())) {\n" +
                "            wrapper.likeRight(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "        <#if qf.queryType == 'GT'>\n" +
                "        if (query.get${qf.fieldName?cap_first}() != null) {\n" +
                "            wrapper.gt(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "        <#if qf.queryType == 'GTE'>\n" +
                "        if (query.get${qf.fieldName?cap_first}() != null) {\n" +
                "            wrapper.ge(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "        <#if qf.queryType == 'LT'>\n" +
                "        if (query.get${qf.fieldName?cap_first}() != null) {\n" +
                "            wrapper.lt(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "        <#if qf.queryType == 'LTE'>\n" +
                "        if (query.get${qf.fieldName?cap_first}() != null) {\n" +
                "            wrapper.le(${className}::get${qf.fieldName?cap_first}, query.get${qf.fieldName?cap_first}());\n" +
                "        }\n" +
                "        </#if>\n" +
                "</#list>\n" +
                "<#if defaultSortField?? && defaultSortField?length gt 0>\n" +
                "        wrapper.orderBy<#if defaultSortOrder == 'DESC'>Desc<#else>Asc</#if>(true, ${className}::get${defaultSortField?cap_first});\n" +
                "</#if>\n" +
                "        return page(query.getPage(), wrapper);\n" +
                "    }\n" +
                "</#if>\n" +
                "<#if customMethods?? && customMethods?size gt 0>\n" +
                "<#list customMethods as method>\n" +
                "\n" +
                "    @Override\n" +
                "    public <#if method.methodType == 'SINGLE'>${className} ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>)</#if><#if method.methodType == 'LIST'>java.util.List<${className}> ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>)</#if><#if method.methodType == 'PAGE'>Page<${className}> ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>)</#if><#if method.methodType == 'COUNT'>int ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>)</#if><#if method.methodType == 'EXISTS'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>)</#if><#if method.methodType == 'DELETE'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>)</#if><#if method.methodType == 'UPDATE'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>)</#if> {\n" +
                "        LambdaQueryWrapper<${className}> wrapper = new LambdaQueryWrapper<>();\n" +
                "<#list method.params as param>\n" +
                "        wrapper.eq(${className}::get${param.columnName?cap_first}, ${param.paramName});\n" +
                "</#list>\n" +
                "        <#if method.methodType == 'SINGLE'>return getOne(wrapper);</#if>\n" +
                "        <#if method.methodType == 'LIST'>return list(wrapper);</#if>\n" +
                "        <#if method.methodType == 'PAGE'>return page(new Page<>(), wrapper);</#if>\n" +
                "        <#if method.methodType == 'COUNT'>return (int) count(wrapper);</#if>\n" +
                "        <#if method.methodType == 'EXISTS'>return count(wrapper) > 0;</#if>\n" +
                "        <#if method.methodType == 'DELETE'>return remove(wrapper);</#if>\n" +
                "        <#if method.methodType == 'UPDATE'>return update(new ${className}(), wrapper);</#if>\n" +
                "    }\n" +
                "</#list>\n" +
                "</#if>\n" +
                "}\n");
        
        mybatisPlusTemplates.put("controller",
                "package ${packageName}.controller;\n" +
                "\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.service.${className}Service;\n" +
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "import ${packageName}.query.${className}Query;\n" +
                "</#if>\n" +
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
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "    /**\n" +
                "     * 条件分页查询\n" +
                "     */\n" +
                "    @GetMapping(\"/page\")\n" +
                "    public Page<${className}> queryPage(${className}Query query) {\n" +
                "        return ${className?uncap_first}Service.queryPage(query);\n" +
                "    }\n" +
                "\n" +
                "</#if>\n" +
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
        
        DEFAULT_TEMPLATES.put(ORM_MYBATIS_PLUS, mybatisPlusTemplates);
        
        Map<String, String> mybatisTemplates = new HashMap<>();
        mybatisTemplates.put("entity",
                "package ${packageName}.entity;\n" +
                "\n" +
                "import lombok.Data;\n" +
                "<#list imports as import>\n" +
                "import ${import};\n" +
                "</#list>\n" +
                "<#if enumConfigs?? && enumConfigs?size gt 0>\n" +
                "<#list enumConfigs as ec>\n" +
                "import ${packageName}.enums.${ec.enumName};\n" +
                "</#list>\n" +
                "</#if>\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment}\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Data\n" +
                "public class ${className} {\n" +
                "<#list columns as column>\n" +
                "\n" +
                "    /**\n" +
                "     * ${column.columnComment!column.fieldName}\n" +
                "     */\n" +
                "    <#if enumMap?? && enumMap[column.columnName]??>\n" +
                "    private ${enumMap[column.columnName]} ${column.fieldName};\n" +
                "    <#else>\n" +
                "    private ${column.javaType} ${column.fieldName};\n" +
                "    </#if>\n" +
                "</#list>\n" +
                "<#if relations?? && relations?size gt 0>\n" +
                "\n" +
                "    // 关联对象\n" +
                "<#list relations as rel>\n" +
                "    /**\n" +
                "     * 关联${rel.targetTable}\n" +
                "     */\n" +
                "    <#if rel.relationType == 'ONE_TO_ONE'>\n" +
                "    private ${rel.targetTable?cap_first} ${rel.fieldName};\n" +
                "    </#if>\n" +
                "    <#if rel.relationType == 'ONE_TO_MANY'>\n" +
                "    private java.util.List<${rel.targetTable?cap_first}> ${rel.fieldName};\n" +
                "    </#if>\n" +
                "</#list>\n" +
                "</#if>\n" +
                "}\n");
        
        mybatisTemplates.put("mapper",
                "package ${packageName}.mapper;\n" +
                "\n" +
                "import ${packageName}.entity.${className};\n" +
                "import org.apache.ibatis.annotations.Mapper;\n" +
                "import org.apache.ibatis.annotations.Param;\n" +
                "import java.util.List;\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Mapper 接口\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Mapper\n" +
                "public interface ${className}Mapper {\n" +
                "    \n" +
                "    /**\n" +
                "     * 根据ID查询\n" +
                "     */\n" +
                "    ${className} selectById(@Param(\"id\") Long id);\n" +
                "    \n" +
                "    /**\n" +
                "     * 查询全部\n" +
                "     */\n" +
                "    List<${className}> selectAll();\n" +
                "    \n" +
                "    /**\n" +
                "     * 新增\n" +
                "     */\n" +
                "    int insert(${className} entity);\n" +
                "    \n" +
                "    /**\n" +
                "     * 更新\n" +
                "     */\n" +
                "    int update(${className} entity);\n" +
                "    \n" +
                "    /**\n" +
                "     * 删除\n" +
                "     */\n" +
                "    int deleteById(@Param(\"id\") Long id);\n" +
                "}\n");
        
        mybatisTemplates.put("mapperXml",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n" +
                "<mapper namespace=\"${packageName}.mapper.${className}Mapper\">\n" +
                "    \n" +
                "    <resultMap id=\"BaseResultMap\" type=\"${packageName}.entity.${className}\">\n" +
                "<#list columns as column>\n" +
                "        <#if column.primaryKey><id property=\"${column.fieldName}\" column=\"${column.columnName}\"/><#else><result property=\"${column.fieldName}\" column=\"${column.columnName}\"/></#if>\n" +
                "</#list>\n" +
                "    </resultMap>\n" +
                "    \n" +
                "    <sql id=\"Base_Column_List\">\n" +
                "        <#list columns as column>${column.columnName}<#if column_has_next>, </#if></#list>\n" +
                "    </sql>\n" +
                "    \n" +
                "    <select id=\"selectById\" resultMap=\"BaseResultMap\">\n" +
                "        SELECT <include refid=\"Base_Column_List\"/>\n" +
                "        FROM ${tableName}\n" +
                "        WHERE <#list columns as column><#if column.primaryKey>${column.columnName} = <#noparse>#{id}</#noparse></#if></#list>\n" +
                "    </select>\n" +
                "    \n" +
                "    <select id=\"selectAll\" resultMap=\"BaseResultMap\">\n" +
                "        SELECT <include refid=\"Base_Column_List\"/>\n" +
                "        FROM ${tableName}\n" +
                "    </select>\n" +
                "    \n" +
                "    <insert id=\"insert\" parameterType=\"${packageName}.entity.${className}\">\n" +
                "        INSERT INTO ${tableName} (\n" +
                "            <#list columns as column><#if !column.primaryKey>${column.columnName}<#if column_has_next>, </#if></#if></#list>\n" +
                "        ) VALUES (\n" +
                "            <#list columns as column><#if !column.primaryKey><#noparse>#{</#noparse>${column.fieldName}<#noparse>}</#noparse><#if column_has_next>, </#if></#if></#list>\n" +
                "        )\n" +
                "    </insert>\n" +
                "    \n" +
                "    <update id=\"update\" parameterType=\"${packageName}.entity.${className}\">\n" +
                "        UPDATE ${tableName}\n" +
                "        SET <#list columns as column><#if !column.primaryKey>${column.columnName} = <#noparse>#{</#noparse>${column.fieldName}<#noparse>}</#noparse><#if column_has_next>, </#if></#if></#list>\n" +
                "        WHERE <#list columns as column><#if column.primaryKey>${column.columnName} = <#noparse>#{</#noparse>${column.fieldName}<#noparse>}</#noparse></#if></#list>\n" +
                "    </update>\n" +
                "    \n" +
                "    <delete id=\"deleteById\">\n" +
                "        DELETE FROM ${tableName}\n" +
                "        WHERE <#list columns as column><#if column.primaryKey>${column.columnName} = <#noparse>#{id}</#noparse></#if></#list>\n" +
                "    </delete>\n" +
                "</mapper>\n");
        
        mybatisTemplates.put("service",
                "package ${packageName}.service;\n" +
                "\n" +
                "import ${packageName}.entity.${className};\n" +
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "import ${packageName}.query.${className}Query;\n" +
                "import com.github.pagehelper.Page;\n" +
                "import com.github.pagehelper.PageHelper;\n" +
                "</#if>\n" +
                "import java.util.List;\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Service 接口\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "public interface ${className}Service {\n" +
                "    \n" +
                "    /**\n" +
                "     * 根据ID查询\n" +
                "     */\n" +
                "    ${className} getById(Long id);\n" +
                "    \n" +
                "    /**\n" +
                "     * 查询全部\n" +
                "     */\n" +
                "    List<${className}> listAll();\n" +
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "    \n" +
                "    /**\n" +
                "     * 分页查询\n" +
                "     */\n" +
                "    List<${className}> queryPage(${className}Query query);\n" +
                "</#if>\n" +
                "<#if customMethods?? && customMethods?size gt 0>\n" +
                "<#list customMethods as method>\n" +
                "    \n" +
                "    /**\n" +
                "     * ${method.description!method.methodName}\n" +
                "     */\n" +
                "    <#if method.methodType == 'SINGLE'>${className} ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'LIST'>List<${className}> ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'COUNT'>int ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'EXISTS'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "    <#if method.methodType == 'DELETE'>boolean ${method.methodName}(<#list method.params as param>${param.paramType} ${param.paramName}<#if param_has_next>, </#if></#list>);</#if>\n" +
                "</#list>\n" +
                "</#if>\n" +
                "    \n" +
                "    /**\n" +
                "     * 新增\n" +
                "     */\n" +
                "    boolean save(${className} entity);\n" +
                "    \n" +
                "    /**\n" +
                "     * 更新\n" +
                "     */\n" +
                "    boolean update(${className} entity);\n" +
                "    \n" +
                "    /**\n" +
                "     * 删除\n" +
                "     */\n" +
                "    boolean removeById(Long id);\n" +
                "}\n");
        
        mybatisTemplates.put("serviceImpl",
                "package ${packageName}.service.impl;\n" +
                "\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.mapper.${className}Mapper;\n" +
                "import ${packageName}.service.${className}Service;\n" +
                "import org.springframework.stereotype.Service;\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import java.util.List;\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Service 实现类\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Service\n" +
                "@RequiredArgsConstructor\n" +
                "public class ${className}ServiceImpl implements ${className}Service {\n" +
                "    \n" +
                "    private final ${className}Mapper ${className?uncap_first}Mapper;\n" +
                "    \n" +
                "    @Override\n" +
                "    public ${className} getById(Long id) {\n" +
                "        return ${className?uncap_first}Mapper.selectById(id);\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public List<${className}> listAll() {\n" +
                "        return ${className?uncap_first}Mapper.selectAll();\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public boolean save(${className} entity) {\n" +
                "        return ${className?uncap_first}Mapper.insert(entity) > 0;\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public boolean update(${className} entity) {\n" +
                "        return ${className?uncap_first}Mapper.update(entity) > 0;\n" +
                "    }\n" +
                "    \n" +
                "    @Override\n" +
                "    public boolean removeById(Long id) {\n" +
                "        return ${className?uncap_first}Mapper.deleteById(id) > 0;\n" +
                "    }\n" +
                "}\n");
        
        mybatisTemplates.put("controller",
                "package ${packageName}.controller;\n" +
                "\n" +
                "import ${packageName}.entity.${className};\n" +
                "import ${packageName}.service.${className}Service;\n" +
                "import org.springframework.web.bind.annotation.*;\n" +
                "import lombok.RequiredArgsConstructor;\n" +
                "import java.util.List;\n" +
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
                "     * 查询全部列表\n" +
                "     *\n" +
                "     * @return 列表数据\n" +
                "     */\n" +
                "    @GetMapping(\"/list\")\n" +
                "    public List<${className}> list() {\n" +
                "        return ${className?uncap_first}Service.listAll();\n" +
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
                "        return ${className?uncap_first}Service.update(entity);\n" +
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
        
        DEFAULT_TEMPLATES.put(ORM_MYBATIS, mybatisTemplates);
        
        Map<String, String> commonTemplates = new HashMap<>();
        commonTemplates.put("vo",
                "package ${packageName}.vo;\n" +
                "\n" +
                "<#if enableLombok>\n" +
                "import lombok.Data;\n" +
                "<#if enableChain>\n" +
                "import lombok.experimental.Accessors;\n" +
                "</#if>\n" +
                "</#if>\n" +
                "<#if enableSwagger>\n" +
                "import io.swagger.annotations.ApiModel;\n" +
                "import io.swagger.annotations.ApiModelProperty;\n" +
                "</#if>\n" +
                "<#if serializable>\n" +
                "import java.io.Serializable;\n" +
                "</#if>\n" +
                "<#list imports as import>\n" +
                "import ${import};\n" +
                "</#list>\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} VO\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "<#if enableSwagger>\n" +
                "@ApiModel(description = \"${tableComment} VO\")\n" +
                "</#if>\n" +
                "<#if enableLombok>\n" +
                "@Data\n" +
                "<#if enableChain>\n" +
                "@Accessors(chain = true)\n" +
                "</#if>\n" +
                "</#if>\n" +
                "public class ${className}VO<#if serializable> implements Serializable</#if> {\n" +
                "\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "<#list columns as column>\n" +
                "    /**\n" +
                "     * ${column.columnComment!column.fieldName}\n" +
                "     */\n" +
                "    <#if enableSwagger>\n" +
                "    @ApiModelProperty(value = \"${column.columnComment!column.fieldName}\")\n" +
                "    </#if>\n" +
                "    private ${column.javaType} ${column.fieldName};\n" +
                "</#list>\n" +
                "}\n");
        
        commonTemplates.put("dto",
                "package ${packageName}.dto;\n" +
                "\n" +
                "<#if enableLombok>\n" +
                "import lombok.Data;\n" +
                "<#if enableChain>\n" +
                "import lombok.experimental.Accessors;\n" +
                "</#if>\n" +
                "</#if>\n" +
                "<#if enableSwagger>\n" +
                "import io.swagger.annotations.ApiModel;\n" +
                "import io.swagger.annotations.ApiModelProperty;\n" +
                "</#if>\n" +
                "<#if enableValidation>\n" +
                "import javax.validation.constraints.*;\n" +
                "</#if>\n" +
                "<#if serializable>\n" +
                "import java.io.Serializable;\n" +
                "</#if>\n" +
                "<#list imports as import>\n" +
                "import ${import};\n" +
                "</#list>\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} DTO\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "<#if enableSwagger>\n" +
                "@ApiModel(description = \"${tableComment} DTO\")\n" +
                "</#if>\n" +
                "<#if enableLombok>\n" +
                "@Data\n" +
                "<#if enableChain>\n" +
                "@Accessors(chain = true)\n" +
                "</#if>\n" +
                "</#if>\n" +
                "public class ${className}DTO<#if serializable> implements Serializable</#if> {\n" +
                "\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "<#list columns as column>\n" +
                "    /**\n" +
                "     * ${column.columnComment!column.fieldName}\n" +
                "     */\n" +
                "    <#if enableSwagger>\n" +
                "    @ApiModelProperty(value = \"${column.columnComment!column.fieldName}\")\n" +
                "    </#if>\n" +
                "    <#if enableValidation && !column.nullable && !column.primaryKey>\n" +
                "    @NotNull(message = \"${column.columnComment!column.fieldName}不能为空\")\n" +
                "    </#if>\n" +
                "    private ${column.javaType} ${column.fieldName};\n" +
                "</#list>\n" +
                "}\n");
        
        commonTemplates.put("query",
                "package ${packageName}.query;\n" +
                "\n" +
                "<#if enableLombok>\n" +
                "import lombok.Data;\n" +
                "<#if enableChain>\n" +
                "import lombok.experimental.Accessors;\n" +
                "</#if>\n" +
                "</#if>\n" +
                "<#if enableSwagger>\n" +
                "import io.swagger.annotations.ApiModel;\n" +
                "import io.swagger.annotations.ApiModelProperty;\n" +
                "</#if>\n" +
                "import com.baomidou.mybatisplus.extension.plugins.pagination.Page;\n" +
                "<#if serializable>\n" +
                "import java.io.Serializable;\n" +
                "</#if>\n" +
                "<#list imports as import>\n" +
                "import ${import};\n" +
                "</#list>\n" +
                "\n" +
                "/**\n" +
                " * ${tableComment} Query\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "<#if enableSwagger>\n" +
                "@ApiModel(description = \"${tableComment} Query\")\n" +
                "</#if>\n" +
                "<#if enableLombok>\n" +
                "@Data\n" +
                "<#if enableChain>\n" +
                "@Accessors(chain = true)\n" +
                "</#if>\n" +
                "</#if>\n" +
                "public class ${className}Query<#if serializable> implements Serializable</#if> {\n" +
                "\n" +
                "    private static final long serialVersionUID = 1L;\n" +
                "\n" +
                "    private Integer pageNum = 1;\n" +
                "\n" +
                "    private Integer pageSize = 10;\n" +
                "\n" +
                "    private String orderBy;\n" +
                "\n" +
                "    private Boolean orderAsc = false;\n" +
                "\n" +
                "    public Page<?> getPage() {\n" +
                "        return new Page<>(pageNum, pageSize);\n" +
                "    }\n" +
                "\n" +
                "<#if queryFields?? && queryFields?size gt 0>\n" +
                "<#list queryFields as qf>\n" +
                "    /**\n" +
                "     * ${qf.fieldName}查询条件\n" +
                "     */\n" +
                "    <#if enableSwagger>\n" +
                "    @ApiModelProperty(value = \"${qf.fieldName}\")\n" +
                "    </#if>\n" +
                "    <#if qf.queryType == 'IN' || qf.queryType == 'NOT_IN'>\n" +
                "    private java.util.List<Object> ${qf.fieldName};\n" +
                "    <#elseif qf.queryType == 'BETWEEN'>\n" +
                "    private Object ${qf.fieldName}Start;\n" +
                "    private Object ${qf.fieldName}End;\n" +
                "    <#else>\n" +
                "    private Object ${qf.fieldName};\n" +
                "    </#if>\n" +
                "</#list>\n" +
                "<#else>\n" +
                "<#list columns as column>\n" +
                "    /**\n" +
                "     * ${column.columnComment!column.fieldName}\n" +
                "     */\n" +
                "    <#if enableSwagger>\n" +
                "    @ApiModelProperty(value = \"${column.columnComment!column.fieldName}\")\n" +
                "    </#if>\n" +
                "    private ${column.javaType} ${column.fieldName};\n" +
                "</#list>\n" +
                "</#if>\n" +
                "}\n");
        
        commonTemplates.put("enum",
                "package ${packageName};\n" +
                "\n" +
                "import com.baomidou.mybatisplus.annotation.EnumValue;\n" +
                "import com.fasterxml.jackson.annotation.JsonValue;\n" +
                "import lombok.Getter;\n" +
                "\n" +
                "/**\n" +
                " * ${enumName} 枚举\n" +
                " *\n" +
                " * @author ${author}\n" +
                " * @date ${date}\n" +
                " */\n" +
                "@Getter\n" +
                "public enum ${enumName} {\n" +
                "<#if values??>\n" +
                "<#list values?keys as key>\n" +
                "    ${values[key]}(${key}<#if descriptions?? && descriptions[key]??>, \"${descriptions[key]}\"</#if>)<#if key_has_next>,<#else>;</#if>\n" +
                "</#list>\n" +
                "</#if>\n" +
                "\n" +
                "    @EnumValue\n" +
                "    @JsonValue\n" +
                "    private final Integer code;\n" +
                "\n" +
                "    private final String desc;\n" +
                "\n" +
                "    ${enumName}(Integer code, String desc) {\n" +
                "        this.code = code;\n" +
                "        this.desc = desc;\n" +
                "    }\n" +
                "}\n");
        
        DEFAULT_TEMPLATES.put(TEMPLATE_GROUP_COMMON, commonTemplates);
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
        }
        
        cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setDirectoryForTemplateLoading(templateDir);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        loadCustomTemplates();
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
    
    public List<String> getTemplateTypes(String ormType) {
        if (ORM_MYBATIS.equals(ormType)) {
            return TEMPLATE_TYPES_MYBATIS;
        }
        return TEMPLATE_TYPES_MYBATIS_PLUS;
    }
    
    public List<String> getExtraTemplateTypes() {
        return TEMPLATE_TYPES_EXTRA;
    }
    
    public String getExtraTemplate(String templateName) {
        return getTemplate(TEMPLATE_GROUP_COMMON, templateName);
    }
    
    public String renderExtraTemplate(String templateName, Map<String, Object> dataModel) throws Exception {
        return render(TEMPLATE_GROUP_COMMON, templateName, dataModel);
    }
    
    public String getTemplate(String ormType, String templateName) {
        Map<String, String> customOrmTemplates = customTemplates.get(ormType);
        if (customOrmTemplates != null && customOrmTemplates.containsKey(templateName)) {
            return customOrmTemplates.get(templateName);
        }
        
        Map<String, String> templates = DEFAULT_TEMPLATES.get(ormType);
        if (templates != null && templates.containsKey(templateName)) {
            return templates.get(templateName);
        }
        
        return null;
    }
    
    public void saveTemplate(String ormType, String templateName, String content) {
        Map<String, String> ormTemplates = customTemplates.computeIfAbsent(ormType, k -> new HashMap<>());
        ormTemplates.put(templateName, content);
        
        try {
            File customFile = new File(templatePath, "custom_templates.json");
            Files.write(customFile.toPath(), JSONUtil.toJsonPrettyStr(customTemplates).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("保存模板失败: {}", templateName, e);
            throw new RuntimeException("保存模板失败", e);
        }
    }
    
    public void resetTemplate(String ormType, String templateName) {
        Map<String, String> ormTemplates = customTemplates.get(ormType);
        if (ormTemplates != null) {
            ormTemplates.remove(templateName);
        }
        
        try {
            File customFile = new File(templatePath, "custom_templates.json");
            Files.write(customFile.toPath(), JSONUtil.toJsonPrettyStr(customTemplates).getBytes(StandardCharsets.UTF_8));
            log.info("模板 {}.{} 已重置为默认", ormType, templateName);
        } catch (IOException e) {
            log.error("重置模板失败: {}", templateName, e);
        }
    }
    
    public String render(String ormType, String templateName, Map<String, Object> dataModel) throws Exception {
        String templateContent = getTemplate(ormType, templateName);
        if (templateContent == null) {
            throw new RuntimeException("模板不存在: " + ormType + "/" + templateName);
        }
        
        freemarker.template.Template template = new freemarker.template.Template(templateName, new StringReader(templateContent), cfg);
        StringWriter writer = new StringWriter();
        template.process(dataModel, writer);
        return writer.toString();
    }
}