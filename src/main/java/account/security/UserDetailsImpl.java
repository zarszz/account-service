package account.security;

import account.model.Role;
import account.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class UserDetailsImpl implements UserDetails {

	private final String username;
	private final String password;
	
	private final boolean isAccountNonLocked;

	private final Set<Role> roles;

	public UserDetailsImpl(User user) {
		this.username = user.getEmail();
		this.password = user.getPassword();
		this.roles = user.getRoles();
		this.isAccountNonLocked = user.isAccountNonLocked();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		for (var role: roles) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return isAccountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
