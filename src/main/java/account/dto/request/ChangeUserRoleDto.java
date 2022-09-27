package account.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUserRoleDto {
    private String user;
    private String role;
    private String operation;
}
