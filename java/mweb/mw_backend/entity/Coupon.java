package mweb.mw_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import mweb.mw_backend.enumeration.CouponType;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El código no puede ser nulo")
    @NotBlank(message = "El código no puede estar en blanco")
    @Size(max = 50, message = "El código no puede exceder los 50 caracteres")
    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @NotNull(message = "La descripción no puede ser nula")
    @NotBlank(message = "La descripción no puede estar en blanco")
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false)
    private CouponType couponType;

    @NotNull(message = "El valor del descuento no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El valor del descuento debe ser positivo")
    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @DecimalMin(value = "0.0", message = "El monto mínimo debe ser positivo")
    @Column(name = "minimum_amount")
    private Double minimumAmount;

    @NotNull(message = "La fecha de inicio no puede ser nula")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin no puede ser nula")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "El límite de uso no puede ser nulo")
    @Min(value = 1, message = "El límite de uso debe ser al menos 1")
    @Column(name = "usage_limit", nullable = false)
    private Integer usageLimit;

    @Column(name = "times_used", nullable = false)
    private Integer timesUsed = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Método para verificar si el cupón está válido
    public boolean isValid() {
        LocalDate now = LocalDate.now();
        return isActive && 
               now.isAfter(startDate.minusDays(1)) && 
               now.isBefore(endDate.plusDays(1)) && 
               timesUsed < usageLimit;
    }
}