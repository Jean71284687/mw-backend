package mweb.mw_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detail_orders")
public class DetailOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 0, message = "La cantidad debe ser un valor positivo")
    @Column(name="quantity", nullable = false)
    private Integer quantity;

    @Min(value = 0, message = "El precio debe ser un valor positivo")
    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    // Muchas líneas de pedido pueden pertenecer a un mismo producto
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Muchas líneas de pedido pertenecen a un pedido
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
