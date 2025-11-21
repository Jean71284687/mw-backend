package mweb.mw_backend.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import mweb.mw_backend.controller.base.BaseController;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.service.ProductService; // ✅ IMPORTAR
import mweb.mw_backend.entity.Product; // ✅ IMPORTAR

@Controller
@RequestMapping("/admin/dashboard")
public class AdminDashboardController extends BaseController {
    private final ProductService productService;
    public AdminDashboardController(CategoryRepository categoryRepository,ProductService productService) {
      super(categoryRepository);
        this.productService = productService;
    }

    // Dashboard principal - solo tablero general
    @GetMapping({"/"})
    public String dashboard(Model model) {
        return "dashboard/home";
    }

    // Página de gestión de usuarios
    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        // TODO: Implementar cuando UserService esté disponible
        // model.addAttribute("usuarios", userService.findAll());
        return "dashboard/usuarios";
    }

    // Página de gestión de productos


    // Página de gestión de categorías
    @GetMapping("/categorias")
    public String categorias(Model model) {
        // TODO: Implementar cuando CategoryService esté completamente configurado
        // model.addAttribute("categorias", categoryService.findAll());
        return "dashboard/categorias";
    }

    // Página de perfil del usuario
    @GetMapping("/perfil")
    public String perfil(Model model) {
        return "dashboard/perfil";
    }
}