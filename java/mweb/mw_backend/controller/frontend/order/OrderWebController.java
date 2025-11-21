package mweb.mw_backend.controller.frontend.order;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.Order;
import mweb.mw_backend.entity.PurchaseCart;
import mweb.mw_backend.entity.User;
import mweb.mw_backend.service.CartService;
import mweb.mw_backend.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/web/orders")
@RequiredArgsConstructor
public class OrderWebController {
    
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping("/checkout")
    public String checkoutForm(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        PurchaseCart cart = cartService.getCartByUserId(user.getId());
        
        if (cart.getPurchaseCartItems().isEmpty()) {
            return "redirect:/web/cart?empty=true";
        }
        
        Double total = cart.calculateTotal();
        Integer totalItems = cart.getTotalItems();
        
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", total);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("user", user);
        
        return "orders/checkout"; // templates/orders/checkout.html
    }

    @PostMapping("/create")
    public String createOrder(@AuthenticationPrincipal User user,
                             @RequestParam String paymentMethod,
                             @RequestParam String shippingAddress,
                             @RequestParam String city,
                             @RequestParam String zipCode,
                             @RequestParam(required = false) String couponCode,
                             RedirectAttributes redirectAttributes) {
        
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        try {
            Order order = orderService.createOrderFromCart(
                user.getId(), 
                paymentMethod, 
                shippingAddress, 
                city, 
                zipCode, 
                couponCode
            );
            
            redirectAttributes.addFlashAttribute("success", "¡Pedido creado exitosamente!");
            return "redirect:/web/orders/" + order.getId();
            
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/orders/checkout";
        }
    }

    @GetMapping("/{orderId}")
    public String orderDetail(@AuthenticationPrincipal User user,
                             @PathVariable Long orderId,
                             Model model) {
        
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        try {
            Order order = orderService.getOrderById(orderId);
            
            // Verificar que la orden pertenece al usuario actual
            if (!order.getUser().getId().equals(user.getId())) {
                return "redirect:/web/orders?error=unauthorized";
            }
            
            model.addAttribute("order", order);
            return "orders/detail"; // templates/orders/detail.html
            
        } catch (RuntimeException e) {
            return "redirect:/web/orders?error=notfound";
        }
    }

    @GetMapping
    public String orderHistory(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/web/auth/login";
        }
        
        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        model.addAttribute("orders", orders);
        
        return "orders/history"; // templates/orders/history.html
    }
}