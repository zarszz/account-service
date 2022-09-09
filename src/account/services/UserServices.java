package account.services;

import account.model.SecurityEvent;
import account.model.User;
import account.repository.SecurityEventRepository;
import account.repository.UserRepository;
import account.security.constant.SecurityEventEnum;
import account.security.constant.UserSecurityLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Service
@Transactional
public class UserServices {
    public static final int MAX_FAILED_ATTEMPTS = 5;

    private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 hours

    @Autowired
    private UserRepository repo;

    @Autowired
    private SecurityEventService securityEventService;
    

    public User findByEmailIgnorecase(String email) {
        return repo.findByEmailIgnoreCase(email).orElse(null);
    }

    public void increaseFailedAttempts(User user, String path) {
        securityEventService.saveSecurityEvent(user.getEmail(), path, SecurityEventEnum.LOGIN_FAILED, path);
        int newFailAttempts = user.getFailedAttempt() + 1;
        if (newFailAttempts < 5) {
            user.setUserSecurityLogging(UserSecurityLogging.LOGIN_FAILED);
        }
        System.out.println("Impostor : " + "maxmustermann@acme.com");
        System.out.println("Current user: " + user.getEmail());
        System.out.println("Current failed attempt: " + user.getFailedAttempt());
        if (newFailAttempts == 5) {
            System.out.println("===> Continue in locking state");
            securityEventService.saveSecurityEvent(user.getEmail(), path, SecurityEventEnum.BRUTE_FORCE, path);
            var isAdmin = user.getRoles().stream().anyMatch(r -> r.getCode().equalsIgnoreCase("ADMINISTRATOR"));
            if (!isAdmin) {
                lock(user, path);
                user.setUserSecurityLogging(UserSecurityLogging.LOCK_USER);
            }
        }
        user.setFailedAttempt(newFailAttempts);
        repo.save(user);
//        repo.updateFailedAttempts(newFailAttempts, user.getEmail());
    }

    public void resetFailedAttempts(String email) {
        var user = repo.findByEmailIgnoreCase(email);
        if(user.isEmpty()) return;
        var existUser = user.get();
        existUser.setUserSecurityLogging(UserSecurityLogging.NORMAL);
        existUser.setFailedAttempt(0);
        repo.save(existUser);
    }

    public void lock(User user, String path) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        user.setUserSecurityLogging(UserSecurityLogging.LOCK_USER);
        repo.save(user);
        var object = "Lock user " + user.getEmail();
        securityEventService.saveSecurityEvent(user.getEmail(), object, SecurityEventEnum.LOCK_USER, path);
    }

    public boolean unlockWhenTimeExpired(User user, String path) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setUserSecurityLogging(UserSecurityLogging.NORMAL);
            user.setFailedAttempt(0);
            repo.save(user);
            var object = "Unlock user " + user.getEmail();
            securityEventService.saveSecurityEvent(user.getEmail(), object, SecurityEventEnum.UNLOCK_USER, path);
            return true;
        }
        return false;
    }
}