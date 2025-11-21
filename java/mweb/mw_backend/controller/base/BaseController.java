package mweb.mw_backend.controller.base;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.Category;
import mweb.mw_backend.entity.PurchaseCart;
import mweb.mw_backend.entity.User;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.repository.UserRepository;
import mweb.mw_backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Optional;

/**
 * Controlador base que proporciona datos comunes para todas las páginas
 */
@RequiredArgsConstructor
public abstract class BaseController {

    protected final CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CartService cartService;

    /**
     * Agrega atributos globales que necesita el navbar en todas las páginas
     */
    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        // Categorías para el dropdown del navbar
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);

        // Información de autenticación
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && 
                                !auth.getName().equals("anonymousUser");
        
        int cartCount = 0;
        
        if (isAuthenticated) {
            String email = auth.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String fullName = user.getName() + " " + user.getLastName();
                String firstName = user.getName();
                
                model.addAttribute("username", fullName);
                model.addAttribute("userFirstName", firstName);
                model.addAttribute("userEmail", user.getEmail());
                model.addAttribute("userRole", user.getRole().toString());
                model.addAttribute("userAvatar", getAvatarLetter(firstName));
                
                // Obtener el contador del carrito del usuario autenticado
                try {
                    PurchaseCart cart = cartService.getCartByUserId(user.getId());
                    if (cart != null) {
                        cartCount = cart.getTotalItems();
                    }
                } catch (Exception e) {
                    System.err.println("Error al obtener el carrito del usuario: " + e.getMessage());
                    cartCount = 0;
                }
            } else {
                model.addAttribute("username", email);
                model.addAttribute("userFirstName", "Usuario");
                model.addAttribute("userAvatar", "U");
            }
        } else {
            model.addAttribute("username", null);
            model.addAttribute("userFirstName", null);
            model.addAttribute("userEmail", null);
            model.addAttribute("userRole", null);
            model.addAttribute("userAvatar", null);
        }
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("cartCount", cartCount);
    }
    
    /**
     * Obtiene la primera letra del nombre para el avatar
     */
    private String getAvatarLetter(String name) {
        if (name != null && !name.trim().isEmpty()) {
            return name.trim().substring(0, 1).toUpperCase();
        }
        return "U";
    }
}