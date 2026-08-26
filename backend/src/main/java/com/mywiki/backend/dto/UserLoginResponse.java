package com.mywiki.backend.dto;

import lombok.Data;

@Data
public class UserLoginResponse {

    private Long id;

    private String username;
    
    private String token;
}
