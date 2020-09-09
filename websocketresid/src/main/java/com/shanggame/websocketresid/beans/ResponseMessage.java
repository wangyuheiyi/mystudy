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
public class ResponseMessage {
    private String resMessage;
    private String resDesc;
}
