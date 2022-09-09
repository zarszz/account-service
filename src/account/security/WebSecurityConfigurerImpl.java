package account.security;

import account.security.constant.SecurityEventEnum;
import account.services.SecurityEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@EnableWebSecurity
public class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private SecurityEventService securityEventService;

	@Autowired
	private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
			.userDetailsService(userDetailsService) // user store 1
			.passwordEncoder(getEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.exceptionHandling().accessDeniedHandler(accessDeniedHandler())
		.and()
			.httpBasic()
			.authenticationEntryPoint(restAuthenticationEntryPoint) // Handle auth error
		.and()
			.csrf().disable().headers().frameOptions().disable() // for Postman, the H2 console
		.and()
			.authorizeRequests()
			.antMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
			.antMatchers(HttpMethod.POST, "/actuator/**").permitAll()
			.mvcMatchers(HttpMethod.GET, "/api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
			.mvcMatchers("/api/acct/payments").hasRole("ACCOUNTANT")
			.mvcMatchers(HttpMethod.GET, "/api/admin/user").hasRole("ADMINISTRATOR")
			.mvcMatchers(HttpMethod.DELETE, "/api/admin/user/**").hasRole("ADMINISTRATOR")
			.mvcMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
			.mvcMatchers("/api/security/**").hasRole("AUDITOR")
			.anyRequest().authenticated()
		.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS); // no session
}

	@Bean
	public PasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return (request, response, ex) -> {
			var principal = Objects.nonNull(request.getUserPrincipal()) ? request.getUserPrincipal().getName() : "Anonymous";
			securityEventService.saveSecurityEvent(principal, request.getRequestURI(), SecurityEventEnum.ACCESS_DENIED, request.getRequestURI());
			var responseTemplate = "{\n" +
					"  \"timestamp\" : \"<date>\"," +
					"  \"status\" : 403," +
					"  \"error\" : \"Forbidden\"," +
					"  \"message\" : \"Access Denied!\"," +
					"  \"path\" : \"" + request.getRequestURI() + "\"\n" +
					"}";
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			ServletOutputStream out = response.getOutputStream();
			out.println(responseTemplate);
			out.flush();
		};
	}
}
