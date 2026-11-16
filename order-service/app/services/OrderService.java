package services;

import models.*;
import play.Logger;
import repositories.OrderRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer để xử lý business logic cho Order.
 * Tích hợp với ProductServiceClient để kiểm tra và cập nhật tồn kho.
 */
@Singleton
public class OrderService {
    
    private static final Logger.ALogger logger = Logger.of(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    
    /**
     * Constructor với dependency injection
     * @param orderRepository Repository để thao tác database
     * @param productServiceClient Client để gọi Product Service
     */
    @Inject
    public OrderService(OrderRepository orderRepository, ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
    }
    
    /**
     * Lấy tất cả đơn hàng
     * @return List các đơn hàng
     */
    public List<Order> getAllOrders() {
        logger.info("Getting all orders");
        return orderRepository.findAll();
    }
    
    /**
     * Lấy đơn hàng theo ID
     * @param orderId ID của đơn hàng
     * @return Optional chứa Order nếu tìm thấy
     */
    public Optional<Order> getOrderById(Long orderId) {
        logger.info("Getting order by ID: {}", orderId);
        return orderRepository.findById(orderId);
    }
    
    /**
     * Lấy danh sách đơn hàng theo trạng thái
     * @param status Trạng thái cần tìm
     * @return List các đơn hàng
     */
    public List<Order> getOrdersByStatus(String status) {
        logger.info("Getting orders by status: {}", status);
        return orderRepository.findByStatus(status);
    }
    
    /**
     * Tạo đơn hàng mới
     * @param request Request chứa thông tin đơn hàng
     * @return Order đã được tạo
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     * @throws RuntimeException nếu không đủ tồn kho hoặc sản phẩm không tồn tại
     */
    public Order createOrder(CreateOrderRequest request) {
        logger.info("Creating new order for customer: {}", request.getCustomerName());
        
        // Validate request
        if (!request.isValid()) {
            logger.error("Invalid create order request: {}", request);
            throw new IllegalArgumentException("Invalid order request. Customer name and items are required.");
        }
        
        // Validate items
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            if (!itemRequest.isValid()) {
                logger.error("Invalid order item: {}", itemRequest);
                throw new IllegalArgumentException("Invalid order item. Product ID and quantity must be positive.");
            }
        }
        
        // Create Order object
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setStatus("PENDING");
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Process each item
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Integer productId = itemRequest.getProductId();
            Integer quantity = itemRequest.getQuantity();
            
            // Get product information from Product Service
            Optional<Product> productOpt = productServiceClient.getProduct(productId);
            
            if (!productOpt.isPresent()) {
                logger.error("Product not found: {}", productId);
                throw new RuntimeException("Product not found: " + productId);
            }
            
            Product product = productOpt.get();
            
            // Check if product is active
            if (product.getIsActive() != null && !product.getIsActive()) {
                logger.error("Product is not active: {}", productId);
                throw new RuntimeException("Product is not active: " + product.getProductName());
            }
            
            // Check stock availability
            if (!product.hasEnoughStock(quantity)) {
                logger.error("Not enough stock for product {}. Available: {}, Requested: {}", 
                           productId, product.getStockQuantity(), quantity);
                throw new RuntimeException("Not enough stock for product: " + product.getProductName() + 
                                         ". Available: " + product.getStockQuantity() + 
                                         ", Requested: " + quantity);
            }
            
            // Create OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId);
            orderItem.setProductName(product.getProductName());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.calculateSubtotal();
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
            
            logger.debug("Added item to order: {} x {} = {}", 
                       product.getProductName(), quantity, orderItem.getSubtotal());
        }
        
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        // Save order to database
        Order createdOrder = orderRepository.create(order);
        
        // Decrease stock for each product
        for (OrderItem item : createdOrder.getItems()) {
            boolean decreased = productServiceClient.decreaseStock(item.getProductId(), item.getQuantity());
            if (!decreased) {
                logger.error("Failed to decrease stock for product: {}", item.getProductId());
                // Note: In production, should implement compensation/rollback logic
            }
        }
        
