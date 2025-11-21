package mweb.mw_backend.controller.frontend.home;

import mweb.mw_backend.controller.base.BaseController;
import mweb.mw_backend.entity.Product;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController extends BaseController {

    private final ProductRepository productRepository;

    public HomeController(CategoryRepository categoryRepository, ProductRepository productRepository) {
        super(categoryRepository);
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Obtener productos destacados (los más recientes)
        List<Product> featuredProducts = productRepository.findTop8ByOrderByIdDesc();
        
        // Agregar productos destacados al modelo
        // (Las variables del navbar ya se agregan automáticamente por @ModelAttribute en BaseController)
        model.addAttribute("featuredProducts", featuredProducts);
        
        return "index"; // templates/index.html
    }
    
    /**
     * Endpoint de prueba para verificar carga de CSS
     */
    @GetMapping("/test-css")
    public String testCss() {
        return "test-css"; // templates/test-css.html
    }
}