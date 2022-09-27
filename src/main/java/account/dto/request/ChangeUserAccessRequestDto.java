package account.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUserAccessRequestDto {
    private String user;
    private String operation;
}
