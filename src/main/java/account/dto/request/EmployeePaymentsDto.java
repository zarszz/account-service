package account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

public class EmployeePaymentsDto {
    private String employee;

    @Pattern(regexp = "^(0?[1-9]|1[012])\\-[0-9]{4}$")
    private String period;

    @JsonProperty(value = "salary")
    @Range(min = 0l, message = "Please select positive numbers Only")
    private int salary;

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }
}
