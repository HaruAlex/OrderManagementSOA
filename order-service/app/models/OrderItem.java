package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Model class đại diện cho một sản phẩm trong đơn hàng.
 * Chứa thông tin về sản phẩm, số lượng và giá tiền.
 */
public class OrderItem {
    
    @JsonProperty("orderItemId")
    private Long orderItemId;
    
    @JsonProperty("orderId")
    private Long orderId;
    
    @JsonProperty("productId")
    private Integer productId;
    
    @JsonProperty("productName")
    private String productName;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("unitPrice")
    private BigDecimal unitPrice;
    
    @JsonProperty("subtotal")
    private BigDecimal subtotal;
    
    /**
     * Constructor mặc định
     */
    public OrderItem() {
    }
    
    /**
     * Constructor với tham số
     * @param productId ID sản phẩm
     * @param productName Tên sản phẩm
     * @param quantity Số lượng
     * @param unitPrice Đơn giá
     */
    public OrderItem(Integer productId, String productName, Integer quantity, BigDecimal unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice.multiply(new BigDecimal(quantity));
    }
    
    /**
     * Constructor đầy đủ
     * @param orderItemId ID của item trong đơn hàng
     * @param orderId ID đơn hàng
     * @param productId ID sản phẩm
     * @param productName Tên sản phẩm
     * @param quantity Số lượng
     * @param unitPrice Đơn giá
     * @param subtotal Thành tiền
     */
    public OrderItem(Long orderItemId, Long orderId, Integer productId, String productName, 
                     Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }
    
    // Getters and Setters
    
    public Long getOrderItemId() {
        return orderItemId;
    }
    
    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
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
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    /**
     * Tính toán lại thành tiền dựa trên số lượng và đơn giá
     */
    public void calculateSubtotal() {
        if (this.unitPrice != null && this.quantity != null) {
            this.subtotal = this.unitPrice.multiply(new BigDecimal(this.quantity));
        }
    }
    
    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
