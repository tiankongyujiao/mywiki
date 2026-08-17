package com.mywiki.backend.controller;

import com.mywiki.backend.dto.UserRegisterDTO;
import com.mywiki.backend.service.UserService;
import com.mywiki.backend.common.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<?> register(
            @Valid @RequestBody UserRegisterDTO dto
    ) {

        userService.register(dto);

        return Result.success(null);
    }
}
