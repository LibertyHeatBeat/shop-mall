package com.buka.interceptor;

import com.buka.util.JWTUtil;
import com.buka.util.JsonData;
import com.buka.util.CommonUtil;
import com.buka.enums.BizCodeEnum;
import com.buka.vo.LoginUser;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author lhb
 * @version 1.0
 * @description: 登录拦截器
 * @date 2025/2/16 下午3:10
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("token");
        if (token==null){
            token = request.getParameter("token");
        }

        if (token!=null) {
            //验证token
            Claims claims = JWTUtil.checkJWT(token);
            if (claims != null) {
                //将用户信息放入ThreadLocal
                LoginUser loginUser = new LoginUser();
                loginUser.setId(claims.get("id", Integer.class).longValue());
                loginUser.setName(claims.get("name", String.class));
                loginUser.setHeadImg(claims.get("headImg", String.class));
                loginUser.setMail(claims.get("mail",String.class));
                threadLocal.set(loginUser);
                return true;
            }

        }

        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.NOT_LOGIN));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
