package account.dto.request;

import account.constant.ChangeUserAccessOperation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeUserAccessRequestDto {
    private String user;
    private ChangeUserAccessOperation operation;
}
