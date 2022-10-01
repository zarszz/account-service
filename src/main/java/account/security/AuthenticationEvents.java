package account.security;

import account.security.constant.SecurityEventEnum;
import account.services.SecurityEventService;
import account.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Component
public class AuthenticationEvents {
    @Autowired
    private UserServices userService;

    @Autowired
    private SecurityEventService securityEventService;

    @Autowired
    private HttpServletRequest request;

    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent failures) {
        var principal = (String) failures.getAuthentication().getPrincipal();
        var user = userService.findByEmailIgnorecase(principal);
        if (Objects.isNull(user)) {
            var path = request.getRequestURI();
            securityEventService.saveSecurityEvent(principal, path, SecurityEventEnum.LOGIN_FAILED, path);
        } else {
            if (user.isAccountNonLocked()) {
                if (user.getFailedAttempt() < UserServices.MAX_FAILED_ATTEMPTS)
                    userService.increaseFailedAttempts(user, request.getRequestURI());
                else throw new LockedException("User account is locked");
            } else {
                userService.unlockWhenTimeExpired(user, request.getRequestURI());
                throw new LockedException("User account is locked");
            }
        }
    }

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent success) {
        var principal = (UserDetailsImpl) success.getAuthentication().getPrincipal();
        var user = userService.findByEmailIgnorecase(principal.getUsername());
        if (user.getFailedAttempt() > 0) {
            userService.resetFailedAttempts(user.getEmail());
        }
    }
}
