package com.heiyigame.websocketresid.interceptor;

import com.heiyigame.websocketresid.beans.MyPrincipal;
import com.heiyigame.websocketresid.utils.LogUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;

/**
 * @author admin
 * 处理websocket请求
 */
@Component
public class MyPrincipalHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        HttpSession httpSession = getSession(request);
        String user = (String)httpSession.getAttribute("loginName");

        if(StringUtils.isEmpty(user)){
            LogUtil.mygame.error("未登录系统，禁止登录websocket!");
            return null;
        }
        LogUtil.mygame.info(" MyDefaultHandshakeHandler login = " + user);
        return new MyPrincipal(user);
    }

    @Nullable
    private HttpSession getSession(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverRequest = (ServletServerHttpRequest)request;
            return serverRequest.getServletRequest().getSession(false);
        } else {
            return null;
        }
    }
}
