package account.dto.response;

import account.dto.request.SignupRequestDto;
import account.model.Role;
import account.model.User;

import java.util.Comparator;
import java.util.List;

public class SignupResponseDto {
	private long id;
	private String name;
	private String lastname;
	private String email;

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	private List<String> roles;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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
				.toList())
		;
		return response;
	}
}
