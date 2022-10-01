package account.security;

import account.dto.response.ResponseErrorEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof LockedException) {
            var responseErrorEntity = new ResponseErrorEntity<String>();
            responseErrorEntity.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());
            responseErrorEntity.setMessage(authException.getMessage());
            responseErrorEntity.setPath(request.getRequestURI());
            responseErrorEntity.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ServletOutputStream out = response.getOutputStream();
            out.println(new ObjectMapper().writeValueAsString(responseErrorEntity));
            out.flush();
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }
    }
}
