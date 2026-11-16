/**
 * Product Management System - Frontend JavaScript
 */

const API_BASE_URL = '/api/products';

let allProducts = [];
let currentFilter = '';
let isEditMode = false;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Product Management System initialized');
    loadProducts();
    setupEventListeners();
});

function setupEventListeners() {
    document.getElementById('createProductBtn').addEventListener('click', openCreateModal);
    document.getElementById('refreshBtn').addEventListener('click', loadProducts);
    document.getElementById('statusFilter').addEventListener('change', function(e) {
        currentFilter = e.target.value;
        filterProducts();
    });
    document.getElementById('productForm').addEventListener('submit', handleProductFormSubmit);
    document.getElementById('stockForm').addEventListener('submit', handleStockAdjustment);
}

async function loadProducts() {
    try {
        showLoading();
        const response = await fetch(API_BASE_URL);
        
        if (!response.ok) {
            throw new Error('Failed to load products');
        }
        
        allProducts = await response.json();
        console.log('Loaded products:', allProducts.length);
        
        filterProducts();
        updateStatistics();
        
    } catch (error) {
        console.error('Error loading products:', error);
        showToast('Không thể tải danh sách sản phẩm', 'error');
        showError('Không thể tải dữ liệu. Vui lòng thử lại.');
    }
}

function filterProducts() {
    let filteredProducts = allProducts;
    
    if (currentFilter === 'active') {
        filteredProducts = allProducts.filter(p => p.isActive === true);
    } else if (currentFilter === 'inactive') {
        filteredProducts = allProducts.filter(p => p.isActive === false);
    }
    
    displayProducts(filteredProducts);
}

function displayProducts(products) {
    const tbody = document.getElementById('productsTableBody');
    
    if (products.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center">
                    <i class="fas fa-inbox"></i> Không có sản phẩm nào
                </td>
            </tr>
        `;
        return;
    }
    
    tbody.innerHTML = products.map(product => `
        <tr>
            <td><strong>#${product.productId}</strong></td>
            <td>${escapeHtml(product.productName)}</td>
            <td>${escapeHtml(product.description || '-')}</td>
            <td class="price">${formatCurrency(product.price)}</td>
            <td>${getStockBadge(product.stockQuantity)}</td>
            <td>${getStatusBadge(product.isActive)}</td>
            <td>
                <div class="action-buttons">
                    <button class="action-btn edit" onclick="editProduct(${product.productId})" title="Chỉnh sửa">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="action-btn stock" onclick="adjustStock(${product.productId})" title="Điều chỉnh tồn kho">
                        <i class="fas fa-warehouse"></i>
                    </button>
                    <button class="action-btn delete" onclick="confirmDeleteProduct(${product.productId})" title="Xóa">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function updateStatistics() {
    const total = allProducts.length;
    const active = allProducts.filter(p => p.isActive).length;
    const totalStock = allProducts.reduce((sum, p) => sum + p.stockQuantity, 0);
    const lowStock = allProducts.filter(p => p.stockQuantity < 10).length;
    
    document.getElementById('totalCount').textContent = total;
    document.getElementById('activeCount').textContent = active;
    document.getElementById('totalStock').textContent = totalStock;
    document.getElementById('lowStockCount').textContent = lowStock;
}

function openCreateModal() {
    isEditMode = false;
    document.getElementById('modalTitle').textContent = 'Thêm Sản Phẩm';
    document.getElementById('productForm').reset();
    document.getElementById('productId').value = '';
    document.getElementById('isActive').checked = true;
    document.getElementById('productModal').classList.add('active');
}

function closeProductModal() {
    document.getElementById('productModal').classList.remove('active');
}

async function editProduct(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${productId}`);
        
        if (!response.ok) {
            throw new Error('Product not found');
        }
        
        const product = await response.json();
        
        isEditMode = true;
        document.getElementById('modalTitle').textContent = 'Cập Nhật Sản Phẩm';
        document.getElementById('productId').value = product.productId;
        document.getElementById('productName').value = product.productName;
        document.getElementById('description').value = product.description || '';
        document.getElementById('price').value = product.price;
        document.getElementById('stockQuantity').value = product.stockQuantity;
        document.getElementById('isActive').checked = product.isActive;
        
        document.getElementById('productModal').classList.add('active');
        
    } catch (error) {
        console.error('Error loading product:', error);
        showToast('Không thể tải thông tin sản phẩm', 'error');
    }
}

async function handleProductFormSubmit(e) {
    e.preventDefault();
    
    const productId = document.getElementById('productId').value;
    const productData = {
        productName: document.getElementById('productName').value,
        description: document.getElementById('description').value,
        price: parseFloat(document.getElementById('price').value),
        stockQuantity: parseInt(document.getElementById('stockQuantity').value),
        isActive: document.getElementById('isActive').checked
    };
    
    try {
        let response;
        
        if (isEditMode && productId) {
            response = await fetch(`${API_BASE_URL}/${productId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(productData)
            });
        } else {
            response = await fetch(API_BASE_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(productData)
            });
        }
        
        const result = await response.json();
        
        if (response.ok) {
            showToast(isEditMode ? 'Cập nhật sản phẩm thành công!' : 'Thêm sản phẩm thành công!', 'success');
            closeProductModal();
            loadProducts();
        } else {
            showToast(result.error || 'Không thể lưu sản phẩm', 'error');
        }
        
    } catch (error) {
        console.error('Error saving product:', error);
        showToast('Lỗi kết nối đến server', 'error');
    }
}

