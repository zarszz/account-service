package account.dto.response;

import account.model.Role;
import account.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class SignupResponseDto {
	private long id;
	private String name;
	private String lastname;
	private String email;
	private List<String> roles;

	public static SignupResponseDto fromRequest(User user) {
		// java stream sort ascending
		var response = new SignupResponseDto();
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setLastname(user.getLastname());
		response.setId(user.getId());
		response.setRoles(user.getRoles()
				.stream()
				.sorted(Comparator.comparing(Role::getCode))
				.map(r -> "ROLE_" + r.getCode())
				.collect(Collectors.toList()));
		;
		return response;
	}
}
