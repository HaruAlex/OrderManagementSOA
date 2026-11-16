package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import models.Product;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.ProductService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Controller để xử lý các REST API endpoints cho Product Management
 */
@Singleton
public class ProductController extends Controller {
    
    private static final Logger.ALogger logger = Logger.of(ProductController.class);
    private final ProductService productService;
    private final ObjectMapper objectMapper;
    
    @Inject
    public ProductController(ProductService productService) {
        this.productService = productService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    public Result managePage(Http.Request request) {
        return ok(views.html.products.render());
    }
    
    public Result getAllProducts(Http.Request request) {
        try {
            List<Product> products = productService.getAllProducts();
            JsonNode json = objectMapper.valueToTree(products);
            return ok(json).as("application/json");
        } catch (Exception e) {
            logger.error("Error getting all products", e);
            return internalServerError(createErrorResponse("Failed to retrieve products"));
        }
    }
    
    public Result getProductById(Http.Request request, Integer id) {
        try {
            Optional<Product> productOpt = productService.getProductById(id);
            
            if (productOpt.isPresent()) {
                JsonNode json = objectMapper.valueToTree(productOpt.get());
                return ok(json).as("application/json");
            } else {
                return notFound(createErrorResponse("Product not found with ID: " + id));
            }
        } catch (Exception e) {
            logger.error("Error getting product by ID: {}", id, e);
            return internalServerError(createErrorResponse("Failed to retrieve product"));
        }
    }
    
    public Result createProduct(Http.Request request) {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest(createErrorResponse("Missing request body"));
            }
            
            Product product = objectMapper.treeToValue(json, Product.class);
            Product createdProduct = productService.createProduct(product);
            JsonNode responseJson = objectMapper.valueToTree(createdProduct);
            
            return created(responseJson).as("application/json");
        } catch (IllegalArgumentException e) {
            return badRequest(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product", e);
            return internalServerError(createErrorResponse("Failed to create product"));
        }
    }
    
    public Result updateProduct(Http.Request request, Integer id) {
        try {
            JsonNode json = request.body().asJson();
            if (json == null) {
                return badRequest(createErrorResponse("Missing request body"));
            }
            
            Product product = objectMapper.treeToValue(json, Product.class);
            Optional<Product> updatedProductOpt = productService.updateProduct(id, product);
            
            if (updatedProductOpt.isPresent()) {
                JsonNode responseJson = objectMapper.valueToTree(updatedProductOpt.get());
                return ok(responseJson).as("application/json");
            } else {
                return notFound(createErrorResponse("Product not found with ID: " + id));
            }
        } catch (Exception e) {
            logger.error("Error updating product: {}", id, e);
            return internalServerError(createErrorResponse("Failed to update product"));
        }
    }
    
    public Result deleteProduct(Http.Request request, Integer id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            
            if (deleted) {
                ObjectNode response = Json.newObject();
                response.put("success", true);
                response.put("message", "Product deleted successfully");
                return ok(response).as("application/json");
            } else {
                return notFound(createErrorResponse("Product not found with ID: " + id));
            }
        } catch (Exception e) {
            logger.error("Error deleting product: {}", id, e);
            return internalServerError(createErrorResponse("Failed to delete product"));
        }
    }
    
    public Result decreaseStock(Http.Request request, Integer id) {
        try {
            JsonNode json = request.body().asJson();
            if (json == null || !json.has("quantity")) {
                return badRequest(createErrorResponse("Missing quantity in request body"));
            }
            
            Integer quantity = json.get("quantity").asInt();
            boolean decreased = productService.decreaseStock(id, quantity);
            
            if (decreased) {
                ObjectNode response = Json.newObject();
                response.put("success", true);
                response.put("message", "Stock decreased successfully");
                return ok(response).as("application/json");
            } else {
                return badRequest(createErrorResponse("Not enough stock or product not found"));
            }
        } catch (IllegalArgumentException e) {
            return badRequest(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error decreasing stock for product: {}", id, e);
            return internalServerError(createErrorResponse("Failed to decrease stock"));
        }
    }
    
    public Result increaseStock(Http.Request request, Integer id) {
        try {
            JsonNode json = request.body().asJson();
            if (json == null || !json.has("quantity")) {
                return badRequest(createErrorResponse("Missing quantity in request body"));
            }
            
            Integer quantity = json.get("quantity").asInt();
            boolean increased = productService.increaseStock(id, quantity);
            
            if (increased) {
                ObjectNode response = Json.newObject();
                response.put("success", true);
                response.put("message", "Stock increased successfully");
                return ok(response).as("application/json");
            } else {
                return notFound(createErrorResponse("Product not found with ID: " + id));
            }
        } catch (IllegalArgumentException e) {
            return badRequest(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error increasing stock for product: {}", id, e);
            return internalServerError(createErrorResponse("Failed to increase stock"));
        }
    }
    
    public Result health(Http.Request request) {
        ObjectNode response = Json.newObject();
        response.put("status", "UP");
        response.put("service", "Product Service");
        return ok(response).as("application/json");
    }
    
    private JsonNode createErrorResponse(String message) {
        ObjectNode error = Json.newObject();
        error.put("success", false);
        error.put("error", message);
        return error;
    }
}
