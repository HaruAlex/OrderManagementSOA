package services;

import models.Product;
import play.Logger;
import repositories.ProductRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Service layer để xử lý business logic cho Product
 */
@Singleton
public class ProductService {
    
    private static final Logger.ALogger logger = Logger.of(ProductService.class);
    private final ProductRepository productRepository;
    
    @Inject
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Integer productId) {
        return productRepository.findById(productId);
    }
    
    public Product createProduct(Product product) {
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid price");
        }
        
        return productRepository.create(product);
    }
    
    public Optional<Product> updateProduct(Integer productId, Product product) {
        Optional<Product> existingProduct = productRepository.findById(productId);
        
        if (!existingProduct.isPresent()) {
            return Optional.empty();
        }
        
        boolean updated = productRepository.update(productId, product);
        
        if (updated) {
            return productRepository.findById(productId);
        }
        
        return Optional.empty();
    }
    
    public boolean deleteProduct(Integer productId) {
        return productRepository.delete(productId);
    }
    
    public boolean decreaseStock(Integer productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        return productRepository.decreaseStock(productId, quantity);
    }
    
    public boolean increaseStock(Integer productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        return productRepository.increaseStock(productId, quantity);
    }
}
