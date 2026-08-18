let currentReturnId = null;

const shiftMap = {  
    1: "Ca 1 (07:00 - 09:30)",  
    2: "Ca 2 (09:30 - 12:00)",  
    3: "Ca 3 (13:00 - 15:30)",  
    4: "Ca 4 (15:30 - 18:00)",  
    5: "Ca 5 (18:00 - 20:30)"  
};  

function initIcons() {  
    if (window.lucide) {  
        lucide.createIcons();  
    }  
}  

function fetchHistory() {  
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
        
        const statusBadge = record.trangThai === 'ACTIVE' 
            ? `<span class="badge badge-active">Đang mượn</span>`
            : `<span class="badge badge-canceled" style="background: #E2E8F0; color: #4A5568;">Đã trả</span>`;
            
        let actionBtn = '';
        let otpSection = '';
        if (record.trangThai === 'ACTIVE') {
            actionBtn = `<button class="btn-primary btn-sm" onclick="openReturnModal(${record.id})">
                <i data-lucide="check-circle"></i> Trả thiết bị
            </button>`;

            otpSection = `
                <div style="margin-top: 10px; background: #E6FFFA; padding: 10px; border-radius: 6px; display: inline-flex; align-items: center; gap: 10px; border: 1px solid #B2F5EA;">
                    <span style="color: #234E52; font-weight: 600;">Mã mở cửa:</span>
                    <strong style="font-size: 1.2rem; letter-spacing: 2px; color: #319795;" id="otp-text-${record.id}">${record.otp}</strong>
                    <button onclick="refreshOtp(${record.id})" style="background: none; border: none; cursor: pointer; color: #319795; padding: 2px;" title="Tạo mã mới">
                        <i data-lucide="refresh-cw" style="width: 18px; height: 18px;"></i>
                    </button>
                </div>
            `;
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
                    ${otpSection}
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

function openReturnModal(id) {
    currentReturnId = id;
    document.getElementById('return-modal').classList.add('active');
}

function closeReturnModal() {
    currentReturnId = null;
    document.getElementById('return-modal').classList.remove('active');
}

function confirmReturn() {
    if (currentReturnId) {
        fetch(`/api/bookings/${currentReturnId}/return`, {
            method: 'POST'
        })
        .then(response => {
            if (!response.ok) throw new Error('Lỗi');
            return response.json();
        })
        .then(data => {
            closeReturnModal();
            showToast('Trả thiết bị thành công!', 'success'); 
            fetchHistory(); 
        })
        .catch(error => {
            closeReturnModal();
            showToast('Có lỗi xảy ra, không thể trả!', 'danger');
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

window.refreshOtp = function(id) {
    fetch(`/api/bookings/${id}/refresh-otp`, {
        method: 'POST'
    })
    .then(response => {
        if (!response.ok) throw new Error('Không thể làm mới mã');
        return response.json();
    })
    .then(data => {
        document.getElementById(`otp-text-${id}`).textContent = data.newOtp;
        showToast('Đã tạo mã OTP mới!', 'success');
    })
    .catch(error => {
        showToast('Lỗi: Không thể lấy mã mới!', 'danger');
    });
}

document.addEventListener('DOMContentLoaded', () => {  
    fetchHistory();  

    const closeReturnBtn = document.getElementById('btn-close-return-modal');
    const cancelReturnBtn = document.getElementById('btn-cancel-return-modal');
    const confirmReturnBtn = document.getElementById('btn-confirm-return-modal');

    if (closeReturnBtn) closeReturnBtn.addEventListener('click', closeReturnModal);
    if (cancelReturnBtn) cancelReturnBtn.addEventListener('click', closeReturnModal);
    if (confirmReturnBtn) confirmReturnBtn.addEventListener('click', confirmReturn);

    window.addEventListener('click', (e) => {
        if (e.target.id === 'return-modal') closeReturnModal();
    });
});

function sendLedCommand(state) {
  let url = "/api/led/" + state;

  fetch(url, {
    method: "POST",
  })
    .then((response) => response.text())
    .then((data) => {
      console.log("Server:", data);
    })
    .catch((error) => {
      console.error("Lỗi:", error);
    });
}

function sendOTP(){
    let url = "/api/otp";

  fetch(url, {
    method: "POST",
  })
    .then((response) => response.text())
    .then((data) => {
      console.log("Server:", data);
    })
    .catch((error) => {
      console.error("Lỗi:", error);
    });
}

document
  .querySelector(".btn-On")
  .addEventListener("click", () => sendLedCommand(1));
document
  .querySelector(".btn-Off")
  .addEventListener("click", () => sendLedCommand(0));
  document
  .querySelector(".btn-otp")
  .addEventListener("click", () => sendOTP());

window.openReturnModal = openReturnModal;