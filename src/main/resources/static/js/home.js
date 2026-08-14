let allBuildings = [];
let currentSelectedRoomId = null;
let roomToCancelId = null;
let bookingHistory = [];

document.addEventListener('DOMContentLoaded', () => {
    setDefaultDate();
    
    fetch('/api/buildings')
        .then(response => response.json())
        .then(data => {
            allBuildings = data;
            populateBuildingFilter(allBuildings);
            renderRooms(allBuildings);
        })
        .catch(error => console.error('Lỗi lấy dữ liệu từ API:', error));
});

function initIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}

function setDefaultDate() {
    const dateInput = document.getElementById('date-select');
    if(dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.value = today;
    }
}

function populateBuildingFilter(buildings) {
    const select = document.getElementById('building-select');
    select.innerHTML = '<option value="ALL">Chọn tất cả</option>';
    
    buildings.forEach(b => {
        select.innerHTML += `<option value="${b.maToaNha}">${b.tenToaNha}</option>`;
    });
}

// Render dữ liệu ra màn hình
function renderRooms(buildingsToRender) {
    const roomList = document.getElementById('room-list');
    roomList.innerHTML = '';

    // Kiểm tra xem có tòa nhà nào hoặc phòng nào không
    const hasAnyRoom = buildingsToRender.some(b => b.rooms && b.rooms.length > 0);
    
    if (!hasAnyRoom) {
        roomList.innerHTML = '<p class="no-data empty-state">Không tìm thấy phòng phù hợp.</p>';
        return;
    }

    // Duyệt qua từng tòa nhà
    buildingsToRender.forEach(building => {
        if (!building.rooms || building.rooms.length === 0) return; // Bỏ qua tòa nhà không có phòng

        const section = document.createElement('div');
        section.className = 'building-section';
        
        section.innerHTML = `
            <h2 class="building-title"><i data-lucide="building"></i> ${building.tenToaNha}</h2>
            <div class="room-grid"></div>
        `;
        
        const grid = section.querySelector('.room-grid');
        
        // Duyệt qua từng phòng trong tòa nhà
        building.rooms.forEach(room => {
            const isBorrowed = room.trangThai !== '0'; // '0' là trống, khác '0' là đang mượn
            const card = document.createElement('div');
            card.className = 'room-card' + (isBorrowed ? ' borrowed-card' : '');
            
            let actionButton = '';
            if (isBorrowed) {
                actionButton = `<button class="btn-danger btn-action" onclick="openCancelModal('${room.maPhong}', '${room.tenPhong}')">
                    <i data-lucide="x-circle"></i> Hủy đăng ký
                </button>`;
            } else {
                actionButton = `<button class="btn-primary btn-action" onclick="openModal('${room.maPhong}', '${room.tenPhong}')">
                    Đăng ký mượn
                </button>`;
            }

            card.innerHTML = `
                <div class="card-header">
                    <div class="card-title">${room.tenPhong}</div>
                    <div class="card-capacity">
                        <i data-lucide="users"></i> ${room.sucChua}
                    </div>
                </div>
                <div class="card-body">
                    <div class="equipment-title">Túi đồ bao gồm:</div>
                    <ul class="equipment-list">
                        <!-- Tạm thời fix cứng do chưa có bảng Thiết bị -->
                        <li class="equipment-item"><i data-lucide="check-square"></i> 1 Mic</li>
                        <li class="equipment-item"><i data-lucide="check-square"></i> 1 ĐK Máy chiếu</li>
                        <li class="equipment-item"><i data-lucide="check-square"></i> 1 ĐK Điều hòa</li>
                        <li class="equipment-item"><i data-lucide="check-square"></i> 1 Chìa khóa</li>
                    </ul>
                </div>
                <div class="card-footer">
                    ${actionButton}
                </div>
            `;
            grid.appendChild(card);
        });
        
        roomList.appendChild(section);
    });

    initIcons();
}

// Filter Logic
function applyFilters() {
    const selectedBuildingCode = document.getElementById('building-select').value;
    
    let filtered = allBuildings;
    if (selectedBuildingCode !== 'ALL') {
        filtered = allBuildings.filter(b => b.maToaNha === selectedBuildingCode);
    }
    
    renderRooms(filtered);
}

// Navigation Logic
function toggleView(view) {
    const bookingView = document.getElementById('booking-view');
    const historyView = document.getElementById('history-view');
    const btnHome = document.getElementById('btn-home');
    const btnHistory = document.getElementById('btn-history');

    if (view === 'history') {
        bookingView.classList.add('hidden');
        historyView.classList.remove('hidden');
        btnHistory.classList.add('active-nav');
        btnHome.classList.remove('active-nav');
        renderHistory();
    } else {
        bookingView.classList.remove('hidden');
        historyView.classList.add('hidden');
        btnHome.classList.add('active-nav');
        btnHistory.classList.remove('active-nav');
        applyFilters();
    }
}