function confirmDeleteProduct(productId) {
    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
        deleteProduct(productId);
    }
}

async function deleteProduct(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${productId}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showToast('Xóa sản phẩm thành công!', 'success');
            loadProducts();
        } else {
            showToast(result.error || 'Không thể xóa sản phẩm', 'error');
        }
        
    } catch (error) {
        console.error('Error deleting product:', error);
        showToast('Lỗi kết nối đến server', 'error');
    }
}

async function adjustStock(productId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${productId}`);
        
        if (!response.ok) {
            throw new Error('Product not found');
        }
        
        const product = await response.json();
        
        document.getElementById('stockProductId').value = product.productId;
        document.getElementById('stockProductName').textContent = product.productName;
        document.getElementById('currentStock').textContent = product.stockQuantity;
        document.getElementById('adjustmentType').value = 'increase';
        document.getElementById('adjustmentQuantity').value = '';
        
        document.getElementById('stockModal').classList.add('active');
        
    } catch (error) {
        console.error('Error loading product for stock adjustment:', error);
        showToast('Không thể tải thông tin sản phẩm', 'error');
    }
}

function closeStockModal() {
    document.getElementById('stockModal').classList.remove('active');
}

async function handleStockAdjustment(e) {
    e.preventDefault();
    
    const productId = document.getElementById('stockProductId').value;
    const adjustmentType = document.getElementById('adjustmentType').value;
    const quantity = parseInt(document.getElementById('adjustmentQuantity').value);
    
    if (!quantity || quantity <= 0) {
        showToast('Số lượng phải lớn hơn 0', 'error');
        return;
    }
    
    try {
        const endpoint = adjustmentType === 'increase' ? 'increase-stock' : 'decrease-stock';
        const response = await fetch(`${API_BASE_URL}/${productId}/${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity })
        });
        
        const result = await response.json();
        
        if (response.ok) {
            showToast('Điều chỉnh tồn kho thành công!', 'success');
            closeStockModal();
            loadProducts();
        } else {
            showToast(result.error || 'Không thể điều chỉnh tồn kho', 'error');
        }
        
    } catch (error) {
        console.error('Error adjusting stock:', error);
        showToast('Lỗi kết nối đến server', 'error');
    }
}

function showLoading() {
    const tbody = document.getElementById('productsTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="7" class="loading">
                <i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu...
            </td>
        </tr>
    `;
}

function showError(message) {
    const tbody = document.getElementById('productsTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="7" class="text-center" style="color: #dc3545;">
                <i class="fas fa-exclamation-triangle"></i> ${escapeHtml(message)}
            </td>
        </tr>
    `;
}

function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3100);
}

function getStatusBadge(isActive) {
    return isActive 
        ? '<span class="status-badge active">Đang bán</span>'
        : '<span class="status-badge inactive">Ngừng bán</span>';
}

function getStockBadge(quantity) {
    let badgeClass = 'normal';
    if (quantity === 0) {
        badgeClass = 'out';
    } else if (quantity < 10) {
        badgeClass = 'low';
    }
    return `<span class="stock-badge ${badgeClass}">${quantity}</span>`;
}

function formatCurrency(amount) {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

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
