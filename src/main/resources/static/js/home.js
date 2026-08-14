// Global State
let allBuildings = [];
let currentSelectedRoomId = null;

document.addEventListener('DOMContentLoaded', () => {
    setDefaultDate();
    
    // Gọi API của Spring Boot để lấy danh sách Tòa nhà & Phòng học
    fetch('/api/buildings')
        .then(response => response.json())
        .then(data => {
            allBuildings = data;
            populateBuildingFilter(allBuildings);
            renderRooms(allBuildings);
        })
        .catch(error => console.error('Lỗi lấy dữ liệu từ API:', error));
});

// Khởi tạo Lucide Icons
function initIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}

// Set mặc định ngày hôm nay cho ô Input Date
function setDefaultDate() {
    const dateInput = document.getElementById('date-select');
    if(dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.value = today;
    }
}

// Đổ dữ liệu Tòa nhà vào thẻ <select>
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

    const hasAnyRoom = buildingsToRender.some(b => b.rooms && b.rooms.length > 0);
    
    if (!hasAnyRoom) {
        roomList.innerHTML = '<p class="no-data empty-state">Không tìm thấy phòng phù hợp.</p>';
        return;
    }

    buildingsToRender.forEach(building => {
        if (!building.rooms || building.rooms.length === 0) return;

        const section = document.createElement('div');
        section.className = 'building-section';
        
        section.innerHTML = `
            <h2 class="building-title"><i data-lucide="building"></i> ${building.tenToaNha}</h2>
            <div class="room-grid"></div>
        `;
        
        const grid = section.querySelector('.room-grid');
        
        building.rooms.forEach(room => {
            const isBorrowed = room.trangThai !== '0';
            const card = document.createElement('div');
            card.className = 'room-card' + (isBorrowed ? ' borrowed-card' : '');
            
            // Logic Nút bấm: Đang mượn thì làm mờ không cho bấm, Trống thì cho mượn
            let actionButton = '';
            if (isBorrowed) {
                actionButton = `<button class="btn-secondary btn-action" disabled style="cursor: not-allowed; opacity: 0.7;">
                    <i data-lucide="lock"></i> Đang mượn
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

// Logic Lọc theo Tòa nhà
function applyFilters() {
    const selectedBuildingCode = document.getElementById('building-select').value;
    
    let filtered = allBuildings;
    if (selectedBuildingCode !== 'ALL') {
        filtered = allBuildings.filter(b => b.maToaNha === selectedBuildingCode);
    }
    
    renderRooms(filtered);
}

// Modal Logic - Mở Form Xác nhận
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

// Modal Logic - Đóng Form
function closeModal() {
    currentSelectedRoomId = null;
    document.getElementById('borrow-modal').classList.remove('active');
}

// Gọi API POST để mượn phòng
function confirmBorrow() {
    if (currentSelectedRoomId) {
        const selectedDate = document.getElementById('date-select').value;
        const shiftValue = document.getElementById('shift-select').value;
        
        const requestData = {
            maPhong: currentSelectedRoomId,
            ngayMuon: selectedDate,
            caMuon: parseInt(shiftValue)
        };

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
            
            // Gọi lại API để load màu nền thẻ phòng (từ trắng sang đỏ)
            fetch('/api/buildings')
                .then(res => res.json())
                .then(newData => {
                    allBuildings = newData;
                    applyFilters();
                });
        })
        .catch(error => {
            closeModal();
            showToast('Vui lòng đăng nhập', 'danger');
            console.error(error);
        });
    }
}

// Toast Thông báo góc dưới màn hình
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

// Cài đặt các sự kiện (Event Listeners)
document.addEventListener('DOMContentLoaded', () => {
    // Sự kiện Modal
    document.getElementById('btn-close-modal').addEventListener('click', closeModal);
    document.getElementById('btn-cancel-modal').addEventListener('click', closeModal);
    document.getElementById('btn-confirm-modal').addEventListener('click', confirmBorrow);
    
    // Đóng Modal khi click ra khoảng trống bên ngoài
    window.addEventListener('click', (e) => {
        if (e.target.id === 'borrow-modal') closeModal();
    });

    // Sự kiện Nút Tìm kiếm
    document.getElementById('btn-search').addEventListener('click', () => {
        applyFilters();
        showToast('Đã cập nhật danh sách phòng', 'success');
    });
});

// Expose hàm ra toàn cục để HTML gọi được (onclick)
window.openModal = openModal;