// Render History View
function renderHistory() {
    const container = document.getElementById('history-list');
    container.innerHTML = '';
    
    if (bookingHistory.length === 0) {
        container.innerHTML = '<div class="empty-state">Chưa có lịch sử mượn phòng nào.</div>';
        return;
    }
    
    const sortedHistory = [...bookingHistory].reverse();
    
    sortedHistory.forEach(record => {
        const item = document.createElement('div');
        item.className = 'history-item';
        
        const statusBadge = record.status === 'active' 
            ? `<span class="badge badge-active">Đang mượn</span>`
            : `<span class="badge badge-canceled">Đã hủy</span>`;
            
        let actionBtn = '';
        if (record.status === 'active') {
            actionBtn = `<button class="btn-danger btn-sm" onclick="openCancelModal('${record.roomId}', '${record.roomName}')">
                <i data-lucide="x-circle"></i> Hủy
            </button>`;
        }

        item.innerHTML = `
            <div class="history-info">
                <div class="history-title">
                    <h4>${record.roomName} - ${record.building}</h4>
                    ${statusBadge}
                </div>
                <div class="history-details">
                    <span><i data-lucide="calendar"></i> ${new Date(record.date).toLocaleDateString('vi-VN')}</span>
                    <span><i data-lucide="clock"></i> ${record.shift}</span>
                    <span><i data-lucide="info"></i> Đã đặt lúc: ${record.timestamp}</span>
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

// Modal Logic - Borrow
function openModal(roomId, roomName) {
    currentSelectedRoomId = roomId;
    document.getElementById('modal-room-name').textContent = roomName;
    
    const selectedDate = document.getElementById('date-select').value;
    const shiftSelect = document.getElementById('shift-select');
    const selectedShiftText = shiftSelect.options[shiftSelect.selectedIndex].text;
    
    const dateObj = new Date(selectedDate);
    document.getElementById('modal-date').textContent = dateObj.toLocaleDateString('vi-VN');
    document.getElementById('modal-shift').textContent = selectedShiftText;

    document.getElementById('borrow-modal').classList.add('active');
}

function closeModal() {
    currentSelectedRoomId = null;
    document.getElementById('borrow-modal').classList.remove('active');
}

function confirmBorrow() {
    if (currentSelectedRoomId) {
        
        // Thu thập dữ liệu từ giao diện
        const selectedDate = document.getElementById('date-select').value;
        const shiftValue = document.getElementById('shift-select').value;
        
        const requestData = {
            maPhong: currentSelectedRoomId,
            ngayMuon: selectedDate,
            caMuon: parseInt(shiftValue)
        };

        // Bắn API
        fetch('/api/bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Có lỗi xảy ra hoặc chưa đăng nhập');
            return response.json();
        })
        .then(data => {
            closeModal();
            showToast('Đăng ký mượn phòng thành công!', 'success');
            
            // Lấy lại danh sách phòng mới nhất từ server để giao diện tự cập nhật thẻ màu đỏ
            fetch('/api/buildings')
                .then(res => res.json())
                .then(newData => {
                    allBuildings = newData;
                    applyFilters();
                });
        })
        .catch(error => {
            closeModal();
            showToast('Lỗi: Không thể mượn phòng!', 'danger');
            console.error(error);
        });
    }
}

// Modal Logic - Cancel
function openCancelModal(roomId, roomName) {
    roomToCancelId = roomId;
    document.getElementById('cancel-modal-room-name').textContent = roomName;
    document.getElementById('cancel-modal').classList.add('active');
}

function closeCancelModal() {
    roomToCancelId = null;
    document.getElementById('cancel-modal').classList.remove('active');
}

function confirmCancel() {
    if (roomToCancelId) {
        // Tạm thời update data ở FE, sau này gọi API thật
        let foundRoom = null;
        for (let b of allBuildings) {
            let r = b.rooms.find(x => x.maPhong === roomToCancelId);
            if (r) { foundRoom = r; break; }
        }

        if (foundRoom) {
            foundRoom.trangThai = '0'; // Trả về trạng thái trống
            
            const booking = bookingHistory.find(b => b.roomId === roomToCancelId && b.status === 'active');
            if (booking) {
                booking.status = 'canceled';
            }
            
            applyFilters();
            if (!document.getElementById('history-view').classList.contains('hidden')) {
                renderHistory();
            }
            closeCancelModal();
            showToast('Đã hủy đăng ký thành công!', 'danger');
        }
    }
}

// Toast Logic
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
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// Event Listeners Setup
document.addEventListener('DOMContentLoaded', () => {
    // Nav Events
    document.getElementById('btn-home').addEventListener('click', () => toggleView('home'));
    document.getElementById('btn-history').addEventListener('click', () => toggleView('history'));

    // Borrow Modal Events
    document.getElementById('btn-close-modal').addEventListener('click', closeModal);
    document.getElementById('btn-cancel-modal').addEventListener('click', closeModal);
    document.getElementById('btn-confirm-modal').addEventListener('click', confirmBorrow);
    
    // Cancel Modal Events
    document.getElementById('btn-close-cancel-modal').addEventListener('click', closeCancelModal);
    document.getElementById('btn-cancel-cancel-modal').addEventListener('click', closeCancelModal);
    document.getElementById('btn-confirm-cancel-modal').addEventListener('click', confirmCancel);
    
    // Đóng Modal khi click ra ngoài
    window.addEventListener('click', (e) => {
        if (e.target.id === 'borrow-modal') closeModal();
        if (e.target.id === 'cancel-modal') closeCancelModal();
    });

    // Nút Tìm kiếm lọc theo Tòa nhà
    document.getElementById('btn-search').addEventListener('click', () => {
        applyFilters();
        showToast('Đã cập nhật danh sách phòng', 'success');
    });
});

// Expose functions globally cho các inline onclick handlers ở HTML
window.openModal = openModal;
window.openCancelModal = openCancelModal;