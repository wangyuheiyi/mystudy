package com.heiyigame.mynettyclient.bean;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleBean {
    /** 玩家id*/
    private long userId;
    /** 请求状态*/
    private String name;
    /** 请求状态*/
    private int vip;
    /** 玩家网络通道*/
    private Channel channel;
}
