/**
 * Order Management System - Frontend JavaScript
 * Xử lý tương tác với API và cập nhật giao diện
 */

// API Base URL
const API_BASE_URL = '/api/orders';

// Global state
let allOrders = [];
let currentFilter = '';

/**
 * Khởi tạo ứng dụng khi DOM đã load xong
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('Order Management System initialized');
    
    // Load orders on page load
    loadOrders();
    
    // Setup event listeners
    setupEventListeners();
});

/**
 * Thiết lập các event listeners
 */
function setupEventListeners() {
    // Create order button
    document.getElementById('createOrderBtn').addEventListener('click', openCreateModal);
    
    // Refresh button
    document.getElementById('refreshBtn').addEventListener('click', loadOrders);
    
    // Status filter
    document.getElementById('statusFilter').addEventListener('change', function(e) {
        currentFilter = e.target.value;
        filterOrders();
    });
    
    // Create order form
    document.getElementById('createOrderForm').addEventListener('submit', handleCreateOrder);
    
    // Edit order form
    document.getElementById('editOrderForm').addEventListener('submit', handleEditOrder);
    
    // Add item button
    document.getElementById('addItemBtn').addEventListener('click', addOrderItemRow);
}

/**
 * Load danh sách đơn hàng từ API
 */
async function loadOrders() {
    try {
        showLoading();
        
        const response = await fetch(API_BASE_URL);
        
        if (!response.ok) {
            throw new Error('Failed to load orders');
        }
        
        allOrders = await response.json();
        console.log('Loaded orders:', allOrders.length);
        
        filterOrders();
        updateStatistics();
        
    } catch (error) {
        console.error('Error loading orders:', error);
        showToast('Không thể tải danh sách đơn hàng', 'error');
        showError('Không thể tải dữ liệu. Vui lòng thử lại.');
    }
}

/**
 * Lọc và hiển thị đơn hàng theo trạng thái
 */
function filterOrders() {
    let filteredOrders = allOrders;
    
    if (currentFilter) {
        filteredOrders = allOrders.filter(order => order.status === currentFilter);
    }
    
    displayOrders(filteredOrders);
}

/**
 * Hiển thị danh sách đơn hàng trong bảng
 */
