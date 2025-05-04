package com.yilena.service.filter;

import com.yilena.service.constant.MqConstant;
import com.yilena.service.constant.StatusConstant;
import com.yilena.service.dao.UserMapper;
import com.yilena.service.entity.po.User;
import com.yilena.service.entity.vo.UserVO;
import com.yilena.service.utils.CurrentHolder;
import com.yilena.service.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;

/**
 * 自定义过滤器用于鉴权
 * 过滤所有请求以验证token的有效性，并设置当前用户信息
 */
@Slf4j
@WebFilter(urlPatterns = "/*")
@RequiredArgsConstructor
public class GlobalFilter implements Filter {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("进入过滤器");
        // 将ServletRequest和ServletResponse转换为HttpServletRequest和HttpServletResponse
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取请求的URL
        String url = request.getRequestURI();

        // 如果是登录请求，直接放行
        if(url.contains("/user/login") || url.contains("/search/")
                || url.contains("/user/register")
                || url.contains("/manager/login")
                || url.contains("/danmu")){
            log.info("直接放行");
            filterChain.doFilter(request,response);
            return;
        }

        String token = null;

        // 如果是websocket连接，token是作为路径参数传递的
        if(url.contains("/YVW/")){
            token = request.getParameter("token");
        }else{
            // 获取请求头中的token
            token = request.getHeader("token");
        }

        // 如果token为空，返回401状态码
        if(token == null || token.isEmpty()){
            log.info("token为空");
            response.setStatus(401);
            return;
        }

        try {
            // 解析token，获取用户信息
            Claims claims =JwtUtils.parseToken(token);
            Long id = Long.valueOf(claims.get("id").toString());
            // 将当前用户信息设置到ThreadLocal中
            CurrentHolder.setCurrent(id);
            log.info("token解析成功");

            // 发送消息给MQ检测当前用户是否被冻结
            UserVO userVO = new UserVO();
            userVO.setId(id);
            userVO.setStatus(MqConstant.SEND_QUERY);
            rabbitTemplate.convertAndSend(MqConstant.CRUD_EXCHANGE,MqConstant.USER_BINDING_KEY,userVO);
        } catch (Exception e) {
            // 如果token解析失败，返回401状态码
            log.info("token解析失败");
            response.setStatus(401);
            return;
        }
        // 继续执行下一个过滤器或目标资源
        filterChain.doFilter(request,response);
        // 移除ThreadLocal中的当前用户信息
        CurrentHolder.removeCurrent();
    }
}

