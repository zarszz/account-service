package account.model;

import account.security.constant.SecurityEventEnum;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Date date;

    @Enumerated(EnumType.STRING)
    private SecurityEventEnum action;
    private String subject;
    private String object;
    private String path;

    @CreationTimestamp
    private Date createdDate;
}
