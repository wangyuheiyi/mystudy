package com.heiyigame.websocketresid.controller;
import com.alibaba.fastjson.JSON;
import com.heiyigame.websocketresid.beans.RequestMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author admin
 */
@Controller
@Slf4j
public class SendBroadcastCtl {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @RequestMapping(value = "/sendInfo", method = RequestMethod.GET)
    @ResponseBody
    public String sendInfo(@RequestParam(name="userName",required=false) String userName,@RequestParam(name="message",required=false) String message){
        log.info("send info ========="+userName);
        RequestMessage requestMessage=new RequestMessage(userName,message);
        amqpTemplate.convertAndSend("mytest",userName+"/topic/getResponse",JSON.toJSONString(requestMessage));
        return "wangyu"+userName;
    }
}
