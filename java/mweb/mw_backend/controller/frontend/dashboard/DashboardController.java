package mweb.mw_backend.controller.frontend.dashboard;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("admin/dashboard")
public class DashboardController {

    /**
     * Página principal del dashboard
     */
    @GetMapping
    public String dashboard(Model model) {
        System.out.println("🔵 Accediendo a /dashboard");
        
        try {
            // Dashboard no necesita categorías porque no tiene navbar de tienda 
            // Es una interfaz administrativa independiente
            
            log.info("✅ Renderizando dashboard/home");
            return "dashboard/home";
            
        } catch (Exception e) {
            log.error("❌ Error al cargar dashboard: {}", e.getMessage(), e);
            return "error/500"; // O una página de error personalizada
        }
    }

    /**
     * Endpoint alternativo con /home por si se accede directamente
     */
    @GetMapping("/home")
    public String dashboardHome(Model model) {
        log.info("🔵 Accediendo a /dashboard/home (redirigiendo)");
        return dashboard(model);
    }
}