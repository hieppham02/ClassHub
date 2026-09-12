let currentReturnId = null;
let currentDelegateId = null;

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
  fetch("/api/lich-su")
    .then((response) => {
      if (response.status === 401) {
        window.location.href = "/login";
        throw new Error("Chưa đăng nhập");
      }
      if (!response.ok) throw new Error("Lỗi server");
      return response.json();
    })
    .then((data) => {
      renderHistory(data);
    })
    .catch((error) => {
      document.getElementById("history-list").innerHTML =
        '<div class="empty-state">Lỗi tải dữ liệu. Vui lòng đăng nhập lại!</div>';
      console.error(error);
    });
}

function renderHistory(histories) {
  const container = document.getElementById("history-list");
  container.innerHTML = "";

  if (!histories || histories.length === 0) {
    container.innerHTML =
      '<div class="empty-state">Chưa có lịch sử mượn phòng nào.</div>';
    return;
  }

  histories.forEach((record) => {
    const item = document.createElement("div");
    item.className = "history-item";

    let statusBadge = "";
    if (record.trangThai === "DA_UY_QUYEN") {
      statusBadge = `<span class="badge" style="background: #EDE9FE; color: #7C3AED; padding: 4px 12px; border-radius: 20px; font-weight: 600;"><i data-lucide="user-check"></i> Đã ủy quyền</span>`;
    } else if (record.trangThai === "UY_QUYEN") {
      statusBadge = `<span class="badge" style="background: #E0E7FF; color: #4338CA; padding: 4px 12px; border-radius: 20px; font-weight: 600;"><i data-lucide="shield-check"></i> Được ủy quyền</span>`;
    } else if (record.trangThai === "DA_TRA_HO") {
      // HIỂN THỊ ĐÃ TRẢ HỘ
      statusBadge = `<span class="badge" style="background: #ECFDF5; color: #047857; border: 1px solid #A7F3D0; padding: 4px 12px; border-radius: 20px; font-weight: 600;"><i data-lucide="check-check"></i> Đã trả hộ</span>`;
    } else if (record.trangThai === "CHO_DUYET" || record.trangThai === "PENDING") {
      statusBadge = `<span class="badge" style="background: #FEF3C7; color: #D97706; padding: 4px 12px; border-radius: 20px; font-weight: 600;">Chờ duyệt</span>`;
    } else if (record.trangThai === "DA_DUYET" || record.trangThai === "ACTIVE") {
      statusBadge = `<span class="badge badge-active" style="background: #DBEAFE; color: #2563EB; padding: 4px 12px; border-radius: 20px; font-weight: 600;">Đang mượn</span>`;
    } else if (record.trangThai === "DA_TRA" || record.trangThai === "COMPLETED") {
      statusBadge = `<span class="badge badge-canceled" style="background: #DCFCE7; color: #16A34A; padding: 4px 12px; border-radius: 20px; font-weight: 600;">Đã trả</span>`;
    } else if (record.trangThai === "TU_CHOI") {
      statusBadge = `<span class="badge" style="background: #FEE2E2; color: #DC2626; padding: 4px 12px; border-radius: 20px; font-weight: 600;">Bị từ chối</span>`;
    } else {
      statusBadge = `<span class="badge" style="background: #E2E8F0; color: #4A5568; padding: 4px 12px; border-radius: 20px;">${record.trangThai}</span>`;
    }

    let actionBtn = "";
    if (record.trangThai === "ACTIVE" || record.trangThai === "DA_DUYET") {
      actionBtn = `
          <div style="display: flex; gap: 8px;">
              <button class="btn-primary btn-sm" onclick="openReturnModal(${record.id})">
                  <i data-lucide="check-circle"></i> Trả thiết bị
              </button>
              <button class="btn-sm" style="background: #F1F5F9; color: #334155; border: 1px solid #CBD5E1; border-radius: 6px; padding: 6px 12px; font-weight: 500; cursor: pointer;" onclick="openDelegateModal(${record.id})">
                  <i data-lucide="user-check"></i> Ủy quyền trả
              </button>
          </div>
      `;
    } else if (record.trangThai === "UY_QUYEN") {
      actionBtn = `
          <button class="btn-primary btn-sm" onclick="openReturnModal(${record.id})">
              <i data-lucide="check-circle"></i> Trả thiết bị (Trả hộ)
          </button>
      `;
    }

    const borrowDate = new Date(record.ngayMuon).toLocaleDateString("vi-VN");
    const createdTime = new Date(record.thoiGianTao).toLocaleString("vi-VN");
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
                <span><i data-lucide="info"></i> Đã mượn lúc: ${createdTime}</span>
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
  document.getElementById("return-modal").classList.add("active");
}

function closeReturnModal() {
  currentReturnId = null;
  document.getElementById("return-modal").classList.remove("active");
}

function confirmReturn() {
  if (currentReturnId) {
    fetch(`/api/bookings/${currentReturnId}/return`, {
      method: "POST",
    })
      .then((response) => {
        if (!response.ok) throw new Error("Lỗi");
        return response.json();
      })
      .then((data) => {
        closeReturnModal();
        showToast("Trả thiết bị thành công!", "success");
        fetchHistory();
      })
      .catch((error) => {
        closeReturnModal();
        showToast("Có lỗi xảy ra, không thể trả!", "danger");
      });
  }
}

function openDelegateModal(id) {
  currentDelegateId = id;
  let modal = document.getElementById("delegate-modal");
  if (!modal) {
    modal = document.createElement("div");
    modal.id = "delegate-modal";
    modal.className = "modal-overlay";
    modal.style.cssText =
      "position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 9999;";
    modal.innerHTML = `
        <div style="background: white; border-radius: 12px; padding: 24px; width: 400px; max-width: 90%; box-shadow: 0 10px 25px rgba(0,0,0,0.2);">
            <h4 style="margin-top: 0; margin-bottom: 8px; color: #1E293B;">Ủy quyền người khác trả</h4>
            <p style="color: #64748B; font-size: 0.85rem; margin-bottom: 16px;">Nhập Mã sinh viên của bạn mà bạn muốn nhờ trả thiết bị hộ:</p>
            <div style="margin-bottom: 16px;">
                <label style="display: block; font-weight: 600; font-size: 0.85rem; margin-bottom: 6px; color: #334155;">Mã Sinh Viên người nhận:</label>
                <input type="text" id="delegate-masv" placeholder="Ví dụ: 20231111 hoặc 20231206" style="width: 100%; padding: 8px 12px; border: 1px solid #CBD5E1; border-radius: 6px; box-sizing: border-box; font-size: 0.9rem;">
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px;">
                <button type="button" onclick="closeDelegateModal()" style="padding: 8px 16px; border: 1px solid #CBD5E1; background: #F8FAFC; border-radius: 6px; cursor: pointer;">Hủy</button>
                <button type="button" onclick="confirmDelegate()" style="padding: 8px 16px; border: none; background: #2563EB; color: white; border-radius: 6px; font-weight: 500; cursor: pointer;">Xác nhận ủy quyền</button>
            </div>
        </div>
    `;
    document.body.appendChild(modal);
  }
  document.getElementById("delegate-masv").value = "";
  modal.style.display = "flex";
}

function closeDelegateModal() {
  currentDelegateId = null;
  const modal = document.getElementById("delegate-modal");
  if (modal) modal.style.display = "none";
}

function confirmDelegate() {
  const maSv = document.getElementById("delegate-masv").value.trim();
  if (!maSv) {
    alert("Vui lòng nhập Mã sinh viên!");
    return;
  }

  fetch(
    `/api/bookings/${currentDelegateId}/delegate?maSv=${encodeURIComponent(maSv)}`,
    {
      method: "POST",
    }
  )
    .then((res) => res.json())
    .then((data) => {
      if (data.success) {
        closeDelegateModal();
        showToast(data.message, "success");
        fetchHistory();
      } else {
        alert(data.message);
      }
    })
    .catch((err) => {
      alert("Có lỗi xảy ra khi ủy quyền!");
    });
}

function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  const toastIconContainer = document.getElementById("toast-icon-container");

  if (!toast) return;

  document.getElementById("toast-message").textContent = message;

  if (type === "danger") {
    toast.style.backgroundColor = "var(--danger, #ef4444)";
    if (toastIconContainer)
      toastIconContainer.innerHTML = '<i data-lucide="alert-circle"></i>';
  } else {
    toast.style.backgroundColor = "var(--success, #10b981)";
    if (toastIconContainer)
      toastIconContainer.innerHTML = '<i data-lucide="check-circle"></i>';
  }

  initIcons();
  toast.classList.add("show");
  setTimeout(() => {
    toast.classList.remove("show");
  }, 3000);
}

document.addEventListener("DOMContentLoaded", () => {
  fetchHistory();

  const closeReturnBtn = document.getElementById("btn-close-return-modal");
  const cancelReturnBtn = document.getElementById("btn-cancel-return-modal");
  const confirmReturnBtn = document.getElementById("btn-confirm-return-modal");

  if (closeReturnBtn)
    closeReturnBtn.addEventListener("click", closeReturnModal);
  if (cancelReturnBtn)
    cancelReturnBtn.addEventListener("click", closeReturnModal);
  if (confirmReturnBtn)
    confirmReturnBtn.addEventListener("click", confirmReturn);

  window.addEventListener("click", (e) => {
    if (e.target.id === "return-modal") closeReturnModal();
  });
});

window.openReturnModal = openReturnModal;
window.openDelegateModal = openDelegateModal;
window.closeDelegateModal = closeDelegateModal;
window.confirmDelegate = confirmDelegate;