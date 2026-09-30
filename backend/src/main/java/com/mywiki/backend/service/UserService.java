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
import com.mywiki.backend.dto.UserLoginDTO;
import com.mywiki.backend.dto.UserLoginResponse;
import com.mywiki.backend.util.JwtUtil;
import com.mywiki.backend.vo.UserVO;

@Service
public class UserService {

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public UserService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        user.setRole("USER");

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.USERNAME_EXIST);
        }
    }


    public UserLoginResponse login(UserLoginDTO dto) {

        LambdaQueryWrapper<User> wrapper =
                new LambdaQueryWrapper<>();

        wrapper.eq(
                User::getUsername,
                dto.getUsername()
        );

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        boolean matches =
                passwordEncoder.matches(
                        dto.getPassword(),
                        user.getPassword()
                );

        if (!matches) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        UserLoginResponse response =
                new UserLoginResponse();

        response.setId(user.getId());

        response.setUsername(user.getUsername());

        String token =
                jwtUtil.generateToken(
                        user.getId(),
                        user.getUsername()
                );

        response.setToken(token);

        return response;
    }

    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
                throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        UserVO vo = new UserVO();

        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRole(user.getRole());
        return vo;
    }
}
