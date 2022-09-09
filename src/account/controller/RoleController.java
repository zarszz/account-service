package account.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.HashMap;
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

	//@PreAuthorize("hasAnyRole('ADMINISTRATOR')")
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
		if (userOptional.isEmpty()) return responseNotFound("User not found!", "/api/admin/user/role");
		var roleOptional = roleRepository.findByCodeIgnoreCase(role);
		if (roleOptional.isEmpty()) return responseNotFound("Role not found!", "/api/admin/user/role");

		var user = userOptional.get();

		User persistedUser = new User();

		if (dto.getOperation().equals("GRANT")) {
			user.getRoles().forEach(r -> System.out.println(r.getCode()));
			var isAdmin = user.getRoles().stream().anyMatch(r -> r.getCode().equals("ADMINISTRATOR"));
			if (isAdmin && (role.equalsIgnoreCase("BUSINESS") || role.equalsIgnoreCase("AUDITOR") || role.equalsIgnoreCase("USER"))) {
				return responseBadRequest("The user cannot combine administrative and business roles!", "/api/admin/user/role");
			}
			var isBusiness = user.getRoles().stream().anyMatch(r -> r.getCode().equals("ACCOUNTANT"));
			if (isBusiness && role.equalsIgnoreCase("ADMINISTRATOR")) {
				return responseBadRequest("The user cannot combine administrative and business roles!", "/api/admin/user/role");
			}
			user.getRoles().add(roleOptional.get());
			persistedUser = userRepository.saveAndFlush(user);
			var object = "Grant role " + dto.getRole() + " to " + user.getEmail();
			securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.GRANT_ROLE, httpServletRequest.getRequestURI());
		}

		if (dto.getOperation().equals("REMOVE")) {
			String finalRole = role;
			var userRoles = user.getRoles().stream().anyMatch(r -> r.getCode().equals(finalRole));
			if (!userRoles) return responseBadRequest("The user does not have a role!", "/api/admin/user/role");

			var isAdmin = user.getRoles().stream().anyMatch(r -> r.getCode().equals("ADMINISTRATOR"));
			if (isAdmin) return responseBadRequest("Can't remove ADMINISTRATOR role!", "/api/admin/user/role");

			var countRoles = user.getRoles().size();
			if (countRoles == 1) return responseBadRequest("The user must have at least one role!", "/api/admin/user/role");
			String finalRole1 = role;
			var roles = user.getRoles().stream().filter(p -> !p.getCode().equals(finalRole1));
			user.setRoles(roles.collect(Collectors.toSet()));
			persistedUser = userRepository.saveAndFlush(user);
			var object = "Remove role " + dto.getRole() + " from " + user.getEmail();
			securityEventService.saveSecurityEvent(userDetails.getUsername(), object, SecurityEventEnum.REMOVE_ROLE, httpServletRequest.getRequestURI());
		}
		return ResponseEntity.ok(SignupResponseDto.fromRequest(persistedUser));
	}

	//@PreAuthorize("hasAnyRole('ADMINISTRATOR')")
	@RequestMapping(value = "/user/{email}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUser(@PathVariable(value = "email", required = false) String email, @AuthenticationPrincipal UserDetails userDetails) {
		var user = userRepository.findByEmailIgnoreCase(email);
		if (user.isEmpty()) {
			return responseNotFound("User not found!", "/api/admin/user/" + email);
		}
		boolean isDeleteRoleAdmin = user.get().getRoles().stream().anyMatch(r -> r.getCode().equals("ADMINISTRATOR"));
		if (isDeleteRoleAdmin) {
			return responseBadRequest("Can't remove ADMINISTRATOR role!", "/api/admin/user/" + email);
		}
		userRepository.delete(user.get());
		var response = new HashMap<String, Object>();
		response.put("user", email);
		response.put("status", "Deleted successfully!");
		securityEventService.saveSecurityEvent(userDetails.getUsername(), user.get().getEmail(), SecurityEventEnum.DELETE_USER, httpServletRequest.getRequestURI());
		return ResponseEntity.ok(response);
	}

	//@PreAuthorize("hasAnyRole('ADMINISTRATOR')")
	@GetMapping("/user")
	public ResponseEntity<?> getAllUsers() {
		return ResponseEntity.ok(
				userRepository
						.findAll()
						.stream()
						.sorted(Comparator.comparingLong(User::getId))
						.map(SignupResponseDto::fromRequest)
						.toList()
		);
	}

	@PutMapping("/user/access")
	public ResponseEntity<?> updateAccess(
			@RequestBody ChangeUserAccessRequestDto changeUserAccessRequestDto,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		var userOptional = userRepository.findByEmailIgnoreCase(changeUserAccessRequestDto.getUser());
		if (userOptional.isEmpty()) {
			return responseNotFound("User not found!", "/api/admin/user/access");
		}

		var user = userOptional.get();

		if (changeUserAccessRequestDto.getOperation().equalsIgnoreCase("LOCK")) {
			if (user.getRoles().stream().anyMatch(r -> r.getCode().equalsIgnoreCase("ADMINISTRATOR"))) {
				return responseBadRequest("Can't lock the ADMINISTRATOR!", "/api/admin/user/access");
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

		var mappedOperation = changeUserAccessRequestDto.getOperation().equalsIgnoreCase("lock") ? "locked" : "unlocked";
		var response = new HashMap<String, Object>();
		response.put("status", "User " + user.getEmail() + " " + mappedOperation + "!");
		return ResponseEntity.ok(response);
	}

	private ResponseEntity<?> responseNotFound(String message, String endpoint) {
		var response = new HashMap<String, Object>();
		response.put("timestamp", "data");
		response.put("status", HttpStatus.NOT_FOUND.value());
		response.put("error", "Not Found");
		response.put("message", message);
		response.put("path", endpoint);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	private ResponseEntity<?> responseBadRequest(String message, String endpoint) {
		var response = new HashMap<String, Object>();
		response.put("timestamp", "data");
		response.put("status", HttpStatus.BAD_REQUEST.value());
		response.put("error", "Bad Request");
		response.put("message", message);
		response.put("path", endpoint);
		return ResponseEntity.badRequest().body(response);
	}
}
