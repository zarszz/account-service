package account.services;

import account.dto.response.ResponseSecurityEvent;
import account.model.SecurityEvent;
import account.repository.SecurityEventRepository;
import account.security.constant.SecurityEventEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SecurityEventService {

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Async
    public void saveSecurityEvent(String subject, String object, SecurityEventEnum action, String path) {
        try {
            var securityEvent = new SecurityEvent();
            securityEvent.setAction(action);
            securityEvent.setPath(path);
            securityEvent.setDate(new Date());
            securityEvent.setSubject(subject);
            securityEvent.setObject(object);
            securityEventRepository.save(securityEvent);
        } catch (Exception e) {
            log.info("[saveSecurityEvent] failed to save securityEvent : {}", e.getMessage());
        }

    }

    public List<ResponseSecurityEvent> getAllSecurityEvents() {
        return securityEventRepository
                .findAll()
                .stream()
                .map(ResponseSecurityEvent::fromEntity)
                .collect(Collectors.toList());
    }

}
