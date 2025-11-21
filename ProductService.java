package mweb.mw_backend.service;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.Product;
import mweb.mw_backend.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductRepository productRepository;
    
    /**
     * Obtiene todos los productos con paginación
     */
    public Page<Product> findAllProducts(Pageable pageable) {
        logger.info("=== PRODUCT SERVICE - findAllProducts ===");
        logger.info("Request pageable: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        // Primero contar total de productos
        long totalCount = productRepository.count();
        logger.info("Total productos en BD: {}", totalCount);
        
        Page<Product> result = productRepository.findAll(pageable);
        logger.info("Productos obtenidos: {} de {} totales", 
                   result.getContent().size(), result.getTotalElements());
        
        if (result.hasContent()) {
            logger.info("Primer producto: {}", result.getContent().get(0).getName());
        } else {
            logger.warn("No hay productos en la página solicitada");
        }
        
        return result;
    }
    
    /**
     * Obtiene productos por categoría con paginación
     */
    public Page<Product> findProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    /**
     * Busca un producto por ID
     */
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * Obtiene un producto por ID o lanza excepción si no existe
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }
    
    /**
     * Obtiene productos relacionados de la misma categoría (excluyendo el producto actual)
     */
    public List<Product> findRelatedProducts(Long productId, Long categoryId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByCategoryIdAndIdNot(categoryId, productId, pageable)
                .getContent();
    }
    
    /**
     * Obtiene productos relacionados de la misma categoría (excluyendo el producto actual)
     * Con límite por defecto de 4 productos
     */
    public List<Product> findRelatedProducts(Long productId, Long categoryId) {
        return findRelatedProducts(productId, categoryId, 4);
    }
    
    /**
     * Verifica si un producto existe
     */
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }
    
    /**
     * Cuenta el total de productos
     */
    public long countAllProducts() {
        return productRepository.count();
    }
    
    /**
     * Cuenta productos por categoría
     */
    public long countProductsByCategory(Long categoryId) {
        return productRepository.countByCategoryId(categoryId);
    }
    
    /**
     * Busca productos por término de búsqueda (nombre o descripción)
     */
    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllProducts(pageable);
        }
        return productRepository.findBySearchTerm(searchTerm.trim(), pageable);
    }
    
    /**
     * Busca productos por categoría y término de búsqueda
     */
    public Page<Product> searchProductsByCategory(Long categoryId, String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findProductsByCategory(categoryId, pageable);
        }
        return productRepository.findByCategoryIdAndSearchTerm(categoryId, searchTerm.trim(), pageable);
    }
    
    /**
     * Obtiene productos activos solamente
     */
    public Page<Product> findActiveProducts(Pageable pageable) {
        return productRepository.findByProductStatus(mweb.mw_backend.enumeration.ProductStatus.ACTIVE, pageable);
    }
    
    /**
     * Crea un nuevo producto con código aleatorio de 9 dígitos
     */
    public Product createProduct(Product product) {
        // Generar código único de 9 dígitos
        product.setCode(generateUniqueProductCode());
        return productRepository.save(product);
    }
    
    /**
     * Actualiza un producto existente
     */
    public Product updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo para actualizar");
        }
        
        if (!productRepository.existsById(product.getId())) {
            throw new RuntimeException("El producto con ID " + product.getId() + " no existe");
        }
        
        // Si el código es nulo, generar uno nuevo
        if (product.getCode() == null) {
            product.setCode(generateUniqueProductCode());
        }
        
        return productRepository.save(product);
    }
    
    /**
     * Elimina un producto por ID
     */
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("El producto con ID " + id + " no existe");
        }
        productRepository.deleteById(id);
    }
    
    /**
     * Genera un código único de 9 dígitos para el producto
     * Verifica que no exista otro producto con el mismo código
     */
    private Long generateUniqueProductCode() {
        Random random = new Random();
        Long code;
        
        do {
            // Generar número aleatorio de 9 dígitos (100000000 - 999999999)
            code = 100000000L + (long)(random.nextDouble() * 900000000L);
        } while (productRepository.existsByCode(code)); // Verificar que sea único
        
        return code;
    }

    /**
     * Obtiene todos los productos sin paginación
     */
    public List<Product> getAllProducts() {
        logger.info("Obteniendo todos los productos para selector");
        List<Product> products = productRepository.findAll();
        logger.info("Productos encontrados: {}", products.size());
        return products;
    }
    public List<Product> findAll() {
        return productRepository.findAll();
    }
}