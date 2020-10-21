package com.heiyigame.websocketresid.beans;

/**
 * @author admin
 */
public class ResponseMessage {
    private String resMessage;
    private String resDesc;

    public ResponseMessage(String resMessage, String resDesc) {
        this.resMessage = resMessage;
        this.resDesc = resDesc;
    }

    public String getResMessage() {
        return resMessage;
    }

    public void setResMessage(String resMessage) {
        this.resMessage = resMessage;
    }

    public String getResDesc() {
        return resDesc;
    }

    public void setResDesc(String resDesc) {
        this.resDesc = resDesc;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "resMessage='" + resMessage + '\'' +
                ", resDesc='" + resDesc + '\'' +
                '}';
    }
}
