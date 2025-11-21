package mweb.mw_backend.controller.frontend.user;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.controller.base.BaseController;
import mweb.mw_backend.entity.User;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/user")
public class UserProfileController extends BaseController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileController(CategoryRepository categoryRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(categoryRepository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        // Recargar el usuario desde la base de datos para obtener datos actualizados
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        model.addAttribute("user", currentUser);
        model.addAttribute("pageTitle", "Mi Perfil");
        
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal User user,
                               @RequestParam String name,
                               @RequestParam String lastName,
                               @RequestParam String cel,
                               @RequestParam(required = false) String address,
                               RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        try {
            User currentUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            currentUser.setName(name);
            currentUser.setLastName(lastName);
            currentUser.setCel(cel);
            currentUser.setAddress(address);
            
            userRepository.save(currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el perfil: " + e.getMessage());
        }
        
        return "redirect:/web/user/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal User user,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        try {
            User currentUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Verificar que la contraseña actual sea correcta
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
                return "redirect:/web/user/profile";
            }
            
            // Verificar que las nuevas contraseñas coincidan
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
                return "redirect:/web/user/profile";
            }
            
            // Validar longitud de la nueva contraseña
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "La nueva contraseña debe tener al menos 6 caracteres");
                return "redirect:/web/user/profile";
            }
            
            // Actualizar contraseña
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Contraseña actualizada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar la contraseña: " + e.getMessage());
        }
        
        return "redirect:/web/user/profile";
    }
}
