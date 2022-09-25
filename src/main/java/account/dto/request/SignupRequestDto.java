package account.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class SignupRequestDto {
	@NotEmpty
	private String name;

	@NotEmpty
	private String lastname;

	@NotEmpty
	@Pattern(regexp = "^[A-Za-z0-9._%+-]+@acme.com$")
	private String email;

	@NotEmpty
	@Size(min = 12, message = "Password length must be 12 chars minimum!")
	private String password;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "SignupRequestDto{" +
				"name='" + name + '\'' +
				", lastname='" + lastname + '\'' +
				", email='" + email + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
