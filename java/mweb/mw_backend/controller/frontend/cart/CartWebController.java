package mweb.mw_backend.controller.frontend.cart;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.PurchaseCart;
import mweb.mw_backend.entity.User;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/web/cart")
@RequiredArgsConstructor
public class CartWebController {
    
    private final CartService cartService;
    private final CategoryRepository categoryRepository;

    @GetMapping
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        // Datos para el navbar
        model.addAttribute("categories", categoryRepository.findAll());
        
        if (user == null) {
            // Usuario no autenticado - mostrar vista con carrito desde localStorage
            model.addAttribute("cartCount", 0);
            model.addAttribute("isAuthenticated", false);
            model.addAttribute("username", "");
            model.addAttribute("cart", null);
            model.addAttribute("cartTotal", 0.0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("isLocalCart", true);
            return "cart/view";
        }
        
        // Usuario autenticado - cargar desde BD
        PurchaseCart cart = cartService.getCartByUserId(user.getId());
        Double total = cart.calculateTotal();
        Integer totalItems = cart.getTotalItems();
        
        model.addAttribute("cartCount", totalItems);
        model.addAttribute("isAuthenticated", true);
        model.addAttribute("username", user.getName());
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", total);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("isLocalCart", false);
        
        return "cart/view"; // templates/cart/view.html
    }

    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal User user,
                           @RequestParam Long productId,
                           @RequestParam(defaultValue = "1") Integer quantity,
                           @RequestParam(required = false) String returnUrl,
                           RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para agregar productos al carrito");
            return "redirect:" + (returnUrl != null ? returnUrl : "/web/products");
        }
        
        try {
            cartService.addItemToCart(user.getId(), productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Producto agregado al carrito exitosamente");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        // Redirigir a la URL de retorno o a productos
        return "redirect:" + (returnUrl != null ? returnUrl : "/web/products");
    }

    @PostMapping("/update/{itemId}")
    public String updateQuantity(@AuthenticationPrincipal User user,
                                @PathVariable Long itemId,
                                @RequestParam Integer quantity,
                                RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        try {
            cartService.updateItemQuantity(user.getId(), itemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cantidad actualizada");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/web/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItem(@AuthenticationPrincipal User user,
                            @PathVariable Long itemId,
                            RedirectAttributes redirectAttributes) {
        
        System.out.println("=== REMOVE ITEM LLAMADO ===");
        System.out.println("ItemId: " + itemId);
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        
        if (user == null) {
            System.out.println("Usuario no autenticado, redirigiendo a login");
            return "redirect:/web/auth/login";
        }
        
        try {
            cartService.removeItemFromCart(user.getId(), itemId);
            redirectAttributes.addFlashAttribute("success", "Producto eliminado del carrito");
            System.out.println("Item eliminado exitosamente");
        } catch (RuntimeException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/web/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal User user,
                           RedirectAttributes redirectAttributes) {
        
        System.out.println("=== CLEAR CART LLAMADO ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        
        if (user == null) {
            System.out.println("Usuario no autenticado, redirigiendo a login");
            return "redirect:/web/auth/login";
        }
        
        cartService.clearCart(user.getId());
        redirectAttributes.addFlashAttribute("success", "Carrito limpiado");
        System.out.println("Carrito limpiado exitosamente");
        
        return "redirect:/web/cart";
    }

    /**
     * Sincroniza el carrito del localStorage con la base de datos
     * Se llama después del login/registro
     */
    @PostMapping("/sync")
    @ResponseBody
    public ResponseEntity<?> syncCart(@AuthenticationPrincipal User user,
                                     @RequestBody List<Map<String, Object>> cartItems) {
        
        if (user == null) {
            return ResponseEntity.status(401).body("Usuario no autenticado");
        }
        
        try {
            for (Map<String, Object> item : cartItems) {
                Long productId = Long.valueOf(item.get("productId").toString());
                Integer quantity = Integer.valueOf(item.get("quantity").toString());
                
                cartService.addItemToCart(user.getId(), productId, quantity);
            }
            
            return ResponseEntity.ok("Carrito sincronizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al sincronizar: " + e.getMessage());
        }
    }
    
    /**
     * Añadir producto al carrito vía AJAX (devuelve JSON)
     */
    @PostMapping("/add-ajax")
    @ResponseBody
    public ResponseEntity<?> addToCartAjax(@AuthenticationPrincipal User user,
                                          @RequestParam Long productId,
                                          @RequestParam(defaultValue = "1") Integer quantity) {
        
        System.out.println("=== ADD TO CART AJAX LLAMADO ===");
        System.out.println("User: " + (user != null ? user.getEmail() : "null"));
        System.out.println("ProductId: " + productId);
        System.out.println("Quantity: " + quantity);
        
        if (user == null) {
            System.out.println("Usuario no autenticado");
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Debes iniciar sesión para agregar productos al carrito",
                "requiresLogin", true
            ));
        }
        
        try {
            cartService.addItemToCart(user.getId(), productId, quantity);
            
            // Refrescar el carrito para obtener el conteo actualizado
            PurchaseCart cart = cartService.getCartByUserId(user.getId());
            
            Integer totalItems = cart != null ? cart.getTotalItems() : 0;
            Double total = cart != null ? cart.calculateTotal() : 0.0;
            
            System.out.println("Producto agregado exitosamente");
            System.out.println("Total items en carrito: " + totalItems);
            System.out.println("Total del carrito: " + total);
            System.out.println("Items en la lista: " + (cart != null ? cart.getPurchaseCartItems().size() : 0));
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Producto agregado al carrito exitosamente",
                "cartCount", totalItems,
                "cartTotal", total
            ));
        } catch (RuntimeException e) {
            System.out.println("Error al agregar al carrito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }
}
