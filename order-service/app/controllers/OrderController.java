package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.CreateOrderRequest;
import models.Order;
import models.UpdateOrderRequest;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.OrderService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Controller để xử lý các REST API endpoints cho Order Management.
 * Cung cấp các API CRUD cho đơn hàng.
 */
@Singleton
public class OrderController extends Controller {
    
    private static final Logger.ALogger logger = Logger.of(OrderController.class);
    
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor với dependency injection
     * @param orderService Service để xử lý business logic
     */
    @Inject
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * API để hiển thị trang quản lý đơn hàng
     * GET /orders/manage
     * @param request HTTP request
     * @return HTML page
     */
    public Result managePage(Http.Request request) {
        logger.info("Accessing order management page");
        return ok(views.html.orders.render());
    }
    
    /**
     * API lấy danh sách tất cả đơn hàng
     * GET /api/orders
     * @param request HTTP request
     * @return JSON array các đơn hàng
     */
    public Result getAllOrders(Http.Request request) {
        try {
            logger.info("API: Get all orders");
            List<Order> orders = orderService.getAllOrders();
            
            JsonNode json = objectMapper.valueToTree(orders);
            return ok(json).as("application/json");
            
        } catch (Exception e) {
            logger.error("Error getting all orders", e);
            return internalServerError(createErrorResponse("Failed to retrieve orders: " + e.getMessage()));
        }
    }
    
    /**
     * API lấy đơn hàng theo ID
     * GET /api/orders/:id
     * @param request HTTP request
     * @param id ID của đơn hàng
     * @return JSON object đơn hàng
     */
    public Result getOrderById(Http.Request request, Long id) {
        try {
            logger.info("API: Get order by ID: {}", id);
            Optional<Order> orderOpt = orderService.getOrderById(id);
            
            if (orderOpt.isPresent()) {
                JsonNode json = objectMapper.valueToTree(orderOpt.get());
                return ok(json).as("application/json");
            } else {
                return notFound(createErrorResponse("Order not found with ID: " + id));
            }
            
        } catch (Exception e) {
            logger.error("Error getting order by ID: {}", id, e);
            return internalServerError(createErrorResponse("Failed to retrieve order: " + e.getMessage()));
        }
    }
    
    /**
     * API lấy danh sách đơn hàng theo trạng thái
     * GET /api/orders/status/:status
     * @param request HTTP request
     * @param status Trạng thái đơn hàng
     * @return JSON array các đơn hàng
     */
    public Result getOrdersByStatus(Http.Request request, String status) {
        try {
            logger.info("API: Get orders by status: {}", status);
            List<Order> orders = orderService.getOrdersByStatus(status);
            
            JsonNode json = objectMapper.valueToTree(orders);
            return ok(json).as("application/json");
            
        } catch (Exception e) {
            logger.error("Error getting orders by status: {}", status, e);
            return internalServerError(createErrorResponse("Failed to retrieve orders: " + e.getMessage()));
        }
    }
    
