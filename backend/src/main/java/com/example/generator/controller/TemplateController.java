package com.example.generator.controller;

import com.example.generator.common.Result;
import com.example.generator.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/template")
@RequiredArgsConstructor
public class TemplateController {
    
    private final TemplateService templateService;
    
    @GetMapping("/types")
    public Result<List<String>> getTemplateTypes(@RequestParam(required = false, defaultValue = "mybatis-plus") String ormType) {
        return Result.success(templateService.getTemplateTypes(ormType));
    }
    
    @GetMapping("/{ormType}/{name}")
    public Result<String> get(@PathVariable String ormType, @PathVariable String name) {
        String content = templateService.getTemplate(ormType, name);
        if (content == null) {
            return Result.error(404, "模板不存在");
        }
        return Result.success(content);
    }
    
    @PostMapping("/save")
    public Result<Void> save(@RequestBody Map<String, String> body) {
        String ormType = body.getOrDefault("ormType", TemplateService.ORM_MYBATIS_PLUS);
        String name = body.get("name");
        String content = body.get("content");
        templateService.saveTemplate(ormType, name, content);
        return Result.success();
    }
    
    @PostMapping("/reset/{ormType}/{name}")
    public Result<Void> reset(@PathVariable String ormType, @PathVariable String name) {
        templateService.resetTemplate(ormType, name);
        return Result.success();
    }
}