package account.services;

import account.constant.ChangeUserAccessOperation;
import account.controller.exception.InvalidElementException;
import account.dto.request.ChangeUserAccessRequestDto;
import account.dto.request.ChangeUserRoleDto;
import account.model.User;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import account.security.constant.SecurityEventEnum;
import account.security.constant.UserSecurityLogging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SecurityEventService securityEventService;

    @Autowired
    private HttpServletRequest httpServletRequest;


    public User updateUserRole(ChangeUserRoleDto dto, UserDetails userDetails) {
        String role = dto.getRole().startsWith("ROLE_") ? dto.getRole().substring("ROLE_".length()) : dto.getRole();

        var userOptional = userRepository.findByEmailIgnoreCase(dto.getUser());
        if (userOptional.isEmpty()) throw new NoSuchElementException("User not found!");

        var roleOptional = roleRepository.findByCodeIgnoreCase(role);
        if (roleOptional.isEmpty()) throw new NoSuchElementException("Role not found!");

        var user = userOptional.get();

        if (dto.getOperation().equals("GRANT")) {
            var isAdmin = user.getRoles().stream().anyMatch(r -> r.getCode().equals("ADMINISTRATOR"));
            if (isAdmin && (role.equalsIgnoreCase("BUSINESS") || role.equalsIgnoreCase("AUDITOR") || role.equalsIgnoreCase("USER"))) {
                throw new InvalidElementException("The user cannot combine administrative and business roles!");
            }
            var isBusiness = user.getRoles().stream().anyMatch(r -> r.getCode().equals("ACCOUNTANT"));
            if (isBusiness && role.equalsIgnoreCase("ADMINISTRATOR")) {
                throw new InvalidElementException("The user cannot combine administrative and business roles!");
            }
            user.getRoles().add(roleOptional.get());
            user = userRepository.saveAndFlush(user);
            var object = "Grant role " + dto.getRole() + " to " + user.getEmail();
            securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.GRANT_ROLE, httpServletRequest.getRequestURI());
        }

        if (dto.getOperation().equals("REMOVE")) {
            var userRoles = user.getRoles().stream().anyMatch(r -> r.getCode().equals(role));
            if (!userRoles) throw new InvalidElementException("The user does not have a role!");

            var isAdmin = user.getRoles().stream().anyMatch(r -> r.getCode().equals("ADMINISTRATOR"));
            if (isAdmin) throw new InvalidElementException("Can't remove ADMINISTRATOR role!");

            var countRoles = user.getRoles().size();
            if (countRoles == 1) throw new InvalidElementException("The user must have at least one role!");

            var roles = user.getRoles()
                    .stream()
                    .filter(p -> !p.getCode().equals(role))
                    .collect(Collectors.toSet());

            user.setRoles(roles);
            user = userRepository.saveAndFlush(user);
            var object = "Remove role " + dto.getRole() + " from " + user.getEmail();

            securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.REMOVE_ROLE, httpServletRequest.getRequestURI());
        }

        return user;
    }

    public HashMap<String, Object> deleteUser(String email, UserDetails userDetails) {
        var userOptional = userRepository.findByEmailIgnoreCase(email);
        if (userOptional.isEmpty()) {
            throw new NoSuchElementException("User not found!");
        }

        var user = userOptional.get();

        boolean isDeleteRoleAdmin = user.getRoles().stream().anyMatch(r -> r.getCode().equalsIgnoreCase("ADMINISTRATOR"));
        if (isDeleteRoleAdmin) {
            throw new InvalidElementException("Can't remove ADMINISTRATOR role!");
        }

        userRepository.delete(user);

        var response = new HashMap<String, Object>();
        response.put("user", email);
        response.put("status", "Deleted successfully!");
        securityEventService.saveSecurityEvent(userDetails.getUsername(), user.getEmail(), SecurityEventEnum.DELETE_USER, httpServletRequest.getRequestURI());

        return response;
    }

    public HashMap<String, Object> updateAccess(ChangeUserAccessRequestDto changeUserAccessRequestDto, UserDetails userDetails) {
        var userOptional = userRepository.findByEmailIgnoreCase(changeUserAccessRequestDto.getUser());
        if (userOptional.isEmpty()) {
            throw new NoSuchElementException("User not found!");
        }

        var user = userOptional.get();

        if (changeUserAccessRequestDto.getOperation().equals(ChangeUserAccessOperation.LOCK)) {
            if (user.getRoles().stream().anyMatch(r -> r.getCode().equalsIgnoreCase("ADMINISTRATOR"))) {
                throw new InvalidElementException("Can't lock the ADMINISTRATOR!");
            }
            user.setUserSecurityLogging(UserSecurityLogging.LOCK_USER);
            user.setAccountNonLocked(false);
        } else {
            user.setUserSecurityLogging(UserSecurityLogging.NORMAL);
            user.setFailedAttempt(0);
            user.setAccountNonLocked(true);
        }

        userRepository.save(user);

        if (changeUserAccessRequestDto.getOperation().equals(ChangeUserAccessOperation.LOCK)) {
            var object = "Lock user " + user.getEmail();
            securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.LOCK_USER, httpServletRequest.getRequestURI());
        } else {
            var object = "Unlock user " + user.getEmail();
            securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.UNLOCK_USER, httpServletRequest.getRequestURI());
        }

        var mappedOperation = changeUserAccessRequestDto.getOperation().toString().toLowerCase() + "ed";
        var response = new HashMap<String, Object>();
        response.put("status", "User " + user.getEmail() + " " + mappedOperation + "!");
        return response;
    }
}
