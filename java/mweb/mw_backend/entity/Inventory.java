package mweb.mw_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La cantidad actual no puede ser nula")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    @Column(name = "current_stock", nullable = false)
    private Long currentStock;

    @NotNull(message = "El stock mínimo no puede ser nulo")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "minimum_stock", nullable = false)
    private Long minimumStock;

    @NotNull(message = "El stock máximo no puede ser nulo")
    @Min(value = 1, message = "El stock máximo debe ser al menos 1")
    @Column(name = "maximum_stock", nullable = false)
    private Long maximumStock;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Método para verificar si está bajo stock
    public boolean isLowStock() {
        return currentStock <= minimumStock;
    }

    // Método para verificar disponibilidad
    public boolean isAvailable(Long quantity) {
        return currentStock >= quantity;
    }
}