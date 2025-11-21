package mweb.mw_backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mweb.mw_backend.entity.ProductImage;
import mweb.mw_backend.service.ProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/images")
@RequiredArgsConstructor
@Slf4j
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * Agregar una imagen a un producto
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addImageToProduct(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altText", defaultValue = "") String altText,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary) {
        
        try {
            // Validaciones
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("El archivo está vacío"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(createErrorResponse("El archivo debe ser una imagen"));
            }

            ProductImage productImage = productImageService.addImageToProduct(productId, file, altText, isPrimary);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagen agregada exitosamente");
            response.put("image", createImageResponse(productImage));
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error al subir imagen para producto {}", productId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Error al subir la imagen: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error al procesar imagen para producto {}", productId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Obtener todas las imágenes de un producto
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProductImages(@PathVariable Long productId) {
        try {
            List<ProductImage> images = productImageService.getProductImages(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("images", images.stream().map(this::createImageResponse).toList());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al obtener imágenes del producto {}", productId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Error al obtener las imágenes"));
        }
    }

    /**
     * Eliminar una imagen específica
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Map<String, Object>> deleteProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        
        try {
            productImageService.deleteProductImage(imageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagen eliminada exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error al eliminar imagen {} del producto {}", imageId, productId, e);
            return ResponseEntity.internalServerError().body(createErrorResponse("Error al eliminar la imagen"));
        } catch (RuntimeException e) {
            log.error("Imagen no encontrada: {}", imageId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Establecer una imagen como primaria
     */
    @PutMapping("/{imageId}/primary")
    public ResponseEntity<Map<String, Object>> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        
        try {
            productImageService.setPrimaryImage(imageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagen establecida como primaria");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error al establecer imagen primaria {} del producto {}", imageId, productId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Actualizar el orden de una imagen
     */
    @PutMapping("/{imageId}/order")
    public ResponseEntity<Map<String, Object>> updateImageOrder(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestParam int order) {
        
        try {
            productImageService.updateImageOrder(imageId, order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orden de imagen actualizado");
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error al actualizar orden de imagen {} del producto {}", imageId, productId, e);
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    // Métodos auxiliares

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        return error;
    }

    private Map<String, Object> createImageResponse(ProductImage image) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", image.getId());
        response.put("imageUrl", image.getImageUrl());
        response.put("altText", image.getAltText());
        response.put("isPrimary", image.getIsPrimary());
        response.put("displayOrder", image.getDisplayOrder());
        return response;
    }
}