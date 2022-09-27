package account.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
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
}
