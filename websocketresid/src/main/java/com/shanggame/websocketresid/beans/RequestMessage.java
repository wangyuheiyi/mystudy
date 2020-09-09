package com.shanggame.websocketresid.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestMessage {
    /** 消息发送对象*/
    private String messageName;
    /** 消息名称*/
    private String messageInfo;
}
