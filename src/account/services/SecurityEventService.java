package account.services;

import account.model.SecurityEvent;
import account.repository.SecurityEventRepository;
import account.security.constant.SecurityEventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SecurityEventService {
    @Autowired
    private SecurityEventRepository securityEventRepository;

    public void saveSecurityEvent(String subject, String object, SecurityEventEnum action, String path) {
        var securityEvent = new SecurityEvent();
        securityEvent.setAction(action);
        securityEvent.setPath(path);
        securityEvent.setDate(new Date());
        securityEvent.setSubject(subject);
        securityEvent.setObject(object);
        securityEventRepository.save(securityEvent);
    }

}