function displayOrders(orders) {
    const tbody = document.getElementById('ordersTableBody');
    
    if (orders.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center">
                    <i class="fas fa-inbox"></i> Không có đơn hàng nào
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = orders.map(order => `
        <tr>
            <td><strong>#${order.orderId}</strong></td>
            <td>${escapeHtml(order.customerName)}</td>
            <td>${order.customerPhone || '-'}</td>
            <td class="price">${formatCurrency(order.totalAmount)}</td>
            <td>${getStatusBadge(order.status)}</td>
            <td>${formatDateTime(order.createdAt)}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn view" onclick="viewOrder(${order.orderId})" title="Xem chi tiết">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="action-btn edit" onclick="editOrder(${order.orderId})" title="Chỉnh sửa">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn delete" onclick="confirmDeleteOrder(${order.orderId})" title="Xóa">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

/**
 * Cập nhật thống kê theo trạng thái
 */
function updateStatistics() {
    const stats = {
        PENDING: 0,
        CONFIRMED: 0,
        SHIPPED: 0,
        COMPLETED: 0
    };
    
    allOrders.forEach(order => {
        if (stats.hasOwnProperty(order.status)) {
            stats[order.status]++;
        }
    });
    
    document.getElementById('pendingCount').textContent = stats.PENDING;
    document.getElementById('confirmedCount').textContent = stats.CONFIRMED;
    document.getElementById('shippedCount').textContent = stats.SHIPPED;
    document.getElementById('completedCount').textContent = stats.COMPLETED;
}

/**
 * Mở modal tạo đơn hàng mới
 */
function openCreateModal() {
    document.getElementById('createOrderForm').reset();
    
    // Reset order items to one row
    const container = document.getElementById('orderItemsContainer');
    container.innerHTML = `
        <div class="order-item-row">
            <input type="number" class="form-control product-id" placeholder="Mã sản phẩm" required min="1">
            <input type="number" class="form-control quantity" placeholder="Số lượng" required min="1">
            <button type="button" class="btn btn-danger btn-sm" onclick="removeOrderItem(this)">
                <i class="fas fa-trash"></i>
            </button>
        </div>
    `;
    
    document.getElementById('createOrderModal').classList.add('active');
}

/**
 * Đóng modal tạo đơn hàng
 */
function closeCreateModal() {
    document.getElementById('createOrderModal').classList.remove('active');
}

/**
 * Thêm dòng sản phẩm mới trong form tạo đơn
 */
function addOrderItemRow() {
    const container = document.getElementById('orderItemsContainer');
    const newRow = document.createElement('div');
    newRow.className = 'order-item-row';
    newRow.innerHTML = `
        <input type="number" class="form-control product-id" placeholder="Mã sản phẩm" required min="1">
        <input type="number" class="form-control quantity" placeholder="Số lượng" required min="1">
        <button type="button" class="btn btn-danger btn-sm" onclick="removeOrderItem(this)">
            <i class="fas fa-trash"></i>
        </button>
    `;
    container.appendChild(newRow);
}

/**
 * Xóa dòng sản phẩm
 */
function removeOrderItem(button) {
    const container = document.getElementById('orderItemsContainer');
    if (container.children.length > 1) {
        button.parentElement.remove();
    } else {
        showToast('Phải có ít nhất một sản phẩm', 'error');
    }
}

/**
 * Xử lý submit form tạo đơn hàng
 */
async function handleCreateOrder(e) {
    e.preventDefault();
    
    const customerName = document.getElementById('customerName').value;
    const customerPhone = document.getElementById('customerPhone').value;
    
    // Collect order items
    const itemRows = document.querySelectorAll('#orderItemsContainer .order-item-row');
    const items = [];
    
    for (const row of itemRows) {
        const productId = parseInt(row.querySelector('.product-id').value);
        const quantity = parseInt(row.querySelector('.quantity').value);
        
        if (productId && quantity) {
            items.push({ productId, quantity });
        }
    }
    
    if (items.length === 0) {
        showToast('Vui lòng thêm ít nhất một sản phẩm', 'error');
        return;
    }
    
    const orderData = {
        customerName,
        customerPhone,
        items
    };
    
    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showToast('Tạo đơn hàng thành công!', 'success');
            closeCreateModal();
            loadOrders();
        } else {
            showToast(result.error || 'Không thể tạo đơn hàng', 'error');
        }
        
    } catch (error) {
        console.error('Error creating order:', error);
        showToast('Lỗi kết nối đến server', 'error');
    }
}

/**
 * Xem chi tiết đơn hàng
 */
async function viewOrder(orderId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${orderId}`);
        
        if (!response.ok) {
            throw new Error('Order not found');
        }
        
        const order = await response.json();
        displayOrderDetails(order);
        
    } catch (error) {
        console.error('Error viewing order:', error);
        showToast('Không thể tải chi tiết đơn hàng', 'error');
    }
}

/**
 * Hiển thị chi tiết đơn hàng trong modal
 */
function displayOrderDetails(order) {
    const content = document.getElementById('orderDetailsContent');
    
    const itemsHtml = order.items.map(item => `
        <tr>
            <td>${item.productId}</td>
            <td>${escapeHtml(item.productName)}</td>
            <td>${item.quantity}</td>
            <td>${formatCurrency(item.unitPrice)}</td>
            <td class="price">${formatCurrency(item.subtotal)}</td>
        </tr>
    `).join('');
    
    content.innerHTML = `
        <div class="order-detail-section">
            <h3><i class="fas fa-info-circle"></i> Thông tin đơn hàng</h3>
            <div class="order-info-grid">
                <div class="info-item">
                    <span class="info-label">Mã đơn hàng</span>
                    <span class="info-value"><strong>#${order.orderId}</strong></span>
                </div>
                <div class="info-item">
                    <span class="info-label">Trạng thái</span>
                    <span class="info-value">${getStatusBadge(order.status)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Khách hàng</span>
                    <span class="info-value">${escapeHtml(order.customerName)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Số điện thoại</span>
                    <span class="info-value">${order.customerPhone || '-'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Ngày tạo</span>
                    <span class="info-value">${formatDateTime(order.createdAt)}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">Cập nhật lần cuối</span>
                    <span class="info-value">${order.updatedAt ? formatDateTime(order.updatedAt) : '-'}</span>
                </div>
            </div>
        </div>
        
        <div class="order-detail-section">
            <h3><i class="fas fa-box"></i> Danh sách sản phẩm</h3>
            <table class="items-table">
                <thead>
                    <tr>
                        <th>Mã SP</th>
                        <th>Tên sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
                    ${itemsHtml}
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="4" style="text-align: right;">Tổng cộng:</td>
                        <td class="price">${formatCurrency(order.totalAmount)}</td>
                    </tr>
                </tfoot>
            </table>
        </div>
    `;
    
    document.getElementById('viewOrderModal').classList.add('active');
}

