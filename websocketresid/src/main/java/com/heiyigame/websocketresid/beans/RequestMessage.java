package com.heiyigame.websocketresid.beans;

/**
 * @author admin
 */
public class RequestMessage {
    /** 消息发送对象*/
    private String messageName;
    /** 消息名称*/
    private String messageInfo;

    public RequestMessage(String userName, String message) {
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public String getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(String messageInfo) {
        this.messageInfo = messageInfo;
    }

    @Override
    public String toString() {
        return "RequestMessage{" +
                "messageName='" + messageName + '\'' +
                ", messageInfo='" + messageInfo + '\'' +
                '}';
    }
}
