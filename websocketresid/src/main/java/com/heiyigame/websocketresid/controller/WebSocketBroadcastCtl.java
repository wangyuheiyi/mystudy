package com.heiyigame.websocketresid.controller;
import com.heiyigame.websocketresid.beans.RequestMessage;
import com.heiyigame.websocketresid.beans.ResponseMessage;
import com.heiyigame.websocketresid.utils.LogUtil;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author admin
 */
@Controller
public class WebSocketBroadcastCtl {
    private AtomicInteger count = new AtomicInteger(0);
    @MessageMapping("/receive")
    @SendToUser("/exchange/mytest/getMqResponse")
    public ResponseMessage broadcast(RequestMessage requestMessage){
        LogUtil.mygame.info("receive info ========="+requestMessage.getMessageInfo());
        return new ResponseMessage("BroadcastCtl receive [" + count.incrementAndGet() + "] records","BroadcastCtl receiveMessage [" + requestMessage.getMessageInfo() + "] records");
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
