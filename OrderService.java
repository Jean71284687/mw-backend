package mweb.mw_backend.service;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.*;
import mweb.mw_backend.enumeration.OrderStatus;
import mweb.mw_backend.enumeration.PayStatus;
import mweb.mw_backend.enumeration.ShippingStatus;
import mweb.mw_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final PurchaseCartRepository cartRepository;
    private final DetailOrderRepository detailOrderRepository;
    private final PayRepository payRepository;
    private final ShipmentRepository shipmentRepository;
    private final InventoryRepository inventoryRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public Order createOrderFromCart(Long userId, String paymentMethod, String shippingAddress, 
                                   String city, String zipCode, String couponCode) {
        // Obtener carrito activo
        PurchaseCart cart = cartRepository.findByUserIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("No hay carrito activo"));

        if (cart.getPurchaseCartItems().isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // Verificar stock de todos los productos
        for (PurchaseCartItem cartItem : cart.getPurchaseCartItems()) {
            Inventory inventory = inventoryRepository.findByProductId(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado para producto: " + cartItem.getProduct().getName()));
            
            if (!inventory.isAvailable(cartItem.getQuantity().longValue())) {
                throw new RuntimeException("Stock insuficiente para producto: " + cartItem.getProduct().getName() + 
                                         ". Disponible: " + inventory.getCurrentStock());
            }
        }

        // Calcular total
        Double subtotal = cart.calculateTotal();
        Double discountAmount = 0.0;
        Coupon appliedCoupon = null;

        // Aplicar cupón si se proporciona
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            appliedCoupon = couponRepository.findValidCouponByCode(couponCode, LocalDate.now())
                    .orElseThrow(() -> new RuntimeException("Cupón inválido o expirado"));

            if (appliedCoupon.getMinimumAmount() != null && subtotal < appliedCoupon.getMinimumAmount()) {
                throw new RuntimeException("El monto mínimo para usar este cupón es: " + appliedCoupon.getMinimumAmount());
            }

            // Calcular descuento
            switch (appliedCoupon.getCouponType()) {
                case PERCENTAGE:
                    discountAmount = subtotal * (appliedCoupon.getDiscountValue() / 100);
                    break;
                case FIXED_AMOUNT:
                    discountAmount = Math.min(appliedCoupon.getDiscountValue(), subtotal);
                    break;
            }
        }

        Double igv = (subtotal - discountAmount) * 0.18; // 18% IGV
        Double total = subtotal - discountAmount + igv;

        // Crear la orden
        Order order = Order.builder()
                .user(cart.getUser())
                .createdDate(LocalDate.now())
                .total(total)
                .orderStatus(OrderStatus.PENDING)
                .coupon(appliedCoupon)
                .discountAmount(discountAmount)
                .build();

        order = orderRepository.save(order);

        // Crear detalles de la orden
        List<DetailOrder> orderDetails = new ArrayList<>();
        for (PurchaseCartItem cartItem : cart.getPurchaseCartItems()) {
            DetailOrder detail = DetailOrder.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getProduct().getDetailProduct().getPrice().doubleValue())
                    .build();
            orderDetails.add(detail);
        }
        detailOrderRepository.saveAll(orderDetails);
        order.setDetailOrderList(orderDetails);

        // Crear pago
        Pay payment = Pay.builder()
                .order(order)
                .paymentMethod(paymentMethod)
                .subtotal(subtotal)
                .igv(igv)
                .total(total)
                .shippingDate(LocalDate.now().plusDays(1))
                .build();
        payRepository.save(payment);
        order.setPay(payment);

        // Crear envío
        Shipment shipment = Shipment.builder()
                .order(order)
                .mailingAddress(shippingAddress)
                .city(city)
                .zipCode(zipCode)
                .shippingStatus(ShippingStatus.PENDING)
                .build();
        shipmentRepository.save(shipment);
        order.setShipment(shipment);

        // Actualizar stock en inventario
        for (PurchaseCartItem cartItem : cart.getPurchaseCartItems()) {
            Inventory inventory = inventoryRepository.findByProductId(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
            inventory.setCurrentStock(inventory.getCurrentStock() - cartItem.getQuantity());
            inventoryRepository.save(inventory);
        }

        // Actualizar uso del cupón
        if (appliedCoupon != null) {
            appliedCoupon.setTimesUsed(appliedCoupon.getTimesUsed() + 1);
            couponRepository.save(appliedCoupon);
        }

        // Limpiar carrito
        cart.setIsActive(false);
        cartRepository.save(cart);

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        
        order.setOrderStatus(newStatus);
        
        // Si se marca como entregado, actualizar fecha de entrega
        if (newStatus == OrderStatus.DELIVERED && order.getShipment() != null) {
            order.getShipment().setShippingStatus(ShippingStatus.DELIVERED);
            order.getShipment().setDeliveryDate(LocalDate.now());
            shipmentRepository.save(order.getShipment());
        }
        
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
    }
}