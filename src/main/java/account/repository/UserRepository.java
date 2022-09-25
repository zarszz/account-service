package account.repository;

import account.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	@Query("SELECT u FROM User u JOIN FETCH u.roles WHERE lower(u.email) = lower(?1)")
	Optional<User> findByEmailIgnoreCase(String email);
	Optional<User> findByIsFirstUserTrue();

	@Query("UPDATE User u SET u.failedAttempt = ?1 WHERE u.email = ?2")
	@Modifying
	void updateFailedAttempts(int failAttempts, String email);
}
