package com.sunjintong.secureservice.common.security;

public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh");
    private String type;
    TokenType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
}