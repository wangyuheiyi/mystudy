package com.heiyigame.websocketresid.beans;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * @author admin
 */
public class MyPrincipal implements Principal {
    private String loginName;

    public MyPrincipal(String loginName){
        this.loginName = loginName;
    }

    @Override
    public String getName() {
        return loginName;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }
}
