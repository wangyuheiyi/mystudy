package com.heiyigame.websocketresid.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author admin
 */
public class LogUtil {
    public static final Logger mygame = getLogger("mygame");


    /**
     * 获取日志对象
     * @param name
     */
    private static Logger getLogger(String name)
    {
        // 断言参数不为空
        // 获取日志
        return LoggerFactory.getLogger(name);
    }
}
