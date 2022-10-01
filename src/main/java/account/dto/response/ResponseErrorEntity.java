package account.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseErrorEntity <T> {
    private LocalDateTime timestamp = LocalDateTime.now();
    private int status;
    private String error;
    private T message;
    private String path;
}
