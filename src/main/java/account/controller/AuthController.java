package account.controller;

import account.constant.BranchedPassword;
import account.controller.exception.InvalidElementException;
import account.dto.request.ChangePasswordRequestDto;
import account.dto.request.SignupRequestDto;
import account.dto.response.SignupResponseDto;
import account.model.User;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import account.security.constant.SecurityEventEnum;
import account.services.AuthenticationService;
import account.services.SecurityEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.Errors;
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
    private AuthenticationService authenticationService;

    @PostMapping("signup")
    ResponseEntity<?> signup(@RequestBody @Valid SignupRequestDto signupRequestDto) {
        var persistedUser = authenticationService.signup(signupRequestDto);
        return ResponseEntity.ok(SignupResponseDto.fromRequest(persistedUser));
    }

    @PostMapping("changepass")
    ResponseEntity<?> changePassword(
        @RequestBody @Valid ChangePasswordRequestDto changePasswordRequestDto,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        var response = authenticationService.changePassword(changePasswordRequestDto, userDetails);
        return ResponseEntity.ok(response);
    }
}
