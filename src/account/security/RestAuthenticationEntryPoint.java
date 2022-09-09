package account.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof LockedException) {
            var responseTemplate = "{\n" +
                    "  \"timestamp\" : \"<date>\"," +
                    "  \"status\" : " + HttpStatus.UNAUTHORIZED.value() + "," +
                    "  \"error\" : \"" + HttpStatus.UNAUTHORIZED.getReasonPhrase() + "\"," +
                    "  \"message\" : \"" + authException.getMessage() + "\"," +
                    "  \"path\" : \"" + request.getRequestURI() + "\"\n" +
                    "}";
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ServletOutputStream out = response.getOutputStream();
            out.println(responseTemplate);
            out.flush();
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }
    }
}
