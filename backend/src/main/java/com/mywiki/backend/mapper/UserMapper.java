package com.mywiki.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mywiki.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
