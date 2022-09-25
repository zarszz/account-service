package account.model;

import account.dto.request.SignupRequestDto;
import account.security.constant.UserSecurityLogging;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "users", indexes = {
	@Index(name = "idx_email", columnList = "email", unique = true),
})
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

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public int getFailedAttempt() {
		return failedAttempt;
	}

	public void setFailedAttempt(int failedAttempt) {
		this.failedAttempt = failedAttempt;
	}

	public Date getLockTime() {
		return lockTime;
	}

	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}

	public UserSecurityLogging getUserSecurityLogging() {
		return userSecurityLogging;
	}

	public void setUserSecurityLogging(UserSecurityLogging userSecurityLogging) {
		this.userSecurityLogging = userSecurityLogging;
	}

	public boolean isFirstUser() {
		return isFirstUser;
	}

	public void setFirstUser(boolean firstUser) {
		isFirstUser = firstUser;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public List<EmployeePayment> getPayments() {
		return payments;
	}

	public void setPayments(List<EmployeePayment> payments) {
		this.payments = payments;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
//	private String role; // should be prefixed with ROLE_

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public static User fromDto(SignupRequestDto signupRequestDto) {
		var user = new User();
		user.setLastname(signupRequestDto.getLastname());
		user.setName(signupRequestDto.getName());
		user.setPassword(signupRequestDto.getPassword());
		user.setEmail(signupRequestDto.getEmail());
		return user;
	}
}
