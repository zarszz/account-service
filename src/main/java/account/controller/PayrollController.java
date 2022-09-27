package account.controller;

import account.dto.request.EmployeePaymentsDto;
import account.dto.response.EmployeePaymentResponseDto;
import account.model.EmployeePayment;
import account.repository.EmployeePaymentRepository;
import account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

@RestController
@RequestMapping(value = "/api")
@Validated
class PayrollController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmployeePaymentRepository employeePaymentRepository;
//
//	@GetMapping(value = "/empl/payments")
//	public ResponseEntity<?> getEmployeePayroll() {
//		return ResponseEntity.ok("");
//	}

	//@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ACCOUNTANT')")
	@GetMapping(value = "/empl/payment")
	public ResponseEntity<?> getSingleEmployeePayroll(
			@RequestParam(value = "period", required = false, defaultValue = "") String period,
			@AuthenticationPrincipal UserDetails userDetails
	) {
		var user = userRepository.findByEmailIgnoreCase(userDetails.getUsername().toLowerCase());
		if (user.isEmpty()) return responseNotFound("User not found", "/api/empl/payments");

		if (period.isEmpty()) {
			var periods = employeePaymentRepository.findByEmployeeId(user.get().getId());
			var sortedDesc = periods.stream().sorted((a, b) -> b.getPeriod().compareTo(a.getPeriod()));
			return ResponseEntity.ok(sortedDesc.map(EmployeePaymentResponseDto::fromRequest));
		}

		var payment = employeePaymentRepository.findByPeriodAndEmployeeId(period, user.get().getId());
		if (payment.isEmpty()) return responseBadRequest("Payment not found", "/api/empl/payment");
		var response = new EmployeePaymentResponseDto();
		response.setPeriod(EmployeePaymentResponseDto.setDate(period));
		response.setName(user.get().getName());
		response.setLastname(user.get().getLastname());
		response.setSalary(EmployeePaymentResponseDto.dollarToCents(payment.get().getSalary()));
		return ResponseEntity.ok(response);
//		return ResponseEntity.ok(SignupResponseDto.fromRequest(user.get()));
	}

	//@PreAuthorize("hasAnyRole('ROLE_ACCOUNTANT')")
	@PostMapping("/acct/payments")
	@Transactional
	public ResponseEntity<?> uploadPayrolls(
		@RequestBody @NotEmpty List<@Valid EmployeePaymentsDto> employeePaymentsDto,
		Errors errors
	) {
		var employeePayments = new ArrayList<EmployeePayment>();

		for (var dto: employeePaymentsDto) {
			var user = userRepository.findByEmailIgnoreCase(dto.getEmployee().toLowerCase());
			var isExist = employeePaymentRepository.findByPeriodAndEmployeeEmail(dto.getPeriod(), dto.getEmployee());
			if (isExist.isPresent()) return responseBadRequest("Duplicated entry in payment list", "/api/acct/payments");
			if (user.isEmpty()) return responseNotFound("user not found", "/api/acct/payments");
			var employeePayment = new EmployeePayment();
			employeePayment.setEmployee(user.get());
			employeePayment.setPeriod(dto.getPeriod());
			employeePayment.setSalary(dto.getSalary());
			employeePayments.add(employeePayment);
		}

		employeePaymentRepository.saveAll(employeePayments);
		var response = new HashMap<String, String>();
		response.put("status", "Added successfully!");
		return ResponseEntity.ok(response);
	}

	//@PreAuthorize("hasAnyRole('ROLE_ACCOUNTANT')")
	@PutMapping("/acct/payments")
	public ResponseEntity<?> updatePaymentsInformation(@RequestBody @Valid EmployeePaymentsDto dto) {
		var user = userRepository.findByEmailIgnoreCase(dto.getEmployee().toLowerCase());
		var isExist = employeePaymentRepository.findByPeriodAndEmployeeEmail(dto.getPeriod(), dto.getEmployee());
		if (isExist.isEmpty()) return responseBadRequest("Payment not found", "/api/acct/payments");
		if (user.isEmpty()) return responseNotFound("user not found", "/api/acct/payments");
		var payment = isExist.get();
		payment.setSalary(dto.getSalary());
		employeePaymentRepository.save(payment);
		var response = new HashMap<String, String>();
		response.put("status", "Updated successfully!");
		return ResponseEntity.ok(response);
	}

	private ResponseEntity<?> responseNotFound(String message, String endpoint) {
		var response = new HashMap<String, Object>();
		response.put("timestamp", "data");
		response.put("status", HttpStatus.NOT_FOUND.value());
		response.put("error", "Not found");
		response.put("message", message);
		response.put("path", endpoint);
		return ResponseEntity.badRequest().body(response);
	}

	private ResponseEntity<?> responseBadRequest(String message, String endpoint) {
		var response = new HashMap<String, Object>();
		response.put("timestamp", "data");
		response.put("status", HttpStatus.BAD_REQUEST.value());
		response.put("error", "Bad Request");
		response.put("message", message);
		response.put("path", endpoint);
		return ResponseEntity.badRequest().body(response);
	}
}