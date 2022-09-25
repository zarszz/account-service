package account.controller;

import account.constant.BranchedPassword;
import account.dto.request.ChangePasswordRequestDto;
import account.dto.request.SignupRequestDto;
import account.dto.response.SignupResponseDto;
import account.model.User;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import account.security.constant.SecurityEventEnum;
import account.services.SecurityEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityEventService securityEventService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @PostMapping("signup")
    ResponseEntity<?> signup(@RequestBody @Valid SignupRequestDto signupRequestDto, Errors errors) {
        if (errors.hasFieldErrors()) {
            if (errors.hasFieldErrors()) {
                FieldError fieldError = errors.getFieldError();
                return getResponseEntity(fieldError.getDefaultMessage(),  "/api/auth/signup");
            }
        }
        var isPasswordBranched = Arrays.stream(BranchedPassword.BRANCHED_PASSWORD)
                .anyMatch(n -> Objects.equals(n, signupRequestDto.getPassword()));
        if (isPasswordBranched) {
            return getResponseEntity("The password is in the hacker's database!",  "/api/auth/signup");
        }
        var roles = roleRepository.findByCodeIn(new String[]{"USER"});
        signupRequestDto.setEmail(signupRequestDto.getEmail().toLowerCase());
        signupRequestDto.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
        if (userRepository.findByEmailIgnoreCase(signupRequestDto.getEmail()).isPresent()) {
            return getResponseEntity("User exist!",  "/api/auth/signup");
        }

        User persistedUser = null;

        var firstUserOptional = userRepository.findByIsFirstUserTrue();
        if(firstUserOptional.isEmpty()) {
            roles = roleRepository.findByCodeIn(new String[]{"ADMINISTRATOR"});
            var user = User.fromDto(signupRequestDto);
            user.setFirstUser(true);
            user.setRoles(new HashSet<>(roles));
            persistedUser = userRepository.saveAndFlush(user);
        } else {
            var user = User.fromDto(signupRequestDto);
            user.setFirstUser(false);
            user.setRoles(new HashSet<>(roles));
            persistedUser = userRepository.saveAndFlush(user);
        }
        securityEventService.saveSecurityEvent("Anonymous", signupRequestDto.getEmail(), SecurityEventEnum.CREATE_USER, httpServletRequest.getRequestURI());
        return ResponseEntity.ok(SignupResponseDto.fromRequest(persistedUser));
    }

    private ResponseEntity<?> getResponseEntity(String message, String endpoint) {
        var response = new HashMap<String, Object>();
        response.put("timestamp", "data");
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", message);
        response.put("path", endpoint);
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("changepass")
    ResponseEntity<?> changePassword(
            @RequestBody @Valid ChangePasswordRequestDto changePasswordRequestDto,
            Errors errors,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (errors.hasFieldErrors()) {
            FieldError fieldError = errors.getFieldError();
            return getResponseEntity(fieldError.getDefaultMessage(),  "/api/auth/changepass");
        }
        var isPasswordBranched = Arrays.stream(BranchedPassword.BRANCHED_PASSWORD)
                .anyMatch(n -> Objects.equals(n, changePasswordRequestDto.getNewPassword()));
        if (isPasswordBranched) return getResponseEntity("The password is in the hacker's database!", "/api/auth/changepass");

        var user = userRepository.findByEmailIgnoreCase(userDetails.getUsername());
        if (user.isEmpty()) throw new UsernameNotFoundException("Not found : " + userDetails.getUsername());

        var existUser = user.get();
        var isSameAsOldPassword = passwordEncoder.matches(changePasswordRequestDto.getNewPassword(), existUser.getPassword());
        if (isSameAsOldPassword) return getResponseEntity("The passwords must be different!", "/api/auth/changepass");

        existUser.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));

        var newUser = userRepository.save(existUser);

        var response = new HashMap<String, String>();
        response.put("email", existUser.getEmail());
        response.put("status", "The password has been updated successfully");
        securityEventService.saveSecurityEvent(newUser.getEmail(), newUser.getEmail(), SecurityEventEnum.CHANGE_PASSWORD, httpServletRequest.getRequestURI());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username")
    public void username(@AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Username : " + userDetails.getUsername());
        System.out.println("User has authorities/roles : " + userDetails.getAuthorities());
    }
}
