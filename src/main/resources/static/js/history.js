document.addEventListener('DOMContentLoaded', () => { //[cite: 18]
    fetchHistory(); //[cite: 18]
}); //[cite: 18]

function initIcons() { //[cite: 18]
    if (window.lucide) { //[cite: 18]
        lucide.createIcons(); //[cite: 18]
    } //[cite: 18]
} //[cite: 18]

const shiftMap = { //[cite: 18]
    1: "Ca 1 (07:00 - 09:30)", //[cite: 18]
    2: "Ca 2 (09:30 - 12:00)", //[cite: 18]
    3: "Ca 3 (13:00 - 15:30)", //[cite: 18]
    4: "Ca 4 (15:30 - 18:00)", //[cite: 18]
    5: "Ca 5 (18:00 - 20:30)" //[cite: 18]
}; //[cite: 18]

function fetchHistory() { //[cite: 18]
    fetch('/api/lich-su')
        .then(response => {
            if (response.status === 401) {
                window.location.href = '/login';
                throw new Error('Chưa đăng nhập');
            }
            if (!response.ok) throw new Error('Lỗi server');
            return response.json();
        })
        .then(data => {
            renderHistory(data); 
        })
        .catch(error => { 
            document.getElementById('history-list').innerHTML = '<div class="empty-state">Lỗi tải dữ liệu. Vui lòng đăng nhập lại!</div>'; 
            console.error(error); 
        });
} //[cite: 18]

function renderHistory(histories) { //[cite: 18]
    const container = document.getElementById('history-list'); //[cite: 18]
    container.innerHTML = ''; //[cite: 18]
    
    if (!histories || histories.length === 0) { //[cite: 18]
        container.innerHTML = '<div class="empty-state">Chưa có lịch sử mượn phòng nào.</div>'; //[cite: 18]
        return; //[cite: 18]
    } //[cite: 18]
    
    histories.forEach(record => { //[cite: 18]
        const item = document.createElement('div'); //[cite: 18]
        item.className = 'history-item'; //[cite: 18]
        
        // Setup Badge Trạng thái (Cập nhật logic Đã trả)
        const statusBadge = record.trangThai === 'ACTIVE' 
            ? `<span class="badge badge-active">Đang mượn</span>`
            : `<span class="badge badge-canceled" style="background: #E2E8F0; color: #4A5568;">Đã trả</span>`;
            
        // Setup Nút Trả phòng (Cập nhật giao diện & hàm gọi)
        let actionBtn = '';
        if (record.trangThai === 'ACTIVE') {
            actionBtn = `<button class="btn-primary btn-sm" onclick="returnBooking(${record.id})">
                <i data-lucide="check-circle"></i> Trả thiết bị
            </button>`;
        }

        const borrowDate = new Date(record.ngayMuon).toLocaleDateString('vi-VN'); //[cite: 18]
        const createdTime = new Date(record.thoiGianTao).toLocaleString('vi-VN'); //[cite: 18]
        const shiftText = shiftMap[record.caMuon] || `Ca ${record.caMuon}`; //[cite: 18]

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
        container.appendChild(item); //[cite: 18]
    }); //[cite: 18]
    
    initIcons(); //[cite: 18]
} //[cite: 18]

window.returnBooking = function(id) {
    if (confirm('Xác nhận trả phòng và thiết bị?')) {
        fetch(`/api/bookings/${id}/return`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) throw new Error('Lỗi');
            return response.json();
        })
        .then(data => {
            showToast('Trả thiết bị thành công!', 'success'); 
            fetchHistory();
        })
        .catch(error => {
            showToast('Có lỗi xảy ra, không thể trả!', 'danger');
        });
    }
}

function showToast(message, type = 'success') { //[cite: 18]
    const toast = document.getElementById('toast'); //[cite: 18]
    const toastIconContainer = document.getElementById('toast-icon-container'); //[cite: 18]
    
    document.getElementById('toast-message').textContent = message; //[cite: 18]
    
    if (type === 'danger') { //[cite: 18]
        toast.style.backgroundColor = 'var(--danger)'; //[cite: 18]
        toastIconContainer.innerHTML = '<i data-lucide="alert-circle"></i>'; //[cite: 18]
    } else { //[cite: 18]
        toast.style.backgroundColor = 'var(--success)'; //[cite: 18]
        toastIconContainer.innerHTML = '<i data-lucide="check-circle"></i>'; //[cite: 18]
    } //[cite: 18]
    
    initIcons();  //[cite: 18]
    toast.classList.add('show'); //[cite: 18]
    setTimeout(() => { toast.classList.remove('show'); }, 3000); //[cite: 18]
} //[cite: 18]