package account.services;

import account.controller.exception.InvalidElementException;
import account.dto.request.EmployeePaymentsDto;
import account.dto.response.EmployeePaymentResponseDto;
import account.model.EmployeePayment;
import account.repository.EmployeePaymentRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class EmployeePaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeePaymentRepository employeePaymentRepository;

    public EmployeePaymentResponseDto getEmployeePaymentByPeriod(
            String period,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var user = userRepository.findByEmailIgnoreCase(userDetails.getUsername().toLowerCase());
        if (user.isEmpty()) throw new NoSuchElementException("User not found");

        var payment = employeePaymentRepository.findByPeriodAndEmployeeId(period, user.get().getId());
        if (payment.isEmpty()) throw new InvalidElementException("Payment not found");

        var response = new EmployeePaymentResponseDto();
        response.setPeriod(EmployeePaymentResponseDto.setDate(period));
        response.setName(user.get().getName());
        response.setLastname(user.get().getLastname());
        response.setSalary(EmployeePaymentResponseDto.dollarToCents(payment.get().getSalary()));

        return response;
    }

    public List<EmployeePaymentResponseDto> getEmployeePayments(
            UserDetails userDetails
    ) {
        var user = userRepository.findByEmailIgnoreCase(userDetails.getUsername().toLowerCase());
        if (user.isEmpty()) throw new NoSuchElementException("User not found");

        var periods = employeePaymentRepository.findByEmployeeId(user.get().getId());
        var sortedDesc = periods
                .stream()
                .sorted((a, b) -> b.getPeriod().compareTo(a.getPeriod()));

        return sortedDesc
                .map(EmployeePaymentResponseDto::fromRequest)
                .collect(Collectors.toList());
    }

    public HashMap<String, String> uploadEmployeePayments(List<EmployeePaymentsDto> employeePaymentsDto) {
        var employeePayments = new ArrayList<EmployeePayment>();

        for (var dto : employeePaymentsDto) {
            var user = userRepository.findByEmailIgnoreCase(dto.getEmployee().toLowerCase());

            var isExist = employeePaymentRepository.findByPeriodAndEmployeeEmail(dto.getPeriod(), dto.getEmployee());
            if (isExist.isPresent()) throw new InvalidElementException("Duplicated entry in payment list");
            if (user.isEmpty()) throw new NoSuchElementException("user not found");

            var employeePayment = new EmployeePayment();
            employeePayment.setEmployee(user.get());
            employeePayment.setPeriod(dto.getPeriod());
            employeePayment.setSalary(dto.getSalary());
            employeePayments.add(employeePayment);
        }

        employeePaymentRepository.saveAll(employeePayments);
        var response = new HashMap<String, String>();
        response.put("status", "Added successfully!");
        return response;
    }

    public HashMap<String, String> updateEmployeePayment(EmployeePaymentsDto dto) {
        var user = userRepository.findByEmailIgnoreCase(dto.getEmployee().toLowerCase());

        var isExist = employeePaymentRepository.findByPeriodAndEmployeeEmail(dto.getPeriod(), dto.getEmployee());
        if (isExist.isEmpty()) throw new InvalidElementException("Payment not found");
        if (user.isEmpty()) throw new NoSuchElementException("user not found ");

        var payment = isExist.get();
        payment.setSalary(dto.getSalary());
        employeePaymentRepository.save(payment);
        var response = new HashMap<String, String>();
        response.put("status", "Updated successfully!");
        return response;
    }
}
