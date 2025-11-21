package mweb.mw_backend.controller.frontend.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mweb.mw_backend.auth.LoginRequest;
import mweb.mw_backend.auth.RegisterRequest;
import mweb.mw_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/web/auth")
@RequiredArgsConstructor
public class AuthWebController {
    
    private final AuthService authService;

    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        
        // Esta página ya no es necesaria, redirigir al home con parámetros
        if (error != null) {
            return "redirect:/?error=login";
        }
        
        if (logout != null) {
            return "redirect:/?message=logout";
        }
        
        // Redirigir al home donde están los modales
        return "redirect:/";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        // Esta página ya no es necesaria, redirigir al home
        return "redirect:/?showRegister=true";
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerAjax(@Valid @ModelAttribute RegisterRequest registerRequest,
                                                             BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Si hay errores de validación, retornarlos
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                response.put("success", false);
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Registrar usuario y obtener token
            var authResponse = authService.register(registerRequest);

            // Crear cookie con el token (jwt) para que el filtro pueda autenticar en páginas web
            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt", authResponse.getToken())
                    .path("/")
                    .httpOnly(true)
                    .sameSite("Lax")
                    .maxAge(7 * 24 * 60 * 60) // 7 días
                    .build();

            response.put("success", true);
            response.put("message", "¡Registro exitoso! Tu cuenta ha sido creada correctamente.");
            return ResponseEntity.ok().header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginAjax(@Valid @ModelAttribute LoginRequest loginRequest,
                                                          BindingResult bindingResult) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Si hay errores de validación, retornarlos
            if (bindingResult.hasErrors()) {
                Map<String, String> errors = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
                response.put("success", false);
                response.put("errors", errors);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Autenticar usuario
            var authResponse = authService.login(loginRequest);
            
            // Crear cookie con el token (jwt)
            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("jwt", authResponse.getToken())
                    .path("/")
                    .httpOnly(true)
                    .sameSite("Lax")
                    .maxAge(7 * 24 * 60 * 60) // 7 días
                    .build();

            response.put("success", true);
            response.put("message", "¡Login exitoso! Bienvenido de vuelta.");
            return ResponseEntity.ok().header(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", "Email o contraseña incorrectos");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:/web/auth/login?logout=true";
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logoutAjax() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Crear cookie para eliminar la existente (expires inmediatamente)
            org.springframework.http.ResponseCookie deleteCookie = org.springframework.http.ResponseCookie.from("jwt", "")
                    .path("/")
                    .httpOnly(true)
                    .sameSite("Lax")
                    .maxAge(0) // Expira inmediatamente
                    .build();
            
            response.put("success", true);
            response.put("message", "Sesión cerrada exitosamente");
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error al cerrar sesión");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAuthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            org.springframework.security.core.Authentication auth = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                     !"anonymousUser".equals(auth.getName());
            
            response.put("authenticated", isAuthenticated);
            response.put("user", isAuthenticated ? auth.getName() : null);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("authenticated", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    
    @GetMapping("/test-register")
    public String testRegister(Model model) {
        return "test-register";
    }
}