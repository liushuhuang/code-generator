package com.example.generator.model;

import lombok.Data;
import java.util.List;

@Data
public class AppConfig {
    private List<ConnectionConfig> connections;
    private GeneratorConfig settings;
}