package account.controller;

import account.controller.exception.InvalidElementException;
import account.dto.request.EmployeePaymentsDto;
import account.dto.response.EmployeePaymentResponseDto;
import account.model.EmployeePayment;
import account.repository.EmployeePaymentRepository;
import account.repository.UserRepository;
import account.services.EmployeePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(value = "/api")
@Validated
class EmployeePaymentController {

    @Autowired
    private EmployeePaymentService employeePaymentService;

    @GetMapping(value = "/empl/payment")
    public ResponseEntity<?> getSingleEmployeePayroll(
            @RequestParam(value = "period", required = false, defaultValue = "") String period,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (period.isEmpty())
            return ResponseEntity.ok(employeePaymentService.getEmployeePayments(userDetails));
        return ResponseEntity.ok(employeePaymentService.getEmployeePaymentByPeriod(period, userDetails));
    }

    @PostMapping("/acct/payments")
    @Transactional
    public ResponseEntity<?> uploadPayrolls(
            @RequestBody @NotEmpty List<@Valid EmployeePaymentsDto> employeePaymentsDto
    ) {
        var response = employeePaymentService.uploadEmployeePayments(employeePaymentsDto);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/acct/payments")
    public ResponseEntity<?> updatePaymentsInformation(@RequestBody @Valid EmployeePaymentsDto dto) {
        var response = employeePaymentService.updateEmployeePayment(dto);
        return ResponseEntity.ok(response);
    }
}