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
    
    @GetMapping("/list")
    public Result<List<String>> list() {
        return Result.success(templateService.listTemplates());
    }
    
    @GetMapping("/{name}")
    public Result<String> get(@PathVariable String name) {
        String content = templateService.getTemplate(name);
        if (content == null) {
            return Result.error(404, "模板不存在");
        }
        return Result.success(content);
    }
    
    @PostMapping("/save")
    public Result<Void> save(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String content = body.get("content");
        templateService.saveTemplate(name, content);
        return Result.success();
    }
    
    @PostMapping("/reset/{name}")
    public Result<Void> reset(@PathVariable String name) {
        templateService.resetTemplate(name);
        return Result.success();
    }
}