package account.services;

import account.constant.BranchedPassword;
import account.controller.exception.InvalidElementException;
import account.dto.request.ChangePasswordRequestDto;
import account.dto.request.SignupRequestDto;
import account.model.User;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import account.security.constant.SecurityEventEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

@Service
public class AuthenticationService {

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

    public User signup(SignupRequestDto signupRequestDto) {
        var isPasswordBranched = BranchedPassword.BRANCHED_PASSWORD
                .stream()
                .anyMatch(n -> Objects.equals(n, signupRequestDto.getPassword()));
        if (isPasswordBranched) {
            throw new InvalidElementException("The password is in the hacker's database!");
        }

        var roles = roleRepository.findByCodeIn(new String[]{"USER"});
        signupRequestDto.setEmail(signupRequestDto.getEmail().toLowerCase());
        signupRequestDto.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));

        if (userRepository.findByEmailIgnoreCase(signupRequestDto.getEmail()).isPresent()) {
            throw new InvalidElementException("User exist!");
        }

        var user = User.fromDto(signupRequestDto);
        var firstUserOptional = userRepository.findByIsFirstUserTrue();

        if(firstUserOptional.isEmpty()) {
            roles = roleRepository.findByCodeIn(new String[]{"ADMINISTRATOR"});
            user.setFirstUser(true);
        }

        user.setRoles(new HashSet<>(roles));
        var persistedUser = userRepository.saveAndFlush(user);
        securityEventService.saveSecurityEvent("Anonymous", signupRequestDto.getEmail(), SecurityEventEnum.CREATE_USER, httpServletRequest.getRequestURI());
        return persistedUser;
    }

    public HashMap<String, Object> changePassword(
        @RequestBody @Valid ChangePasswordRequestDto changePasswordRequestDto,
        UserDetails userDetails
    ) {
        var isPasswordBranched = BranchedPassword.BRANCHED_PASSWORD
                .stream()
                .anyMatch(n -> Objects.equals(n, changePasswordRequestDto.getNewPassword()));
        if (isPasswordBranched) throw new InvalidElementException("The password is in the hacker's database!");

        var user = userRepository.findByEmailIgnoreCase(userDetails.getUsername());
        if (user.isEmpty()) throw new UsernameNotFoundException("Not found : " + userDetails.getUsername());

        var existUser = user.get();

        var isSameAsOldPassword = passwordEncoder.matches(changePasswordRequestDto.getNewPassword(), existUser.getPassword());
        if (isSameAsOldPassword) throw new InvalidElementException("The passwords must be different!");

        existUser.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));

        var newUser = userRepository.save(existUser);

        var response = new HashMap<String, Object>();
        response.put("email", existUser.getEmail());
        response.put("status", "The password has been updated successfully");
        securityEventService.saveSecurityEvent(newUser.getEmail(), newUser.getEmail(), SecurityEventEnum.CHANGE_PASSWORD, httpServletRequest.getRequestURI());
        return response;
    }
}
