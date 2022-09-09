package account.dto.response;

import account.model.EmployeePayment;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class EmployeePaymentResponseDto {
    private String name;
    private String lastname;
    private String period;

    @JsonIgnore
    private Long salaryLong;

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    private String salary;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getSalaryLong() {
        return salaryLong;
    }

    public void setSalaryLong(Long salaryLong) {
        this.salaryLong = salaryLong;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public static EmployeePaymentResponseDto fromRequest(EmployeePayment employeePayment) {
        var response = new EmployeePaymentResponseDto();
        response.setPeriod(setDate(employeePayment.getPeriod()));
        response.setName(employeePayment.getEmployee().getName());
        response.setLastname(employeePayment.getEmployee().getLastname());
        response.setSalary(dollarToCents(employeePayment.getSalary()));
        return response;
    }

    public static String setDate(String date) {
        var month = date.substring(0, 2);
        var monthName = Month.of(Integer.parseInt(month)).getDisplayName(TextStyle.FULL_STANDALONE, Locale.ENGLISH);
        var year = date.substring(3);
        return monthName + "-" + year;

    }

    public static String dollarToCents(long salaryLong) {
        var stringValue = String.valueOf(salaryLong);
        var cents = stringValue.substring(stringValue.length() - 2);
        var dollars = stringValue.substring(0, stringValue.length() - 2);
        var emptyHandler = dollars.isEmpty() ? "0" : dollars;
        return emptyHandler + " dollar(s) " + cents + " cent(s)";
    }
}
