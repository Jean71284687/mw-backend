package mweb.mw_backend.controller.frontend.product;

import mweb.mw_backend.controller.base.BaseController;
import mweb.mw_backend.entity.Category;
import mweb.mw_backend.entity.Product;
import mweb.mw_backend.entity.Review;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.service.ProductService;
import mweb.mw_backend.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/web/products")
public class ProductWebController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductWebController.class);
    
    private final ProductService productService;
    private final ReviewService reviewService;

    public ProductWebController(CategoryRepository categoryRepository, 
                               ProductService productService, 
                               ReviewService reviewService) {
        super(categoryRepository);
        this.productService = productService;
        this.reviewService = reviewService;
    }

    @GetMapping("/images")
    public String imageManager(Model model) {
        logger.info("Accediendo a gestión de imágenes de productos");
        
        // Obtener todos los productos para el selector
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        
        return "products/image-manager";
    }

    @GetMapping
    public String productList(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) String search,
                             Model model) {
        
        logger.info("=== PRODUCT LIST DEBUG ===");
        logger.info("Parámetros recibidos - page: {}, size: {}, categoryId: {}, search: '{}'", 
                   page, size, categoryId, search);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products;
        
        // Filtrado combinado: categoría y búsqueda
        if (categoryId != null && search != null && !search.trim().isEmpty()) {
            logger.info("Búsqueda por categoría y término: categoryId={}, search='{}'", categoryId, search);
            products = productService.searchProductsByCategory(categoryId, search, pageable);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("searchTerm", search);
        } else if (categoryId != null) {
            logger.info("Búsqueda por categoría: categoryId={}", categoryId);
            products = productService.findProductsByCategory(categoryId, pageable);
            model.addAttribute("selectedCategoryId", categoryId);
        } else if (search != null && !search.trim().isEmpty()) {
            logger.info("Búsqueda por término: search='{}'", search);
            products = productService.searchProducts(search, pageable);
            model.addAttribute("searchTerm", search);
        } else {
            logger.info("Obteniendo todos los productos");
            products = productService.findAllProducts(pageable);
        }
        
        logger.info("Productos encontrados: {}", products != null ? products.getTotalElements() : "null");
        logger.info("Páginas totales: {}", products != null ? products.getTotalPages() : "null");
        logger.info("Contenido de la página actual: {}", 
                   products != null && products.hasContent() ? products.getContent().size() : "sin contenido");
        
        // Información para la vista (las categorías ya se agregan automáticamente por BaseController)
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products != null ? products.getTotalPages() : 0);
        model.addAttribute("totalElements", products != null ? products.getTotalElements() : 0);
        model.addAttribute("pageSize", size);
        
        // Mantener parámetros para la paginación
        model.addAttribute("categoryParam", categoryId);
        model.addAttribute("searchParam", search);
        
        logger.info("=== MODELO FINAL ===");
        logger.info("Template que se va a renderizar: products/list");
        
        return "products/list-clean"; // Cambiado para usar el template correcto
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        
        List<Review> reviews = reviewService.getReviewsByProduct(id);
        Double averageRating = reviewService.getAverageRatingByProduct(id);
        Long reviewCount = reviewService.getReviewCountByProduct(id);
        
        // Productos relacionados de la misma categoría
        List<Product> relatedProducts = productService.findRelatedProducts(id, product.getCategory().getId());
        
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("relatedProducts", relatedProducts);
        
        return "products/detail"; // templates/products/detail.html
    }

    @GetMapping("/detail/{id}")
    public String productDetailAlt(@PathVariable Long id, Model model) {
        return productDetail(id, model);
    }

    @GetMapping("/category/{categoryId}")
    public String productsByCategory(@PathVariable Long categoryId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   Model model) {
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productService.findProductsByCategory(categoryId, pageable);
        
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        
        return "products/category"; // templates/products/category.html
    }
    
    /**
     * Endpoint para búsqueda AJAX (opcional para mejorar UX)
     */
    @GetMapping("/search")
    @ResponseBody
    public Page<Product> searchProductsAjax(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "12") int size,
                                           @RequestParam(required = false) Long categoryId,
                                           @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        if (categoryId != null && search != null && !search.trim().isEmpty()) {
            return productService.searchProductsByCategory(categoryId, search, pageable);
        } else if (categoryId != null) {
            return productService.findProductsByCategory(categoryId, pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            return productService.searchProducts(search, pageable);
        } else {
            return productService.findAllProducts(pageable);
        }
    }
}