package account.model;

import javax.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "idx_period", columnList = "id, period", unique = true),
})
public class EmployeePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String period;

    private long salary;

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User employee;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }
}
