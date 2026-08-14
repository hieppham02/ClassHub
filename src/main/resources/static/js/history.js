document.addEventListener('DOMContentLoaded', () => {
    fetchHistory();
});

function initIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}

const shiftMap = {
    1: "Ca 1 (07:00 - 09:30)",
    2: "Ca 2 (09:30 - 12:00)",
    3: "Ca 3 (13:00 - 15:30)",
    4: "Ca 4 (15:30 - 18:00)",
    5: "Ca 5 (18:00 - 20:30)"
};

function fetchHistory() {
    fetch('/api/lich-su')
        .then(response => {
            if (!response.ok) throw new Error('Chưa đăng nhập hoặc lỗi server');
            return response.json();
        })
        .then(data => {
            renderHistory(data);
        })
        .catch(error => {
            document.getElementById('history-list').innerHTML = '<div class="empty-state">Lỗi tải dữ liệu. Vui lòng đăng nhập lại!</div>';
            console.error(error);
        });
}

function renderHistory(histories) {
    const container = document.getElementById('history-list');
    container.innerHTML = '';
    
    if (!histories || histories.length === 0) {
        container.innerHTML = '<div class="empty-state">Chưa có lịch sử mượn phòng nào.</div>';
        return;
    }
    
    histories.forEach(record => {
        const item = document.createElement('div');
        item.className = 'history-item';
        
        // Setup Badge Trạng thái
        const statusBadge = record.trangThai === 'ACTIVE' 
            ? `<span class="badge badge-active">Đang mượn</span>`
            : `<span class="badge badge-canceled">Đã hủy</span>`;
            
        // Setup Nút Hủy
        let actionBtn = '';
        if (record.trangThai === 'ACTIVE') {
            actionBtn = `<button class="btn-danger btn-sm" onclick="cancelBooking(${record.id})">
                <i data-lucide="x-circle"></i> Hủy
            </button>`;
        }

        const borrowDate = new Date(record.ngayMuon).toLocaleDateString('vi-VN');
        const createdTime = new Date(record.thoiGianTao).toLocaleString('vi-VN');
        const shiftText = shiftMap[record.caMuon] || `Ca ${record.caMuon}`;

        item.innerHTML = `
            <div class="history-info">
                <div class="history-title">
                    <h4>${record.room.tenPhong} - ${record.room.building.tenToaNha}</h4>
                    ${statusBadge}
                </div>
                <div class="history-details">
                    <span><i data-lucide="calendar"></i> ${borrowDate}</span>
                    <span><i data-lucide="clock"></i> ${shiftText}</span>
                    <span><i data-lucide="info"></i> Đã đặt lúc: ${createdTime}</span>
                </div>
            </div>
            <div class="history-action">
                ${actionBtn}
            </div>
        `;
        container.appendChild(item);
    });
    
    initIcons();
}

window.cancelBooking = function(id) {
    if (confirm('Bạn có chắc chắn muốn hủy mượn phòng này?')) {
        fetch(`/api/bookings/${id}/cancel`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) throw new Error('Lỗi hủy phòng');
            return response.json();
        })
        .then(data => {
            showToast('Hủy phòng thành công!', 'danger');
            fetchHistory(); // Load lại danh sách ngay lập tức
        })
        .catch(error => {
            showToast('Có lỗi xảy ra, không thể hủy!', 'danger');
        });
    }
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    const toastIconContainer = document.getElementById('toast-icon-container');
    
    document.getElementById('toast-message').textContent = message;
    
    if (type === 'danger') {
        toast.style.backgroundColor = 'var(--danger)';
        toastIconContainer.innerHTML = '<i data-lucide="alert-circle"></i>';
    } else {
        toast.style.backgroundColor = 'var(--success)';
        toastIconContainer.innerHTML = '<i data-lucide="check-circle"></i>';
    }
    
    initIcons(); 
    toast.classList.add('show');
    setTimeout(() => { toast.classList.remove('show'); }, 3000);
}