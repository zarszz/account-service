package account.dto.response;


import account.model.SecurityEvent;
import account.security.constant.SecurityEventEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSecurityEvent {

    private SecurityEventEnum action;
    private String subject;
    private String object;
    private String path;

    public static ResponseSecurityEvent fromEntity(SecurityEvent securityEvent) {
        var responseSecurityEvent = new ResponseSecurityEvent();
        responseSecurityEvent.setAction(securityEvent.getAction());
        responseSecurityEvent.setObject(securityEvent.getObject());
        responseSecurityEvent.setPath(securityEvent.getPath());
        responseSecurityEvent.setSubject(securityEvent.getSubject());
        return responseSecurityEvent;
    }
}
