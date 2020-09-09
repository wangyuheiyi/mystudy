package com.shanggame.websocketresid.controller;
import com.shanggame.websocketresid.beans.RequestMessage;
import com.shanggame.websocketresid.beans.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author admin
 */
@Controller
@Slf4j
public class WebSocketBroadcastCtl {
    private AtomicInteger count = new AtomicInteger(0);
    @SendToUser("/exchange/mytest/getMqResponse")
    public ResponseMessage broadcast(RequestMessage requestMessage){
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setResMessage("BroadcastCtl receive [" + count.incrementAndGet() + "] records");
        responseMessage.setResDesc("BroadcastCtl receiveMessage [" + requestMessage.getMessageInfo() + "] records");
        return responseMessage;
    }

    /**
     * 模拟登录
     * @param request
     * @param username
     * @param password
     * @return
     */
    @RequestMapping(value = "/loginIn", method = RequestMethod.POST)
    @ResponseBody
    public String login(HttpServletRequest request, @RequestParam(required=false) String username, String password){
        HttpSession httpSession = request.getSession();
        // 如果登录成功，则保存到会话中
        httpSession.setAttribute("loginName", username);
        return "Welcome to mytest";
    }
}
