package account.controller;

import account.dto.response.ResponseSecurityEvent;
import account.model.SecurityEvent;
import account.repository.SecurityEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/security/events")
public class SecurityEventController {
    @Autowired
    private SecurityEventRepository securityEventRepository;

    @GetMapping
    private ResponseEntity<?> getAllSecurityEvents() {
        var responses = securityEventRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(SecurityEvent::getId))
                .map(ResponseSecurityEvent::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

}
