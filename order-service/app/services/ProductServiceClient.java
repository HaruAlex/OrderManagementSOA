package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.typesafe.config.Config;
import models.Product;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Client để gọi API của Product Service.
 * Sử dụng để lấy thông tin sản phẩm và kiểm tra tồn kho.
 */
@Singleton
public class ProductServiceClient {
    
    private static final Logger.ALogger logger = Logger.of(ProductServiceClient.class);
    
    private final String productServiceUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Constructor với dependency injection
     * @param config Configuration từ application.conf
     */
    @Inject
    public ProductServiceClient(Config config) {
        this.productServiceUrl = config.getString("product.service.url");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        logger.info("ProductServiceClient initialized with URL: {}", productServiceUrl);
    }
    
    /**
     * Lấy thông tin sản phẩm theo ID
     * @param productId ID của sản phẩm
     * @return Optional chứa Product nếu tìm thấy
     */
    public Optional<Product> getProduct(Integer productId) {
        String url = productServiceUrl + "/api/products/" + productId;
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .build();
            
            logger.debug("Calling Product Service: GET {}", url);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                Product product = objectMapper.readValue(response.body(), Product.class);
                logger.info("Successfully retrieved product: {}", productId);
                return Optional.of(product);
            } else if (response.statusCode() == 404) {
                logger.warn("Product not found: {}", productId);
                return Optional.empty();
            } else {
                logger.error("Error response from Product Service. Status: {}, Body: {}", 
                           response.statusCode(), response.body());
                return Optional.empty();
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error calling Product Service for product: {}", productId, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return Optional.empty();
        }
    }
    
    /**
     * Kiểm tra tồn kho của sản phẩm
     * @param productId ID của sản phẩm
     * @param requestedQuantity Số lượng yêu cầu
     * @return true nếu đủ tồn kho, false nếu không
     */
    public boolean checkStock(Integer productId, Integer requestedQuantity) {
        Optional<Product> productOpt = getProduct(productId);
        
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            boolean hasStock = product.hasEnoughStock(requestedQuantity);
            
            if (hasStock) {
                logger.info("Product {} has enough stock. Available: {}, Requested: {}", 
                          productId, product.getStockQuantity(), requestedQuantity);
            } else {
                logger.warn("Product {} does not have enough stock. Available: {}, Requested: {}", 
                          productId, product.getStockQuantity(), requestedQuantity);
            }
            
            return hasStock;
        }
        
        logger.warn("Cannot check stock for product {} - product not found", productId);
        return false;
    }
    
    /**
     * Giảm số lượng tồn kho của sản phẩm
     * @param productId ID của sản phẩm
     * @param quantity Số lượng cần giảm
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean decreaseStock(Integer productId, Integer quantity) {
        String url = productServiceUrl + "/api/products/" + productId + "/decrease-stock";
        
        try {
            String requestBody = String.format("{\"quantity\": %d}", quantity);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            
            logger.debug("Calling Product Service: POST {} with quantity: {}", url, quantity);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.info("Successfully decreased stock for product {}: {} units", productId, quantity);
                return true;
            } else {
                logger.error("Failed to decrease stock for product {}. Status: {}, Body: {}", 
                           productId, response.statusCode(), response.body());
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error calling Product Service to decrease stock for product: {}", productId, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
    
    /**
     * Tăng số lượng tồn kho của sản phẩm (dùng khi hủy đơn hàng)
     * @param productId ID của sản phẩm
     * @param quantity Số lượng cần tăng
     * @return true nếu cập nhật thành công, false nếu thất bại
     */
    public boolean increaseStock(Integer productId, Integer quantity) {
        String url = productServiceUrl + "/api/products/" + productId + "/increase-stock";
        
        try {
            String requestBody = String.format("{\"quantity\": %d}", quantity);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build();
            
            logger.debug("Calling Product Service: POST {} with quantity: {}", url, quantity);
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                logger.info("Successfully increased stock for product {}: {} units", productId, quantity);
                return true;
            } else {
                logger.error("Failed to increase stock for product {}. Status: {}, Body: {}", 
                           productId, response.statusCode(), response.body());
                return false;
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("Error calling Product Service to increase stock for product: {}", productId, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
    
    /**
     * Kiểm tra xem Product Service có hoạt động không
     * @return true nếu service hoạt động, false nếu không
     */
    public boolean isServiceAvailable() {
        String url = productServiceUrl + "/api/products";
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            boolean available = response.statusCode() == 200;
            logger.info("Product Service availability check: {}", available ? "UP" : "DOWN");
            return available;
            
        } catch (IOException | InterruptedException e) {
            logger.error("Product Service is not available", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
    
    /**
     * Lấy URL của Product Service
     * @return URL string
     */
    public String getServiceUrl() {
        return productServiceUrl;
    }
}
