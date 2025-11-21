package mweb.mw_backend.controller.frontend.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.service.UserService;

@Controller
@RequestMapping("/web/users")
@RequiredArgsConstructor
public class UserWebController {

    private final UserService userService;

    // Listar usuarios
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users"; // Nombre del template Thymeleaf
    }

    // Ver detalle de usuario
    @GetMapping("/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        var user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        model.addAttribute("user", user);
        return "user-detail"; // Template de detalle
    }

    // Cambiar estado (activar/desactivar)
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/web/users";
    }
}
