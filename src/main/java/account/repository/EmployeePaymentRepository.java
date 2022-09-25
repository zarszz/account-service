package account.repository;

import account.model.EmployeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePaymentRepository extends JpaRepository<EmployeePayment, Long> {
    Optional<EmployeePayment> findByPeriodAndEmployeeId(String period, Long employeeId);
    Optional<EmployeePayment> findByPeriodAndEmployeeEmail(String period, String employeeEMail);
    List<EmployeePayment> findByEmployeeId(Long userId);

}
