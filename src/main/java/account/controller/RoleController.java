package account.controller;

import account.dto.request.ChangeUserAccessRequestDto;
import account.dto.request.ChangeUserRoleDto;
import account.dto.response.SignupResponseDto;
import account.repository.RoleRepository;
import account.repository.UserRepository;
import account.services.RoleService;
import account.services.SecurityEventService;
import account.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/api/admin")
public class RoleController {

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserServices userServices;

	@PutMapping("/user/role")
	public ResponseEntity<?> updateUserRole(@RequestBody ChangeUserRoleDto dto, @AuthenticationPrincipal UserDetails userDetails) {
		var persistedUser = roleService.updateUserRole(dto, userDetails);
		return ResponseEntity.ok(SignupResponseDto.fromRequest(persistedUser));
	}

	@RequestMapping(value = "/user/{email}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteUser(@PathVariable(value = "email", required = false) String email, @AuthenticationPrincipal UserDetails userDetails) {
		return ResponseEntity.ok(roleService.deleteUser(email, userDetails));
	}

	@GetMapping("/user")
	public ResponseEntity<?> getAllUsers() {
		return ResponseEntity.ok(userServices.findAll());

	}

	@PutMapping("/user/access")
	public ResponseEntity<?> updateAccess(
			@RequestBody ChangeUserAccessRequestDto changeUserAccessRequestDto,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		return ResponseEntity.ok(roleService.updateAccess(changeUserAccessRequestDto, userDetails));
	}
}
