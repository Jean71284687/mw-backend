package mweb.mw_backend.controller.frontend.dashboard.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mweb.mw_backend.entity.User;
import mweb.mw_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardUserController {

    private final UserRepository userRepository;

    /**
     * Página principal de gestión de usuarios
     */
    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model
    ) {
        log.info("👥 Accediendo a gestión de usuarios - Página: {}, Tamaño: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<User> usersPage;

            if (search != null && !search.trim().isEmpty()) {
                usersPage = (Page<User>) userRepository.findByUsernameContainingIgnoreCase(search);
            } else {
                usersPage = userRepository.findAll(pageable);
            }

            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalUsers", usersPage.getTotalElements());
            model.addAttribute("search", search);

            log.info("✅ Usuarios cargados exitosamente: {} usuarios encontrados", usersPage.getTotalElements());
            return "dashboard/usuarios";

        } catch (Exception e) {
            log.error("❌ Error al cargar usuarios: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los usuarios");
            return "dashboard/usuarios";
        }
    }

    /**
     * Ver detalles de un usuario específico
     */
    @GetMapping("/usuarios/{id}")
    public String verUsuario(@PathVariable Long id, Model model) {
        log.info("👁️ Viendo detalles de usuario ID: {}", id);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            model.addAttribute("user", user);
            return "dashboard/usuarios-detalle";

        } catch (Exception e) {
            log.error("❌ Error al cargar detalles de usuario: {}", e.getMessage(), e);
            model.addAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/dashboard/usuarios";
        }
    }

    /**
     * Actualizar estado de un usuario (activar/desactivar)
     */
    @PostMapping("/usuarios/{id}/estado")
    @ResponseBody
    public Map<String, Object> actualizarEstadoUsuario(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        log.info("🔄 Actualizando estado de usuario ID: {} -> {}", id, active ? "ACTIVO" : "INACTIVO");

        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            user.setActive(active);
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "Estado de usuario actualizado exitosamente");
            response.put("newStatus", active);

            log.info("✅ Estado del usuario actualizado correctamente");

        } catch (Exception e) {
            log.error("❌ Error al actualizar estado del usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al actualizar estado del usuario: " + e.getMessage());
        }

        return response;
    }
}

