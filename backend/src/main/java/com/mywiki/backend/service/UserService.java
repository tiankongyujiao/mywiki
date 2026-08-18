package com.mywiki.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mywiki.backend.common.ErrorCode;
import com.mywiki.backend.dto.UserRegisterDTO;
import com.mywiki.backend.entity.User;
import com.mywiki.backend.exception.BusinessException;
import com.mywiki.backend.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

@Service
public class UserService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(UserRegisterDTO dto) {

        LambdaQueryWrapper<User> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(
                User::getUsername,
                dto.getUsername()
        );

        User existUser =
                userMapper.selectOne(wrapper);

        if (existUser != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXIST);
        }

        User user = new User();

        user.setUsername(dto.getUsername());

        user.setPassword(
                passwordEncoder.encode(dto.getPassword())
        );

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.USERNAME_EXIST);
        }
    }
}
