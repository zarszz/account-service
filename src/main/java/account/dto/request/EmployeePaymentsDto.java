package account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class EmployeePaymentsDto {
    private String employee;

    @Pattern(regexp = "^(0?[1-9]|1[012])\\-[0-9]{4}$")
    private String period;

    @JsonProperty(value = "salary")
    @Range(min = 0l, message = "Please select positive numbers Only")
    private int salary;
}
