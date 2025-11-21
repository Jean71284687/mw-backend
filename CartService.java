package mweb.mw_backend.service;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.*;
import mweb.mw_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    
    private final PurchaseCartRepository cartRepository;
    private final PurchaseCartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public PurchaseCart getOrCreateActiveCart(Long userId) {
        Optional<PurchaseCart> existingCart = cartRepository.findByUserIdWithItems(userId);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        PurchaseCart newCart = PurchaseCart.builder()
                .user(user)
                .isActive(true)
                .build();
        
        return cartRepository.save(newCart);
    }

    @Transactional
    public PurchaseCartItem addItemToCart(Long userId, Long productId, Integer quantity) {
        PurchaseCart cart = getOrCreateActiveCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        // Verificar disponibilidad de stock
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        
        if (!inventory.isAvailable(quantity.longValue())) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + inventory.getCurrentStock());
        }

        // Buscar si el item ya existe en el carrito
        Optional<PurchaseCartItem> existingItem = cart.getPurchaseCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        PurchaseCartItem savedItem;
        
        if (existingItem.isPresent()) {
            // Actualizar cantidad del item existente
            PurchaseCartItem item = existingItem.get();
            Integer newQuantity = item.getQuantity() + quantity;
            
            if (!inventory.isAvailable(newQuantity.longValue())) {
                throw new RuntimeException("Stock insuficiente para la cantidad total solicitada. Disponible: " + inventory.getCurrentStock());
            }
            
            item.setQuantity(newQuantity);
            savedItem = cartItemRepository.save(item);
        } else {
            // Crear nuevo item
            PurchaseCartItem newItem = PurchaseCartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            savedItem = cartItemRepository.save(newItem);
            
            // Agregar el item a la colección del carrito
            cart.getPurchaseCartItems().add(savedItem);
        }
        
        // Forzar la sincronización con la base de datos
        cartItemRepository.flush();
        
        return savedItem;
    }

    @Transactional
    public void removeItemFromCart(Long userId, Long itemId) {
        PurchaseCart cart = getOrCreateActiveCart(userId);
        PurchaseCartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("El item no pertenece al carrito del usuario");
        }
        
        System.out.println("Eliminando item " + itemId + " del carrito " + cart.getId());
        
        // Primero remover de la colección del carrito
        cart.getPurchaseCartItems().remove(item);
        
        // Luego eliminar de la base de datos
        cartItemRepository.delete(item);
        
        System.out.println("Item eliminado exitosamente");
    }

    @Transactional
    public PurchaseCartItem updateItemQuantity(Long userId, Long itemId, Integer newQuantity) {
        PurchaseCart cart = getOrCreateActiveCart(userId);
        PurchaseCartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("El item no pertenece al carrito del usuario");
        }
        
        // Verificar stock disponible
        Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        
        if (!inventory.isAvailable(newQuantity.longValue())) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + inventory.getCurrentStock());
        }
        
        item.setQuantity(newQuantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        PurchaseCart cart = getOrCreateActiveCart(userId);
        
        System.out.println("Limpiando carrito ID: " + cart.getId());
        System.out.println("Items en el carrito: " + cart.getPurchaseCartItems().size());
        
        if (!cart.getPurchaseCartItems().isEmpty()) {
            // Hacer una copia de la lista para evitar ConcurrentModificationException
            List<PurchaseCartItem> itemsToDelete = new ArrayList<>(cart.getPurchaseCartItems());
            
            // Eliminar cada item individualmente
            for (PurchaseCartItem item : itemsToDelete) {
                System.out.println("Eliminando item ID: " + item.getId());
                cartItemRepository.delete(item);
            }
            
            // Limpiar la lista en el carrito
            cart.getPurchaseCartItems().clear();
            
            System.out.println("Todos los items eliminados");
        } else {
            System.out.println("El carrito ya está vacío");
        }
    }

    @Transactional
    public PurchaseCart getCartByUserId(Long userId) {
        // Usar el método con FETCH para evitar LazyInitializationException
        Optional<PurchaseCart> cart = cartRepository.findByUserIdWithItems(userId);
        
        if (cart.isPresent()) {
            return cart.get();
        }
        
        // Si no existe, crear uno nuevo
        return getOrCreateActiveCart(userId);
    }
}