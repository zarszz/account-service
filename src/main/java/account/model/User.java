package account.model;

import account.dto.request.SignupRequestDto;
import account.security.constant.UserSecurityLogging;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
	@Index(name = "idx_email", columnList = "email", unique = true),
})
@Getter
@Setter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String name;
	private String lastname;
	private String password;
	private String email;

	private boolean isFirstUser;

	@Column(name = "account_non_locked", columnDefinition = "boolean default true")
	private boolean accountNonLocked = true;

	@Column(name = "failed_attempt", columnDefinition = "int default 0")
	private int failedAttempt;

	@Column(name = "lock_time", nullable = true)
	private Date lockTime;

	@Enumerated(EnumType.STRING)
	private UserSecurityLogging userSecurityLogging = UserSecurityLogging.NORMAL;

	@OneToMany
	@JoinColumn(name = "user_id")
	private List<EmployeePayment> payments = new ArrayList<>();

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "user_role",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id")
	)
	public Set<Role> roles = new HashSet<>();

	public static User fromDto(SignupRequestDto signupRequestDto) {
		var user = new User();
		user.setLastname(signupRequestDto.getLastname());
		user.setName(signupRequestDto.getName());
		user.setPassword(signupRequestDto.getPassword());
		user.setEmail(signupRequestDto.getEmail());
		return user;
	}
}
