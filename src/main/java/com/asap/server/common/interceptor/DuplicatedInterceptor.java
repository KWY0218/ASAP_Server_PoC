package com.asap.server.common.interceptor;


import com.asap.server.common.filter.CustomHttpServletRequestWrapper;
import com.asap.server.exception.Error;
import com.asap.server.exception.model.TooManyRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
@RequiredArgsConstructor
public class DuplicatedInterceptor implements HandlerInterceptor {
    private static final String REDIS_KEY = "ASAP_REDIS";
    private static final String RMAP_VALUE = "ASAP";
    private final RedissonClient redissonClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (lock(request)) return true;
        throw new TooManyRequestException(Error.TOO_MANY_REQUEST_EXCEPTION);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        unLock(request);
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        unLock(request);
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private boolean lock(HttpServletRequest request) {
        final String rmapKey = ((CustomHttpServletRequestWrapper) request).getBody();
        RMap<String, String> redissonClientMap = redissonClient.getMap(REDIS_KEY);
        return redissonClientMap.putIfAbsent(rmapKey, RMAP_VALUE) == null;
    }

    private void unLock(HttpServletRequest request) {
        final String rmapKey = ((CustomHttpServletRequestWrapper) request).getBody();
        RMap<String, String> redissonClientMap = redissonClient.getMap(REDIS_KEY);
        redissonClientMap.remove(rmapKey);
    }
}
