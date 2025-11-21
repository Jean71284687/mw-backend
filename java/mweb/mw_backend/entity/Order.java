package mweb.mw_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import mweb.mw_backend.enumeration.OrderStatus;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name= "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name= "total", nullable = false)
    private Double total;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    // Un usuario puede tener muchos pedidos
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Un pedido tiene varias líneas de detalle
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailOrder> detailOrderList;

    // Relación 1:1 con Pago
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pay_id", referencedColumnName = "id")
    private Pay pay;

    // Relación 1:1 con Envío
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipment_id", referencedColumnName = "id")
    private Shipment shipment;

    // Relación con cupón (opcional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

}
