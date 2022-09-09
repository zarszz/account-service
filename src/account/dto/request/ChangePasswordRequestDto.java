package account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class ChangePasswordRequestDto {
	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	@JsonProperty(value = "new_password")
	@Size(min = 12, message = "Password length must be 12 chars minimum!")
	private String newPassword;
}
