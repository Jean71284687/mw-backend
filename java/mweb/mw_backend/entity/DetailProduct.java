package mweb.mw_backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import mweb.mw_backend.enumeration.ProductCondition;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detail_products")
public class DetailProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre comercial no puede ser nulo")
    @NotBlank(message = "El nombre comercial no puede estar en blanco")
    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres")
    @Column(name = "commercial_name")
    private String commercialName;

    @NotNull(message = "La descripción no puede ser nulo")
    @NotBlank(message = "La descripción no puede estar en blanco")
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    @Column(name = "description")
    private String description;

    @NotNull(message = "La marca no puede ser nulo")
    @NotBlank(message = "La marca no puede estar en blanco")
    @Size(max = 30, message = "La marca no puede exceder los 30 caracteres")
    @Column(name = "brand")
    private String brand;

    @NotNull(message = "El modelo no puede ser nulo")
    @NotBlank(message = "El modelo no puede estar en blanco")
    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres")
    @Column(name = "model")
    private String model;


    @Column(name = "imageUrl")
    private String imageUrl;

    @NotNull(message = "Las especificaciones no puede ser nulo")
    @NotBlank(message = "Las especificaciones no pueden estar en blanco")
    @Size(max = 255, message = "Las especificaciones no pueden exceder los 255 caracteres")
    @Column(name = "specifications")
    private String specifications;

    @NotNull(message = "El descuento no puede ser nulo")
    @NotBlank(message = "El descuento no puede estar en blanco")
    @Min(value = 0, message = "El descuento debe ser un valor positivo")
    @Max(value = 100, message = "El descuento no puede exceder el 100%")
    @Column(name = "discount")
    private Integer discount;

    @Column(name = "price", nullable = false)
    private Float price;

    @OneToOne
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;


}
