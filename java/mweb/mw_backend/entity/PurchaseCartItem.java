package mweb.mw_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "purchase_cart_items")
public class PurchaseCartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;

    // Un producto puede estar en muchos items de carrito
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Un carrito puede tener muchos items
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private PurchaseCart cart;

    // Método para calcular el subtotal del item (con descuento aplicado)
    public Float getSubtotal() {
        Float price = product.getDetailProduct().getPrice();
        Integer discount = product.getDetailProduct().getDiscount();
        Float priceWithDiscount = price * (1 - discount / 100.0f);
        return priceWithDiscount * quantity;
    }
}
