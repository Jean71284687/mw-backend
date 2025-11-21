package mweb.mw_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import mweb.mw_backend.enumeration.PayStatus;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pays")
public class Pay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_method")
    @NotNull(message = "No puede ser nulo")
    @NotBlank(message = "No puede estar en blanco")
    private String paymentMethod;

    /*Monto sin impuestos*/
    @Column(name = "subtotal")
    @NotNull(message = "No puede ser nulo")
    @NotBlank(message = "No puede estar en blanco")
    @Min(value = 0, message = "El subtotal debe ser un valor positivo")
    private Double subtotal;

    @Column(name = "igv")
    @Min(value = 0, message = "El igv debe ser un valor positivo")
    private Double igv;
    /*Monto + IGV*/
    @Column(name = "total")
    @NotNull(message = "El total no puede ser nulo")
    @NotBlank(message = "El total no puede estar en blanco")
    @Min(value = 0, message = "El total debe ser un valor positivo")
    private Double total;

    @Column(name = "shipping_date", nullable = true)
    @NotBlank(message = "La fecha de envío no puede estar en blanco")
    private LocalDate shippingDate;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_status", nullable = false)
    private PayStatus payStatus;
}
