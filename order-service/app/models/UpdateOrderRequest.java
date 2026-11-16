package models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request model để cập nhật thông tin đơn hàng.
 * Hỗ trợ cập nhật trạng thái và thông tin khách hàng.
 */
public class UpdateOrderRequest {
    
    @JsonProperty("customerName")
    private String customerName;
    
    @JsonProperty("customerPhone")
    private String customerPhone;
    
    @JsonProperty("status")
    private String status;
    
    /**
     * Constructor mặc định
     */
    public UpdateOrderRequest() {
    }
    
    /**
     * Constructor với tham số
     * @param customerName Tên khách hàng (có thể null nếu không cập nhật)
     * @param customerPhone Số điện thoại (có thể null nếu không cập nhật)
     * @param status Trạng thái đơn hàng (có thể null nếu không cập nhật)
     */
    public UpdateOrderRequest(String customerName, String customerPhone, String status) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = status;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Kiểm tra xem request có chứa thông tin cập nhật không
     * @return true nếu có ít nhất một trường được cập nhật
     */
    public boolean hasUpdates() {
        return (customerName != null && !customerName.trim().isEmpty()) ||
               (customerPhone != null && !customerPhone.trim().isEmpty()) ||
               (status != null && !status.trim().isEmpty());
    }
    
    /**
     * Kiểm tra tính hợp lệ của trạng thái
     * @return true nếu trạng thái hợp lệ hoặc null
     */
    public boolean isStatusValid() {
        if (status == null) {
            return true;
        }
        return status.equals("PENDING") || 
               status.equals("CONFIRMED") || 
               status.equals("SHIPPED") || 
               status.equals("COMPLETED") || 
               status.equals("CANCELLED");
    }
    
    @Override
    public String toString() {
        return "UpdateOrderRequest{" +
                "customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
