package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Model class đại diện cho thông tin sản phẩm từ Product Service.
 * Sử dụng để lấy thông tin sản phẩm khi tạo đơn hàng.
 */
public class Product {
    
    @JsonProperty("productId")
    private Integer productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("stockQuantity")
    private Integer stockQuantity;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    /**
     * Constructor mặc định
     */
    public Product() {
    }
    
    /**
     * Constructor với tham số
     * @param productId ID sản phẩm
     * @param productName Tên sản phẩm
     * @param price Giá sản phẩm
     * @param stockQuantity Số lượng tồn kho
     */
    public Product(Integer productId, String productName, BigDecimal price, Integer stockQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
    
    // Getters and Setters
    
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    /**
     * Kiểm tra xem sản phẩm có đủ số lượng không
     * @param requestedQuantity Số lượng yêu cầu
     * @return true nếu đủ số lượng, false nếu không
     */
    public boolean hasEnoughStock(Integer requestedQuantity) {
        return stockQuantity != null && stockQuantity >= requestedQuantity;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", isActive=" + isActive +
                '}';
    }
}
