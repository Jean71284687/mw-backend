package mweb.mw_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mweb.mw_backend.entity.Product;
import mweb.mw_backend.entity.ProductImage;
import mweb.mw_backend.repository.ProductImageRepository;
import mweb.mw_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    /**
     * Agrega una imagen a un producto
     */
    @Transactional
    public ProductImage addImageToProduct(Long productId, MultipartFile file, String altText, boolean isPrimary) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productId));

        // Si es imagen primaria, quitar la marca primaria de otras imágenes
        if (isPrimary) {
            removeCurrentPrimaryImage(productId);
        }

        // Subir imagen a Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file, "products");
        String publicId = cloudinaryService.extractPublicId(imageUrl);

        // Obtener el próximo orden de visualización
        int nextOrder = getNextDisplayOrder(productId);

        // Crear y guardar la nueva imagen
        ProductImage productImage = ProductImage.builder()
                .imageUrl(imageUrl)
                .altText(altText)
                .isPrimary(isPrimary)
                .displayOrder(nextOrder)
                .cloudinaryPublicId(publicId)
                .product(product)
                .build();

        return productImageRepository.save(productImage);
    }

    /**
     * Obtiene todas las imágenes de un producto ordenadas por displayOrder
     */
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    }

    /**
     * Obtiene la imagen primaria de un producto
     */
    public Optional<ProductImage> getPrimaryImage(Long productId) {
        return productImageRepository.findByProductIdAndIsPrimaryTrue(productId);
    }

    /**
     * Elimina una imagen de producto
     */
    @Transactional
    public void deleteProductImage(Long imageId) throws IOException {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + imageId));

        // Eliminar de Cloudinary
        if (productImage.getCloudinaryPublicId() != null) {
            try {
                cloudinaryService.deleteImage(productImage.getCloudinaryPublicId());
            } catch (IOException e) {
                log.warn("No se pudo eliminar la imagen de Cloudinary: {}", e.getMessage());
            }
        }

        // Eliminar de la base de datos
        productImageRepository.delete(productImage);
    }

    /**
     * Establece una imagen como primaria
     */
    @Transactional
    public void setPrimaryImage(Long imageId) {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + imageId));

        // Quitar marca primaria de otras imágenes del mismo producto
        removeCurrentPrimaryImage(productImage.getProduct().getId());

        // Establecer como primaria
        productImage.setIsPrimary(true);
        productImageRepository.save(productImage);
    }

    /**
     * Actualiza el orden de las imágenes
     */
    @Transactional
    public void updateImageOrder(Long imageId, int newOrder) {
        ProductImage productImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Imagen no encontrada con ID: " + imageId));

        productImage.setDisplayOrder(newOrder);
        productImageRepository.save(productImage);
    }

    /**
     * Elimina todas las imágenes de un producto
     */
    @Transactional
    public void deleteAllProductImages(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        
        // Eliminar de Cloudinary
        for (ProductImage image : images) {
            if (image.getCloudinaryPublicId() != null) {
                try {
                    cloudinaryService.deleteImage(image.getCloudinaryPublicId());
                } catch (IOException e) {
                    log.warn("No se pudo eliminar la imagen de Cloudinary: {}", e.getMessage());
                }
            }
        }

        // Eliminar de la base de datos
        productImageRepository.deleteByProductId(productId);
    }

    // Métodos privados auxiliares

    private void removeCurrentPrimaryImage(Long productId) {
        Optional<ProductImage> currentPrimary = productImageRepository.findByProductIdAndIsPrimaryTrue(productId);
        if (currentPrimary.isPresent()) {
            ProductImage current = currentPrimary.get();
            current.setIsPrimary(false);
            productImageRepository.save(current);
        }
    }

    private int getNextDisplayOrder(Long productId) {
        List<ProductImage> existingImages = productImageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        return existingImages.size();
    }
}