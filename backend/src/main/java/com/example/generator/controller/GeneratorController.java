package com.example.generator.controller;

import com.example.generator.common.Result;
import com.example.generator.model.PreviewResult;
import com.example.generator.model.GenerateResult;
import com.example.generator.model.GenerateRequest;
import com.example.generator.service.GeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generate")
@RequiredArgsConstructor
public class GeneratorController {
    
    private final GeneratorService generatorService;
    
    @PostMapping("/preview")
    public Result<List<PreviewResult>> preview(@RequestBody GenerateRequest request) {
        return Result.success(generatorService.preview(
                request.getConnectionId(),
                request.getTableNames(),
                request.getBasePackage(),
                request.getAuthor()
        ));
    }
    
    @PostMapping("/export")
    public ResponseEntity<byte[]> export(@RequestBody GenerateRequest request) {
        byte[] zip = generatorService.exportZip(
                request.getConnectionId(),
                request.getTableNames(),
                request.getBasePackage(),
                request.getAuthor()
        );
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=code.zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zip);
    }
    
    @PostMapping("/to-dir")
    public Result<GenerateResult> generateToDir(@RequestBody GenerateRequest request) {
        return Result.success(generatorService.generateToDir(
                request.getConnectionId(),
                request.getTableNames(),
                request.getBasePackage(),
                request.getAuthor(),
                request.getOutputPath()
        ));
    }
}