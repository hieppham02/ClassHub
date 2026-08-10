// Mock Data for Rooms across 4 Buildings
const mockRooms = [
    { id: "A501", name: "Phòng 501", building: "Tòa A", capacity: 40, equipment: ["1 Mic", "1 ĐK Máy chiếu", "1 ĐK Điều hòa", "1 Chìa khóa"], status: "available" },
    { id: "A502", name: "Phòng 502", building: "Tòa A", capacity: 60, equipment: ["2 Mic", "1 ĐK Máy chiếu", "2 ĐK Điều hòa", "1 Chìa khóa"], status: "available" },
    { id: "B201", name: "Phòng 201", building: "Tòa B", capacity: 35, equipment: ["1 Mic", "1 ĐK Máy chiếu", "1 ĐK Điều hòa", "1 Chìa khóa"], status: "available" },
    { id: "B202", name: "Phòng 202", building: "Tòa B", capacity: 80, equipment: ["2 Mic", "1 ĐK Máy chiếu", "4 ĐK Điều hòa", "1 Chìa khóa"], status: "available" },
    { id: "C301", name: "Phòng 301", building: "Tòa C", capacity: 50, equipment: ["1 Mic", "1 ĐK Máy chiếu", "2 ĐK Điều hòa", "1 Chìa khóa"], status: "available" },
    { id: "C302", name: "Phòng 302", building: "Tòa C", capacity: 40, equipment: ["1 Mic", "1 ĐK Máy chiếu", "2 ĐK Điều hòa", "1 Chìa khóa"], status: "available" },
    { id: "D401", name: "Phòng 401", building: "Tòa D", capacity: 100, equipment: ["4 Mic", "2 ĐK Máy chiếu", "4 ĐK Điều hòa", "1 Chìa khóa"], status: "available" },
    { id: "D402", name: "Phòng 402", building: "Tòa D", capacity: 30, equipment: ["1 Mic", "1 ĐK Máy chiếu", "1 ĐK Điều hòa", "1 Chìa khóa"], status: "available" }
];

// Global State
let currentSelectedRoomId = null;
let roomToCancelId = null;
let bookingHistory = [];

// [TÍCH HỢP SPRING BOOT]
// TODO: Thay thế mockRooms bằng API Call tới Backend Spring Boot.
// Ví dụ:
// fetch('/api/rooms').then(res => res.json()).then(data => renderRooms(data))

// Initialize Lucide Icons
function initIcons() {
    if (window.lucide) {
        lucide.createIcons();
    }
}

// Set default date to today
function setDefaultDate() {
    const dateInput = document.getElementById('date-select');
    if(dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.value = today;
    }
}

// Render Rooms Grouped by Building
function renderRooms(rooms) {
    const roomList = document.getElementById('room-list');
    roomList.innerHTML = '';

    if (rooms.length === 0) {
        roomList.innerHTML = '<p class="no-data empty-state">Không tìm thấy phòng phù hợp.</p>';
        return;
    }

    // Group rooms by building
    const grouped = {};
    rooms.forEach(room => {
        if (!grouped[room.building]) {
            grouped[room.building] = [];
        }
        grouped[room.building].push(room);
    });

    // Render each building section
    Object.keys(grouped).sort().forEach(buildingName => {
        const section = document.createElement('div');
        section.className = 'building-section';
        
        section.innerHTML = `
            <h2 class="building-title"><i data-lucide="building"></i> ${buildingName}</h2>
            <div class="room-grid"></div>
        `;
        
        const grid = section.querySelector('.room-grid');
        
        grouped[buildingName].forEach(room => {
            const isBorrowed = room.status === 'borrowed';
            const card = document.createElement('div');
            card.className = 'room-card' + (isBorrowed ? ' borrowed-card' : '');
            
            let actionButton = '';
            if (isBorrowed) {
                actionButton = `<button class="btn-danger btn-action" onclick="openCancelModal('${room.id}', '${room.name}')">
                    <i data-lucide="x-circle"></i> Hủy đăng ký
                </button>`;
            } else {
                actionButton = `<button class="btn-primary btn-action" onclick="openModal('${room.id}', '${room.name}')">
                    Đăng ký mượn
                </button>`;
            }

            card.innerHTML = `
                <div class="card-header">
                    <div class="card-title">${room.name}</div>
                    <div class="card-capacity">
                        <i data-lucide="users"></i> ${room.capacity}
                    </div>
                </div>
                <div class="card-body">
                    <div class="equipment-title">Túi đồ bao gồm:</div>
                    <ul class="equipment-list">
                        ${room.equipment.map(item => `
                            <li class="equipment-item">
                                <i data-lucide="check-square"></i> ${item}
                            </li>
                        `).join('')}
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
    const building = document.getElementById('building-select').value;
    
    let filtered = mockRooms;
    if (building !== 'ALL') {
        filtered = mockRooms.filter(r => r.building === building);
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
    
    // Sort by timestamp descending
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
        // [TÍCH HỢP SPRING BOOT]
        // TODO: Gọi API POST tới backend để lưu thông tin mượn phòng.
        // fetch('/api/bookings', { method: 'POST', body: JSON.stringify({...}) })
        //   .then(() => { ... })
        
        const room = mockRooms.find(r => r.id === currentSelectedRoomId);
        room.status = 'borrowed';
        
        const selectedDate = document.getElementById('date-select').value;
        const shiftSelect = document.getElementById('shift-select');
        const selectedShiftText = shiftSelect.options[shiftSelect.selectedIndex].text;
        
        // Add to history
        bookingHistory.push({
            id: Date.now().toString(),
            roomId: room.id,
            roomName: room.name,
            building: room.building,
            date: selectedDate,
            shift: selectedShiftText,
            timestamp: new Date().toLocaleString('vi-VN'),
            status: 'active'
        });
        
        applyFilters(); 
        closeModal();
        showToast('Đăng ký mượn phòng thành công!', 'success');
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
        // [TÍCH HỢP SPRING BOOT]
        // TODO: Gọi API DELETE/PUT tới backend để hủy lịch mượn.
        // fetch(`/api/bookings/${roomToCancelId}`, { method: 'DELETE' }).then(...)
        
        const room = mockRooms.find(r => r.id === roomToCancelId);
        room.status = 'available';
        
        // Update history
        const booking = bookingHistory.find(b => b.roomId === roomToCancelId && b.status === 'active');
        if (booking) {
            booking.status = 'canceled';
        }
        
        applyFilters();
        if(!document.getElementById('history-view').classList.contains('hidden')){
            renderHistory();
        }
        closeCancelModal();
        showToast('Đã hủy đăng ký thành công!', 'danger');
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
    
    initIcons(); // Re-render the icon

    toast.classList.add('show');
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}


// Event Listeners Setup
document.addEventListener('DOMContentLoaded', () => {
    setDefaultDate();
    applyFilters();

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
    
    // Close modals when clicking outside
    window.addEventListener('click', (e) => {
        if (e.target.id === 'borrow-modal') closeModal();
        if (e.target.id === 'cancel-modal') closeCancelModal();
    });

    // Search / Filter button
    document.getElementById('btn-search').addEventListener('click', () => {
        applyFilters();
        showToast('Đã cập nhật danh sách phòng', 'success');
    });
});

// Expose functions globally for inline onclick handlers
window.openModal = openModal;
window.openCancelModal = openCancelModal;
