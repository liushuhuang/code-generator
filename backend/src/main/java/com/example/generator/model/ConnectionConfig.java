package com.example.generator.model;

import lombok.Data;

@Data
public class ConnectionConfig {
    private String id;
    private String name;
    private String host;
    private Integer port;
    private String database;
    private String username;
    private String password;
}