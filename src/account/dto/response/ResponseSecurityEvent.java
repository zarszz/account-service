package account.dto.response;


import account.model.SecurityEvent;
import account.security.constant.SecurityEventEnum;

import java.util.Date;

public class ResponseSecurityEvent {

    private SecurityEventEnum action;
    private String subject;
    private String object;
    private String path;

    public SecurityEventEnum getAction() {
        return action;
    }

    public void setAction(SecurityEventEnum action) {
        this.action = action;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public static ResponseSecurityEvent fromEntity(SecurityEvent securityEvent) {
        var responseSecurityEvent = new ResponseSecurityEvent();
        responseSecurityEvent.setAction(securityEvent.getAction());
        responseSecurityEvent.setObject(securityEvent.getObject());
        responseSecurityEvent.setPath(securityEvent.getPath());
        responseSecurityEvent.setSubject(securityEvent.getSubject());
        return responseSecurityEvent;
    }
}