/**
 * Đóng modal xem chi tiết
 */
function closeViewModal() {
    document.getElementById('viewOrderModal').classList.remove('active');
}

/**
 * Mở modal chỉnh sửa đơn hàng
 */
async function editOrder(orderId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${orderId}`);
        
        if (!response.ok) {
            throw new Error('Order not found');
        }
        
        const order = await response.json();
        
        // Fill form with order data
        document.getElementById('editOrderId').value = order.orderId;
        document.getElementById('editCustomerName').value = order.customerName;
        document.getElementById('editCustomerPhone').value = order.customerPhone || '';
        document.getElementById('editStatus').value = order.status;
        
        document.getElementById('editOrderModal').classList.add('active');
        
    } catch (error) {
        console.error('Error loading order for edit:', error);
        showToast('Không thể tải thông tin đơn hàng', 'error');
    }
}

/**
 * Đóng modal chỉnh sửa
 */
function closeEditModal() {
    document.getElementById('editOrderModal').classList.remove('active');
}

/**
 * Xử lý submit form chỉnh sửa đơn hàng
 */
async function handleEditOrder(e) {
    e.preventDefault();
    
    const orderId = document.getElementById('editOrderId').value;
    const customerName = document.getElementById('editCustomerName').value;
    const customerPhone = document.getElementById('editCustomerPhone').value;
    const status = document.getElementById('editStatus').value;
    
    const updateData = {
        customerName,
        customerPhone,
        status
    };
    
    try {
        const response = await fetch(`${API_BASE_URL}/${orderId}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showToast('Cập nhật đơn hàng thành công!', 'success');
            closeEditModal();
            loadOrders();
        } else {
            showToast(result.error || 'Không thể cập nhật đơn hàng', 'error');
        }
        
    } catch (error) {
        console.error('Error updating order:', error);
        showToast('Lỗi kết nối đến server', 'error');
    }
}

/**
 * Xác nhận xóa đơn hàng
 */
function confirmDeleteOrder(orderId) {
    if (confirm('Bạn có chắc chắn muốn xóa đơn hàng này?\n\nThao tác này sẽ khôi phục lại số lượng tồn kho của các sản phẩm.')) {
        deleteOrder(orderId);
    }
}

/**
 * Xóa đơn hàng
 */
async function deleteOrder(orderId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${orderId}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showToast('Xóa đơn hàng thành công!', 'success');
            loadOrders();
        } else {
            showToast(result.error || 'Không thể xóa đơn hàng', 'error');
        }
        
    } catch (error) {
        console.error('Error deleting order:', error);
        showToast('Lỗi kết nối đến server', 'error');
    }
}

/**
 * Hiển thị trạng thái loading
 */
function showLoading() {
    const tbody = document.getElementById('ordersTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="7" class="loading">
                <i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu...
            </td>
        </tr>
    `;
}

/**
 * Hiển thị thông báo lỗi
 */
function showError(message) {
    const tbody = document.getElementById('ordersTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center" style="color: #dc3545;">
                <i class="fas fa-exclamation-triangle"></i> ${escapeHtml(message)}
            </td>
        </tr>
    `;
}

/**
 * Hiển thị toast notification
 */
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    
    // Show toast
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    // Hide after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3100);
}

/**
 * Tạo status badge HTML
 */
function getStatusBadge(status) {
    const statusMap = {
        'PENDING': { label: 'Đang chờ', class: 'pending' },
        'CONFIRMED': { label: 'Đã xác nhận', class: 'confirmed' },
        'SHIPPED': { label: 'Đang giao', class: 'shipped' },
        'COMPLETED': { label: 'Hoàn thành', class: 'completed' },
        'CANCELLED': { label: 'Đã hủy', class: 'cancelled' }
    };
    
    const statusInfo = statusMap[status] || { label: status, class: 'pending' };
    return `<span class="status-badge ${statusInfo.class}">${statusInfo.label}</span>`;
}

/**
 * Format số tiền
 */
function formatCurrency(amount) {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

/**
 * Format ngày giờ
 */
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '-';
    
    const date = new Date(dateTimeString);
    return new Intl.DateTimeFormat('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    }).format(date);
}

/**
 * Escape HTML để tránh XSS
 */
function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}
