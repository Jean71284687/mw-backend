package mweb.mw_backend.controller.frontend.dashboard.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mweb.mw_backend.entity.Order;
import mweb.mw_backend.enumeration.OrderStatus;
import mweb.mw_backend.enumeration.PayStatus;
import mweb.mw_backend.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class DashboardOrderController {

    private final OrderRepository orderRepository;

    /**
     * Página principal de gestión de órdenes
     */
    @GetMapping("/ordenes")
    public String ordenes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PayStatus payStatus,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            Model model
    ) {
        log.info("📦 Accediendo a gestión de órdenes - Página: {}, Tamaño: {}", page, size);

        try {
            // Crear paginación ordenada por fecha descendente
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

            // Obtener órdenes con filtros
            Page<Order> ordersPage = getFilteredOrders(search, status, payStatus, dateFrom, dateTo, pageable);

            // Calcular estadísticas
            Map<String, Long> stats = calculateOrderStats();

            // Agregar datos al modelo
            model.addAttribute("orders", ordersPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ordersPage.getTotalPages());
            model.addAttribute("totalOrders", ordersPage.getTotalElements());
            model.addAttribute("stats", stats);

            // Mantener filtros en el modelo
            model.addAttribute("search", search);
            model.addAttribute("status", status);
            model.addAttribute("payStatus", payStatus);
            model.addAttribute("dateFrom", dateFrom);
            model.addAttribute("dateTo", dateTo);

            log.info("✅ Órdenes cargadas exitosamente: {} órdenes encontradas", ordersPage.getTotalElements());
            return "dashboard/ordenes";

        } catch (Exception e) {
            log.error("❌ Error al cargar órdenes: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar las órdenes");
            return "dashboard/ordenes";
        }
    }

    /**
     * Ver detalles de una orden específica
     */
    @GetMapping("/ordenes/{id}")
    public String verOrden(@PathVariable Long id, Model model) {
        log.info("👁️ Viendo detalles de orden ID: {}", id);

        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

            model.addAttribute("order", order);
            model.addAttribute("detailProducts", order.getDetailOrderList());

            log.info("✅ Detalles de orden cargados exitosamente");
            return "dashboard/ordenes-detalle";

        } catch (Exception e) {
            log.error("❌ Error al cargar detalles de orden: {}", e.getMessage(), e);
            model.addAttribute("error", "Orden no encontrada");
            return "redirect:/admin/dashboard/ordenes";
        }
    }

    /**
     * Actualizar estado de una orden
     */
    @PostMapping("/ordenes/{id}/estado")
    @ResponseBody
    public Map<String, Object> actualizarEstado(
            @PathVariable Long id,
            @RequestParam OrderStatus orderStatus,
            @RequestParam(required = false) PayStatus payStatus,
            @RequestParam(required = false) String notes
    ) {
        log.info("🔄 Actualizando estado de orden ID: {} a {}", id, orderStatus);

        Map<String, Object> response = new HashMap<>();

        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

            // Actualizar estados
            order.setOrderStatus(orderStatus);
            if (payStatus != null) {
                order.getPay().setPayStatus(payStatus);
            }

            // Guardar cambios
            orderRepository.save(order);

            response.put("success", true);
            response.put("message", "Estado actualizado exitosamente");
            response.put("newStatus", orderStatus.name());

            log.info("✅ Estado de orden actualizado exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al actualizar estado de orden: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error al actualizar el estado: " + e.getMessage());
        }

        return response;
    }

    /**
     * Cancelar una orden
     */
    @PostMapping("/ordenes/{id}/cancelar")
    @ResponseBody
    public Map<String, Object> cancelarOrden(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        log.info("❌ Cancelando orden ID: {}", id);

        Map<String, Object> response = new HashMap<>();

        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

            // Verificar que la orden pueda ser cancelada
            if (order.getOrderStatus() == OrderStatus.DELIVERED) {
                throw new RuntimeException("No se puede cancelar una orden ya entregada");
            }

            order.setOrderStatus(OrderStatus.CANCELED);
            orderRepository.save(order);

            response.put("success", true);
            response.put("message", "Orden cancelada exitosamente");

            log.info("✅ Orden cancelada exitosamente");

        } catch (Exception e) {
            log.error("❌ Error al cancelar orden: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return response;
    }

    /**
     * Exportar órdenes a CSV
     */
    @GetMapping("/ordenes/exportar")
    @ResponseBody
    public String exportarOrdenes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PayStatus payStatus
    ) {
        log.info("📥 Exportando órdenes a CSV");

        try {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Order> orders = getFilteredOrders(search, status, payStatus, null, null, pageable);

            StringBuilder csv = new StringBuilder();
            csv.append("ID,Cliente,Email,Fecha,Total,Estado Orden,Estado Pago\n");

            orders.getContent().forEach(order -> {
                csv.append(order.getId()).append(",")
                   .append(order.getUser().getUsername()).append(",")
                   .append(order.getUser().getEmail()).append(",")
                   .append(order.getCreatedAt()).append(",")
                   .append(order.getTotal()).append(",")
                   .append(order.getOrderStatus()).append(",")
                   .append(order.getPay().getPayStatus()).append("\n");
            });

            log.info("✅ Órdenes exportadas exitosamente");
            return csv.toString();

        } catch (Exception e) {
            log.error("❌ Error al exportar órdenes: {}", e.getMessage(), e);
            return "Error al exportar órdenes";
        }
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    /**
     * Obtener órdenes con filtros aplicados
     */
    private Page<Order> getFilteredOrders(
            String search,
            OrderStatus status,
            PayStatus payStatus,
            String dateFrom,
            String dateTo,
            Pageable pageable
    ) {
        // Si no hay filtros, devolver todas las órdenes con relaciones cargadas
        if (search == null && status == null && payStatus == null && dateFrom == null && dateTo == null) {
            return orderRepository.findAllWithRelations(pageable);
        }

        // Aplicar filtros (puedes implementar un método personalizado en el repository)
        if (status != null) {
            return orderRepository.findByOrderStatus(status, pageable);
        }

        // Por defecto, devolver todas con relaciones
        return orderRepository.findAllWithRelations(pageable);
    }

    /**
     * Calcular estadísticas de órdenes
     */
    private Map<String, Long> calculateOrderStats() {
        Map<String, Long> stats = new HashMap<>();

        try {
            stats.put("total", orderRepository.count());
            stats.put("pending", orderRepository.countByOrderStatus(OrderStatus.PENDING));
            stats.put("completed", orderRepository.countByOrderStatus(OrderStatus.DELIVERED));
            stats.put("cancelled", orderRepository.countByOrderStatus(OrderStatus.CANCELED));
        } catch (Exception e) {
            log.error("Error al calcular estadísticas: {}", e.getMessage());
            // Valores por defecto en caso de error
            stats.put("total", 0L);
            stats.put("pending", 0L);
            stats.put("completed", 0L);
            stats.put("cancelled", 0L);
        }

        return stats;
    }
}

