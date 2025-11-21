package mweb.mw_backend.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);
        final String email;

        // 🔹 Obtenemos la ruta actual
        String path = request.getServletPath();

        // 🔹 Si no hay token en header, buscar en cookie 'jwt'
        if (token == null) {
            if (request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : request.getCookies()) {
                    if ("jwt".equals(c.getName())) {
                        String cookieValue = c.getValue();
                        // Verificar que el valor de la cookie no esté vacío
                        if (StringUtils.hasText(cookieValue)) {
                            token = cookieValue;
                        }
                        break;
                    }
                }
            }
        }

        // 🔹 Si no hay token válido, limpiar contexto de seguridad y continuar
        if (token == null) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Extraer el email del token
        try {
            email = jwtService.getUsernameFromToken(token);
        } catch (Exception e) {
            // Token inválido, limpiar contexto y continuar
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 Validar token y establecer autenticación
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    // Token no válido, limpiar contexto
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                // Error al cargar usuario o validar token, limpiar contexto
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);

        }
        return null;
    }
}
