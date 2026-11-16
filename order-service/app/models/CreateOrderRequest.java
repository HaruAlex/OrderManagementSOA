package models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request model để tạo đơn hàng mới.
 * Chứa thông tin khách hàng và danh sách sản phẩm cần đặt.
 */
public class CreateOrderRequest {
    
    @JsonProperty("customerName")
    private String customerName;
    
    @JsonProperty("customerPhone")
    private String customerPhone;
    
    @JsonProperty("items")
    private List<OrderItemRequest> items;
    
    /**
     * Constructor mặc định
     */
    public CreateOrderRequest() {
    }
    
    /**
     * Constructor với tham số
     * @param customerName Tên khách hàng
     * @param customerPhone Số điện thoại khách hàng
     * @param items Danh sách sản phẩm đặt hàng
     */
    public CreateOrderRequest(String customerName, String customerPhone, List<OrderItemRequest> items) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.items = items;
    }
    
    // Getters and Setters
    
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
    
    public List<OrderItemRequest> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
    
    /**
     * Kiểm tra tính hợp lệ của request
     * @return true nếu request hợp lệ, false nếu không
     */
    public boolean isValid() {
        return customerName != null && !customerName.trim().isEmpty() &&
               items != null && !items.isEmpty();
    }
    
    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", items=" + items +
                '}';
    }
    
    /**
     * Inner class đại diện cho thông tin sản phẩm trong request tạo đơn hàng
     */
    public static class OrderItemRequest {
        
        @JsonProperty("productId")
        private Integer productId;
        
        @JsonProperty("quantity")
        private Integer quantity;
        
        /**
         * Constructor mặc định
         */
        public OrderItemRequest() {
        }
        
        /**
         * Constructor với tham số
         * @param productId ID sản phẩm
         * @param quantity Số lượng đặt
         */
        public OrderItemRequest(Integer productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        // Getters and Setters
        
        public Integer getProductId() {
            return productId;
        }
        
        public void setProductId(Integer productId) {
            this.productId = productId;
        }
        
        public Integer getQuantity() {
            return quantity;
        }
        
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
        
        /**
         * Kiểm tra tính hợp lệ của item
         * @return true nếu item hợp lệ, false nếu không
         */
        public boolean isValid() {
            return productId != null && productId > 0 &&
                   quantity != null && quantity > 0;
        }
        
        @Override
        public String toString() {
            return "OrderItemRequest{" +
                    "productId=" + productId +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
