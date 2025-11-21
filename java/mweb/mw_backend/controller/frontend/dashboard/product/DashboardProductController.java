package mweb.mw_backend.controller.frontend.dashboard.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mweb.mw_backend.entity.Product;
import mweb.mw_backend.entity.DetailProduct;
import mweb.mw_backend.entity.Category;
import mweb.mw_backend.entity.Inventory;
import mweb.mw_backend.enumeration.ProductStatus;
import mweb.mw_backend.enumeration.ProductCondition;
import mweb.mw_backend.repository.ProductRepository;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.repository.DetailProductRepository;
import mweb.mw_backend.repository.InventoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import mweb.mw_backend.service.ProductService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DetailProductRepository detailProductRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Página principal de gestión de productos
     */
    // En AdminDashboardController - ELIMINAR o COMENTAR este método:
    @GetMapping("/productos")
    public String productos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model
    ) {
        log.info("📦 Accediendo a gestión de productos - Página: {}, Tamaño: {}", page, size);

        try {
            // Crear paginación ordenada por ID descendente
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            // Obtener productos paginados
            Page<Product> productosPage = productRepository.findAll(pageable);

            // Calcular estadísticas
            Map<String, Long> stats = calculateProductStats();

            // Obtener lista de categorías
            List<Category> categorias = categoryRepository.findAll();

            // Agregar datos al modelo
            model.addAttribute("productosPage", productosPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productosPage.getTotalPages());
            model.addAttribute("totalProductos", productosPage.getTotalElements());
            model.addAttribute("stats", stats);
            model.addAttribute("categorias", categorias);
            model.addAttribute("currentSearch", search);

            log.info("✅ Productos cargados exitosamente: {} productos encontrados", productosPage.getTotalElements());
            return "dashboard/productos";

        } catch (Exception e) {
            log.error("❌ Error al cargar productos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar los productos");
            return "dashboard/productos";
        }
    }

    /**
     * Ver detalles de un producto específico
     */
    @GetMapping("/productos/{id}")
    @ResponseBody
    public Map<String, Object> verProducto(@PathVariable Long id) {
        log.info("👁️ Viendo detalles de producto ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Product> productoOpt = productRepository.findById(id);
            if (!productoOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return response;
            }

            Product producto = productoOpt.get();

            // Preparar datos para la respuesta usando los métodos REALES
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", producto.getId());
            productData.put("nombre", producto.getName());
            productData.put("codigo", producto.getCode());

            // Métodos CORRECTOS según tu entidad
            productData.put("stock", producto.getStock()); // Este método SÍ existe en tu Product
            productData.put("condicion", producto.getProductCondition().name()); // getProductCondition()
            productData.put("estado", producto.getProductStatus().name()); // getProductStatus()
            productData.put("activo", producto.getProductStatus() == ProductStatus.ACTIVE);

            // Buscar DetailProduct usando Optional
            Optional<DetailProduct> detalleOpt = detailProductRepository.findByProductId(id);
            if (detalleOpt.isPresent()) {
                DetailProduct detalle = detalleOpt.get();
                productData.put("descripcion", detalle.getDescription());
                productData.put("precio", detalle.getPrice()); // Float, no Double
                productData.put("imagenUrl", detalle.getImageUrl());
                productData.put("marca", detalle.getBrand());
                productData.put("modelo", detalle.getModel());
                productData.put("nombreComercial", detalle.getCommercialName());
            } else {
                productData.put("descripcion", "Sin descripción");
                productData.put("precio", 0.0f);
                productData.put("imagenUrl", "");
                productData.put("marca", "");
                productData.put("modelo", "");
                productData.put("nombreComercial", producto.getName());
            }

            // Categoría
            if (producto.getCategory() != null) {
                productData.put("categoria", producto.getCategory().getName());
                productData.put("categoriaId", producto.getCategory().getId());
            } else {
                productData.put("categoria", "Sin categoría");
                productData.put("categoriaId", null);
            }

            response.put("success", true);
            response.put("producto", productData);

            log.info("✅ Detalles de producto cargados exitosamente");
            return response;

        } catch (Exception e) {
            log.error("❌ Error al cargar detalles de producto: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al cargar el producto: " + e.getMessage());
            return response;
        }
    }

    /**
     * Cambiar estado de un producto
     */
    @PostMapping("/productos/{id}/estado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(
            @PathVariable Long id,
            @RequestParam boolean activo
    ) {
        log.info("🔄 Cambiando estado de producto ID: {} a {}", id, activo);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Product> productOpt = productRepository.findById(id);
            if (!productOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return response;
            }

            Product product = productOpt.get();
            // Método CORRECTO: setProductStatus(), no setStatus()
            product.setProductStatus(activo ? ProductStatus.ACTIVE : ProductStatus.INACTIVE);
            productRepository.save(product);

            response.put("success", true);
            response.put("message", "Estado actualizado exitosamente");
            response.put("nuevoEstado", activo);

            log.info("✅ Estado de producto actualizado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al cambiar estado de producto: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al cambiar el estado: " + e.getMessage());
        }

        return response;
    }

    /**
     * Crear o actualizar un producto
     */
    @PostMapping("/productos")
    @ResponseBody
    public Map<String, Object> guardarProducto(@RequestBody Map<String, Object> productData) {
        log.info("💾 Guardando producto");

        Map<String, Object> response = new HashMap<>();

        try {
            Product product;
            DetailProduct detailProduct;
            boolean isNew = false;

            // Verificar si es nuevo o edición
            if (productData.get("id") != null && !productData.get("id").toString().isEmpty()) {
                // Edición
                Long productId = Long.valueOf(productData.get("id").toString());
                product = productRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                // Buscar DetailProduct existente
                Optional<DetailProduct> detalleOpt = detailProductRepository.findByProductId(productId);
                if (detalleOpt.isPresent()) {
                    detailProduct = detalleOpt.get();
                } else {
                    detailProduct = new DetailProduct();
                    detailProduct.setProduct(product);
                }
            } else {
                // Nuevo producto
                product = new Product();
                detailProduct = new DetailProduct();
                detailProduct.setProduct(product);
                isNew = true;

                // Generar código único para nuevo producto
                Long maxCode = productRepository.findMaxCode().orElse(10000L);
                product.setCode(maxCode + 1);
            }

            // Actualizar campos básicos del Product - MÉTODOS CORRECTOS
            product.setName((String) productData.get("nombre"));

            // Estado del producto - MÉTODO CORRECTO
            Boolean activo = (Boolean) productData.get("activo");
            product.setProductStatus(activo ? ProductStatus.ACTIVE : ProductStatus.INACTIVE);

            // Condición del producto - MÉTODO CORRECTO
            String condicionStr = (String) productData.get("condicion");
            product.setProductCondition(ProductCondition.valueOf(condicionStr));

            // Categoría
            Long categoriaId = Long.valueOf(productData.get("categoriaId").toString());
            Category categoria = categoryRepository.findById(categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            product.setCategory(categoria);

            // Actualizar DetailProduct
            detailProduct.setCommercialName((String) productData.get("nombre"));
            detailProduct.setDescription((String) productData.get("descripcion"));

            // Precio es Float, no Double
            Float precio = Float.valueOf(productData.get("precio").toString());
            detailProduct.setPrice(precio);

            detailProduct.setImageUrl((String) productData.get("imagenUrl"));
            detailProduct.setBrand((String) productData.get("marca"));
            detailProduct.setModel((String) productData.get("modelo"));

            // Guardar producto
            Product savedProduct = productRepository.save(product);
            detailProductRepository.save(detailProduct);

            // Manejar Inventory
            Long stock = Long.valueOf(productData.get("stock").toString());
            if (isNew) {
                // Para productos nuevos, crear inventory
                Inventory inventory = new Inventory();
                inventory.setProduct(savedProduct);
                inventory.setCurrentStock(stock);
                inventory.setMinimumStock(5L);
                inventory.setMaximumStock(100L);
                inventoryRepository.save(inventory);
            } else {
                // Para productos existentes, actualizar inventory
                Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(savedProduct.getId());
                if (inventoryOpt.isPresent()) {
                    Inventory inventory = inventoryOpt.get();
                    inventory.setCurrentStock(stock);
                    inventoryRepository.save(inventory);
                } else {
                    // Si no existe inventory, crear uno
                    Inventory inventory = new Inventory();
                    inventory.setProduct(savedProduct);
                    inventory.setCurrentStock(stock);
                    inventory.setMinimumStock(5L);
                    inventory.setMaximumStock(100L);
                    inventoryRepository.save(inventory);
                }
            }

            response.put("success", true);
            response.put("message", isNew ? "Producto creado exitosamente" : "Producto actualizado exitosamente");
            response.put("productId", savedProduct.getId());

            log.info("✅ Producto guardado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al guardar producto: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al guardar el producto: " + e.getMessage());
        }

        return response;
    }

    /**
     * Eliminar un producto
     */
    @PostMapping("/productos/{id}/eliminar")
    @ResponseBody
    public Map<String, Object> eliminarProducto(@PathVariable Long id) {
        log.info("🗑️ Eliminando producto ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Product> productOpt = productRepository.findById(id);
            if (!productOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Producto no encontrado");
                return response;
            }

            Product product = productOpt.get();

            // Eliminar detailProduct si existe
            Optional<DetailProduct> detalleOpt = detailProductRepository.findByProductId(id);
            if (detalleOpt.isPresent()) {
                detailProductRepository.delete(detalleOpt.get());
            }

            // Eliminar inventory si existe
            Optional<Inventory> inventoryOpt = inventoryRepository.findByProductId(id);
            if (inventoryOpt.isPresent()) {
                inventoryRepository.delete(inventoryOpt.get());
            }

            productRepository.delete(product);

            response.put("success", true);
            response.put("message", "Producto eliminado exitosamente");

            log.info("✅ Producto eliminado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al eliminar producto: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al eliminar el producto: " + e.getMessage());
        }

        return response;
    }

    /**
     * Exportar productos a CSV
     */
    @GetMapping("/productos/exportar")
    @ResponseBody
    public String exportarProductos() {
        log.info("📥 Exportando productos a CSV");

        try {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Product> productos = productRepository.findAll(pageable);

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Código,Nombre,Categoría,Precio,Stock,Condición,Estado\n");

            for (Product product : productos.getContent()) {
                String categoria = "";
                Float precio = 0.0f;

                // Obtener detailProduct usando Optional
                Optional<DetailProduct> detalleOpt = detailProductRepository.findByProductId(product.getId());
                if (detalleOpt.isPresent()) {
                    precio = detalleOpt.get().getPrice();
                }

                // Obtener categoría
                if (product.getCategory() != null) {
                    categoria = product.getCategory().getName();
                }

                csv.append(product.getId()).append(",")
                        .append(product.getCode()).append(",")
                        .append(escapeCsv(product.getName())).append(",")
                        .append(escapeCsv(categoria)).append(",")
                        .append(precio).append(",")
                        .append(product.getStock()).append(",") // getStock() SÍ existe
                        .append(product.getProductCondition()).append(",") // getProductCondition()
                        .append(product.getProductStatus()).append("\n"); // getProductStatus()
            }

            log.info("✅ Productos exportados exitosamente");
            return csv.toString();

        } catch (Exception e) {
            log.error("❌ Error al exportar productos: {}", e.getMessage(), e);
            return "Error al exportar productos";
        }
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    /**
     * Calcular estadísticas de productos
     */
    private Map<String, Long> calculateProductStats() {
        Map<String, Long> stats = new HashMap<>();

        try {
            long total = productRepository.count();

            long enStock = 0;
            long stockBajo = 0;
            long agotados = 0;

            // Contar manualmente usando getStock() que SÍ existe
            List<Product> allProducts = productRepository.findAll();
            for (Product product : allProducts) {
                Long stock = product.getStock(); // Este método SÍ existe
                if (stock > 10) {
                    enStock++;
                } else if (stock > 0) {
                    stockBajo++;
                } else {
                    agotados++;
                }
            }

            stats.put("total", total);
            stats.put("enStock", enStock);
            stats.put("stockBajo", stockBajo);
            stats.put("agotados", agotados);
        } catch (Exception e) {
            log.error("Error al calcular estadísticas: {}", e.getMessage());
            stats.put("total", 0L);
            stats.put("enStock", 0L);
            stats.put("stockBajo", 0L);
            stats.put("agotados", 0L);
        }

        return stats;
    }

    /**
     * Escapar valores para CSV
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
    @GetMapping("/productos/listar")
    @ResponseBody
    public Map<String, Object> listarProductosParaModal() {
        log.info("📋 Listando productos para modal");

        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> productos = productRepository.findAll(Sort.by("id").descending());
            List<Map<String, Object>> productosData = new ArrayList<>();

            for (Product producto : productos) {
                Map<String, Object> productData = new HashMap<>();
                productData.put("id", producto.getId());
                productData.put("nombre", producto.getName());
                productData.put("stock", producto.getStock());
                productData.put("activo", producto.getProductStatus() == ProductStatus.ACTIVE);

                // Obtener detailProduct
                Optional<DetailProduct> detalleOpt = detailProductRepository.findByProductId(producto.getId());
                if (detalleOpt.isPresent()) {
                    DetailProduct detalle = detalleOpt.get();
                    productData.put("precio", detalle.getPrice());
                    productData.put("imagenUrl", detalle.getImageUrl());
                    productData.put("marca", detalle.getBrand());
                    productData.put("modelo", detalle.getModel());
                    productData.put("descripcion", detalle.getDescription());
                } else {
                    productData.put("precio", 0.0f);
                    productData.put("imagenUrl", "");
                    productData.put("marca", "");
                    productData.put("modelo", "");
                    productData.put("descripcion", "");
                }

                // Categoría
                if (producto.getCategory() != null) {
                    productData.put("categoriaId", producto.getCategory().getId());
                } else {
                    productData.put("categoriaId", null);
                }

                // Condición
                productData.put("condicion", producto.getProductCondition().name());

                productosData.add(productData);
            }

            response.put("success", true);
            response.put("productos", productosData);

        } catch (Exception e) {
            log.error("❌ Error al listar productos para modal: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al cargar productos");
        }

        return response;
    }
}