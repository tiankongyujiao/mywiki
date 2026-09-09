package com.mywiki.backend.resolver;

import com.mywiki.backend.annotation.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.mywiki.backend.exception.BusinessException;
import com.mywiki.backend.common.ErrorCode;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final HttpServletRequest request;

    public CurrentUserArgumentResolver(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Object userId = request.getAttribute("userId");

        if(userId == null){
            throw new BusinessException(
                ErrorCode.USER_NOT_FOUND
            );
        }

        return userId;
    }
}