    /**
     * API tạo đơn hàng mới
     * POST /api/orders
     * @param request HTTP request với body chứa CreateOrderRequest
     * @return JSON object đơn hàng đã tạo
     */
    public Result createOrder(Http.Request request) {
        try {
            logger.info("API: Create new order");
            
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest(createErrorResponse("Missing request body"));
            }
            
            CreateOrderRequest createRequest = objectMapper.treeToValue(json, CreateOrderRequest.class);
            
            if (!createRequest.isValid()) {
                return badRequest(createErrorResponse("Invalid request. Customer name and items are required."));
            }
            
            Order createdOrder = orderService.createOrder(createRequest);
            JsonNode responseJson = objectMapper.valueToTree(createdOrder);
            
            return created(responseJson).as("application/json");
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for creating order", e);
            return badRequest(createErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Business logic error creating order", e);
            return badRequest(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating order", e);
            return internalServerError(createErrorResponse("Failed to create order: " + e.getMessage()));
        }
    }
    
    /**
     * API cập nhật đơn hàng
     * PUT /api/orders/:id
     * @param request HTTP request với body chứa UpdateOrderRequest
     * @param id ID của đơn hàng
     * @return JSON object đơn hàng đã cập nhật
     */
    public Result updateOrder(Http.Request request, Long id) {
        try {
            logger.info("API: Update order: {}", id);
            
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest(createErrorResponse("Missing request body"));
            }
            
            UpdateOrderRequest updateRequest = objectMapper.treeToValue(json, UpdateOrderRequest.class);
            
            if (!updateRequest.hasUpdates()) {
                return badRequest(createErrorResponse("No updates provided"));
            }
            
            if (updateRequest.getStatus() != null && !updateRequest.isStatusValid()) {
                return badRequest(createErrorResponse("Invalid status"));
            }
            
            Optional<Order> updatedOrderOpt = orderService.updateOrder(id, updateRequest);
            
            if (updatedOrderOpt.isPresent()) {
                JsonNode responseJson = objectMapper.valueToTree(updatedOrderOpt.get());
                return ok(responseJson).as("application/json");
            } else {
                return notFound(createErrorResponse("Order not found with ID: " + id));
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for updating order", e);
            return badRequest(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating order: {}", id, e);
            return internalServerError(createErrorResponse("Failed to update order: " + e.getMessage()));
        }
    }
    
    /**
     * API cập nhật trạng thái đơn hàng
     * PATCH /api/orders/:id/status
     * @param request HTTP request với body chứa status
     * @param id ID của đơn hàng
     * @return JSON response
     */
    public Result updateOrderStatus(Http.Request request, Long id) {
        try {
            logger.info("API: Update order status: {}", id);
            
            JsonNode json = request.body().asJson();
            if (json == null || !json.has("status")) {
                return badRequest(createErrorResponse("Missing status in request body"));
            }
            
            String status = json.get("status").asText();
            
            boolean updated = orderService.updateOrderStatus(id, status);
            
            if (updated) {
                ObjectNode response = Json.newObject();
                response.put("success", true);
                response.put("message", "Order status updated successfully");
                return ok(response).as("application/json");
            } else {
                return notFound(createErrorResponse("Order not found with ID: " + id));
            }
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status for updating order", e);
            return badRequest(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating order status: {}", id, e);
            return internalServerError(createErrorResponse("Failed to update order status: " + e.getMessage()));
        }
    }
    
    /**
     * API xóa đơn hàng
     * DELETE /api/orders/:id
     * @param request HTTP request
     * @param id ID của đơn hàng
     * @return JSON response
     */
    public Result deleteOrder(Http.Request request, Long id) {
        try {
            logger.info("API: Delete order: {}", id);
            
            boolean deleted = orderService.deleteOrder(id);
            
            if (deleted) {
                ObjectNode response = Json.newObject();
                response.put("success", true);
                response.put("message", "Order deleted successfully");
                return ok(response).as("application/json");
            } else {
                return notFound(createErrorResponse("Order not found with ID: " + id));
            }
            
        } catch (Exception e) {
            logger.error("Error deleting order: {}", id, e);
            return internalServerError(createErrorResponse("Failed to delete order: " + e.getMessage()));
        }
    }
    
    /**
     * API kiểm tra health của service
     * GET /api/orders/health
     * @param request HTTP request
     * @return JSON response
     */
    public Result health(Http.Request request) {
        try {
            boolean productServiceHealthy = orderService.checkProductServiceHealth();
            
            ObjectNode response = Json.newObject();
            response.put("status", "UP");
            response.put("service", "Order Service");
            response.put("productServiceConnected", productServiceHealthy);
            
            return ok(response).as("application/json");
            
        } catch (Exception e) {
            logger.error("Error checking health", e);
            ObjectNode response = Json.newObject();
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            return internalServerError(response).as("application/json");
        }
    }
    
    /**
     * Tạo JSON response cho error
     * @param message Error message
     * @return JsonNode chứa error
     */
    private JsonNode createErrorResponse(String message) {
        ObjectNode error = Json.newObject();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}
