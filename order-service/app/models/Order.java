package models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class đại diện cho một đơn hàng trong hệ thống.
 * Chứa thông tin cơ bản của đơn hàng và danh sách sản phẩm trong đơn.
 */
public class Order {
    
    @JsonProperty("orderId")
    private Long orderId;
    
    @JsonProperty("customerName")
    private String customerName;
    
    @JsonProperty("customerPhone")
    private String customerPhone;
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("createdAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonProperty("items")
    private List<OrderItem> items;
    
    /**
     * Constructor mặc định
     */
    public Order() {
        this.items = new ArrayList<>();
        this.totalAmount = BigDecimal.ZERO;
        this.status = "PENDING";
    }
    
    /**
     * Constructor với tham số
     * @param orderId ID của đơn hàng
     * @param customerName Tên khách hàng
     * @param customerPhone Số điện thoại khách hàng
     * @param totalAmount Tổng tiền đơn hàng
     * @param status Trạng thái đơn hàng
     */
    public Order(Long orderId, String customerName, String customerPhone, 
                 BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = new ArrayList<>();
    }
    
    // Getters and Setters
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerPhone() {
        return customerPhone;
    }
    
    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    /**
     * Thêm một sản phẩm vào đơn hàng
     * @param item Sản phẩm cần thêm
     */
    public void addItem(OrderItem item) {
        this.items.add(item);
    }
    
    /**
     * Tính toán lại tổng tiền của đơn hàng dựa trên các sản phẩm
     */
    public void calculateTotalAmount() {
        this.totalAmount = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", items=" + items +
                '}';
    }
}
