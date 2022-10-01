package account.controller;

import account.controller.exception.InvalidElementException;
import account.dto.request.ChangeUserAccessRequestDto;
import account.dto.request.ChangeUserRoleDto;
import account.dto.response.SignupResponseDto;
import account.model.User;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import account.security.constant.SecurityEventEnum;
import account.security.constant.UserSecurityLogging;
import account.services.SecurityEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/admin")
public class RoleController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private SecurityEventService securityEventService;

	@Autowired
	private HttpServletRequest httpServletRequest;

	@PutMapping("/user/role")
	public ResponseEntity<?> updateUserRole(@RequestBody ChangeUserRoleDto dto, @AuthenticationPrincipal UserDetails userDetails) {
		String role = "";
		var isStartWithPrefix = dto.getRole().startsWith("ROLE_");
		if (isStartWithPrefix) {
			role = dto.getRole().substring("ROLE_".length());
		} else {
			role = dto.getRole();
		}
		var userOptional = userRepository.findByEmailIgnoreCase(dto.getUser());
		if (userOptional.isEmpty()) throw new NoSuchElementException("User not found!");
		var roleOptional = roleRepository.findByCodeIgnoreCase(role);
		if (roleOptional.isEmpty()) throw new NoSuchElementException("Role not found!");

		var user = userOptional.get();

		User persistedUser = new User();

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
			persistedUser = userRepository.saveAndFlush(user);
			var object = "Grant role " + dto.getRole() + " to " + user.getEmail();
			securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.GRANT_ROLE, httpServletRequest.getRequestURI());
		}

		if (dto.getOperation().equals("REMOVE")) {
			String finalRole = role;
			var userRoles = user.getRoles().stream().anyMatch(r -> r.getCode().equals(finalRole));
			if (!userRoles) throw new InvalidElementException("The user does not have a role!");

			var isAdmin = user.getRoles().stream().anyMatch(r -> r.getCode().equals("ADMINISTRATOR"));
			if (isAdmin) throw new InvalidElementException("Can't remove ADMINISTRATOR role!");

			var countRoles = user.getRoles().size();
			if (countRoles == 1) throw new InvalidElementException("The user must have at least one role!");
			String finalRole1 = role;
			var roles = user.getRoles().stream().filter(p -> !p.getCode().equals(finalRole1));
			user.setRoles(roles.collect(Collectors.toSet()));
			persistedUser = userRepository.saveAndFlush(user);
			var object = "Remove role " + dto.getRole() + " from " + user.getEmail();
			securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.REMOVE_ROLE, httpServletRequest.getRequestURI());
		}
		return ResponseEntity.ok(SignupResponseDto.fromRequest(persistedUser));
	}

	@RequestMapping(value = "/user/{email}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUser(@PathVariable(value = "email", required = false) String email, @AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmailIgnoreCase(email);
		if (user.isEmpty()) {
			throw new NoSuchElementException("User not found!");
		}
		boolean isDeleteRoleAdmin = user.get().getRoles().stream().anyMatch(r -> r.getCode().equals("ADMINISTRATOR"));
		if (isDeleteRoleAdmin) {
			throw new InvalidElementException("Can't remove ADMINISTRATOR role!");
		}
		userRepository.delete(user.get());
		var response = new HashMap<String, Object>();
		response.put("user", email);
		response.put("status", "Deleted successfully!");
		securityEventService.saveSecurityEvent(userDetails.getUsername(), user.get().getEmail(), SecurityEventEnum.DELETE_USER, httpServletRequest.getRequestURI());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/user")
	public ResponseEntity<?> getAllUsers() {
		return ResponseEntity.ok(
			userRepository
				.findAll()
				.stream()
				.sorted(Comparator.comparingLong(User::getId))
				.map(SignupResponseDto::fromRequest)
				.collect(Collectors.toList())
		);

	}

	@PutMapping("/user/access")
	public ResponseEntity<?> updateAccess(
			@RequestBody ChangeUserAccessRequestDto changeUserAccessRequestDto,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		var userOptional = userRepository.findByEmailIgnoreCase(changeUserAccessRequestDto.getUser());
		if (userOptional.isEmpty()) {
			throw new NoSuchElementException("User not found!");
		}

		var user = userOptional.get();

		if (changeUserAccessRequestDto.getOperation().equalsIgnoreCase("LOCK")) {
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

		if (changeUserAccessRequestDto.getOperation().equalsIgnoreCase("LOCK")) {
			var object = "Lock user " + user.getEmail();
			securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.LOCK_USER, httpServletRequest.getRequestURI());
		} else {
			var object = "Unlock user " + user.getEmail();
			securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.UNLOCK_USER, httpServletRequest.getRequestURI());
		}

		var mappedOperation = changeUserAccessRequestDto.getOperation().toLowerCase() + "ed";
		var response = new HashMap<String, Object>();
		response.put("status", "User " + user.getEmail() + " " + mappedOperation + "!");
		return ResponseEntity.ok(response);
	}
}
