package mweb.mw_backend.service;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.Inventory;
import mweb.mw_backend.entity.Product;
import mweb.mw_backend.repository.InventoryRepository;
import mweb.mw_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Inventory createInventory(Long productId, Long currentStock, Long minimumStock, Long maximumStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (inventoryRepository.findByProductId(productId).isPresent()) {
            throw new RuntimeException("El producto ya tiene inventario registrado");
        }

        Inventory inventory = Inventory.builder()
                .product(product)
                .currentStock(currentStock)
                .minimumStock(minimumStock)
                .maximumStock(maximumStock)
                .build();

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory updateStock(Long productId, Long newStock) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (newStock > inventory.getMaximumStock()) {
            throw new RuntimeException("El stock no puede exceder el máximo permitido: " + inventory.getMaximumStock());
        }

        inventory.setCurrentStock(newStock);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory addStock(Long productId, Long quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        Long newStock = inventory.getCurrentStock() + quantity;
        
        if (newStock > inventory.getMaximumStock()) {
            throw new RuntimeException("El stock resultante excedería el máximo permitido: " + inventory.getMaximumStock());
        }

        inventory.setCurrentStock(newStock);
        return inventoryRepository.save(inventory);
    }

    @Transactional
    public Inventory reduceStock(Long productId, Long quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        if (inventory.getCurrentStock() < quantity) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + inventory.getCurrentStock());
        }

        inventory.setCurrentStock(inventory.getCurrentStock() - quantity);
        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public List<Inventory> getLowStockProducts() {
        return inventoryRepository.findLowStockProducts();
    }

    @Transactional(readOnly = true)
    public List<Inventory> getOutOfStockProducts() {
        return inventoryRepository.findOutOfStockProducts();
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
    }
}