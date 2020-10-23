package com.heiyigame.websocketresid.controller;
import com.alibaba.fastjson.JSON;
import com.heiyigame.websocketresid.beans.RequestMessage;
import com.heiyigame.websocketresid.beans.ResponseMessage;
import com.heiyigame.websocketresid.reactiveutil.RedisReactiveUtil;
import com.heiyigame.websocketresid.utils.LogUtil;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author admin
 */
@Controller
public class SendBroadcastCtl {
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private RedisReactiveUtil redisReactiveUtil;

    @RequestMapping(value = "/sendInfo", method = RequestMethod.GET)
    @ResponseBody
    public String sendInfo(HttpServletRequest request, @RequestParam(name="message",required=false) String message){
        HttpSession httpSession = request.getSession();
        String userName = (String)httpSession.getAttribute("loginName");
        LogUtil.mygame.info("send info ========="+userName);
        ResponseMessage requestMessage=new ResponseMessage("BroadcastCtl receive [" + userName + "] records","BroadcastCtl receiveMessage [" + message + "] records");
        /** 获取用户的链接id*/
        redisReactiveUtil.get(userName).subscribe(sessionId->{
            String routingKey=getTopicRoutingKey("getMqResponse",String.valueOf(sessionId));
            /** 第一个参数是交换机，第二个参数是交换机和queue绑定的key**/
            amqpTemplate.convertAndSend("mytest",routingKey,JSON.toJSONString(requestMessage));
        });
        return "wangyu"+userName;
    }

    private String getTopicRoutingKey(String actualDestination, String sessionId){
        return actualDestination + "-user" + sessionId;
    }
}
