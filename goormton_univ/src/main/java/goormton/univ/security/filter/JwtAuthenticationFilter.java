package goormton.univ.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import goormton.univ.security.service.CustomUserDetailsService;
import goormton.univ.security.token.JwtAuthenticationToken;
import goormton.univ.security.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    Long memberId = jwtUtil.extractMemberId(token);
                    UserDetails userDetails = userDetailsService.loadUserById(memberId);

                    JwtAuthenticationToken authentication =
                            new JwtAuthenticationToken(userDetails, token, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                objectMapper.writeValue(response.getWriter(),
                        Map.of("status", 401, "message", "Access Token Expired"));
                return;
            } catch (JwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                objectMapper.writeValue(response.getWriter(),
                        Map.of("status", 401, "message", "Invalid JWT"));
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