        logger.info("Order created successfully with ID: {}", createdOrder.getOrderId());
        return createdOrder;
    }
    
    /**
     * Cập nhật thông tin đơn hàng
     * @param orderId ID của đơn hàng cần cập nhật
     * @param request Request chứa thông tin cập nhật
     * @return Optional chứa Order đã cập nhật
     * @throws IllegalArgumentException nếu request không hợp lệ
     */
    public Optional<Order> updateOrder(Long orderId, UpdateOrderRequest request) {
        logger.info("Updating order: {}", orderId);
        
        // Validate request
        if (!request.hasUpdates()) {
            logger.error("No updates provided in request");
            throw new IllegalArgumentException("No updates provided");
        }
        
        if (request.getStatus() != null && !request.isStatusValid()) {
            logger.error("Invalid status: {}", request.getStatus());
            throw new IllegalArgumentException("Invalid status. Must be one of: PENDING, CONFIRMED, SHIPPED, COMPLETED, CANCELLED");
        }
        
        // Get existing order
        Optional<Order> existingOrderOpt = orderRepository.findById(orderId);
        
        if (!existingOrderOpt.isPresent()) {
            logger.warn("Order not found: {}", orderId);
            return Optional.empty();
        }
        
        Order existingOrder = existingOrderOpt.get();
        
        // Update fields
        if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            existingOrder.setCustomerName(request.getCustomerName());
        }
        
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty()) {
            existingOrder.setCustomerPhone(request.getCustomerPhone());
        }
        
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            String oldStatus = existingOrder.getStatus();
            String newStatus = request.getStatus();
            
            // If status changes to CANCELLED, restore stock
            if (!oldStatus.equals("CANCELLED") && newStatus.equals("CANCELLED")) {
                logger.info("Order {} is being cancelled. Restoring stock...", orderId);
                restoreStock(existingOrder);
            }
            
            existingOrder.setStatus(newStatus);
        }
        
        // Update in database
        boolean updated = orderRepository.update(orderId, existingOrder);
        
        if (updated) {
            logger.info("Order updated successfully: {}", orderId);
            return orderRepository.findById(orderId);
        } else {
            logger.error("Failed to update order: {}", orderId);
            return Optional.empty();
        }
    }
    
    /**
     * Cập nhật trạng thái đơn hàng
     * @param orderId ID của đơn hàng
     * @param status Trạng thái mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order status: {} -> {}", orderId, status);
        
        // Validate status
        if (!isValidStatus(status)) {
            logger.error("Invalid status: {}", status);
            throw new IllegalArgumentException("Invalid status");
        }
        
        // Get existing order to check current status
        Optional<Order> existingOrderOpt = orderRepository.findById(orderId);
        
        if (!existingOrderOpt.isPresent()) {
            logger.warn("Order not found: {}", orderId);
            return false;
        }
        
        Order existingOrder = existingOrderOpt.get();
        String oldStatus = existingOrder.getStatus();
        
        // If status changes to CANCELLED, restore stock
        if (!oldStatus.equals("CANCELLED") && status.equals("CANCELLED")) {
            logger.info("Order {} is being cancelled. Restoring stock...", orderId);
            restoreStock(existingOrder);
        }
        
        return orderRepository.updateStatus(orderId, status);
    }
    
    /**
     * Xóa đơn hàng
     * @param orderId ID của đơn hàng cần xóa
     * @return true nếu xóa thành công
     */
    public boolean deleteOrder(Long orderId) {
        logger.info("Deleting order: {}", orderId);
        
        // Get order to restore stock if needed
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            
            // Restore stock if order is not already cancelled
            if (!order.getStatus().equals("CANCELLED")) {
                logger.info("Restoring stock before deleting order: {}", orderId);
                restoreStock(order);
            }
        }
        
        boolean deleted = orderRepository.delete(orderId);
        
        if (deleted) {
            logger.info("Order deleted successfully: {}", orderId);
        } else {
            logger.error("Failed to delete order: {}", orderId);
        }
        
        return deleted;
    }
    
    /**
     * Khôi phục tồn kho khi hủy hoặc xóa đơn hàng
     * @param order Đơn hàng cần khôi phục tồn kho
     */
    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            boolean increased = productServiceClient.increaseStock(item.getProductId(), item.getQuantity());
            if (increased) {
                logger.info("Restored stock for product {}: {} units", 
                          item.getProductId(), item.getQuantity());
            } else {
                logger.error("Failed to restore stock for product: {}", item.getProductId());
            }
        }
    }
    
    /**
     * Kiểm tra trạng thái có hợp lệ không
     * @param status Trạng thái cần kiểm tra
     * @return true nếu hợp lệ
     */
    private boolean isValidStatus(String status) {
        return status != null && (
            status.equals("PENDING") ||
            status.equals("CONFIRMED") ||
            status.equals("SHIPPED") ||
            status.equals("COMPLETED") ||
            status.equals("CANCELLED")
        );
    }
    
    /**
     * Kiểm tra kết nối với Product Service
     * @return true nếu service hoạt động
     */
    public boolean checkProductServiceHealth() {
        return productServiceClient.isServiceAvailable();
    }
}
