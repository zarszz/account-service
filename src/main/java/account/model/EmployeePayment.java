package account.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_period", columnList = "id, period", unique = true),
})
@Data
public class EmployeePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String period;

    private long salary;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User employee;
}
