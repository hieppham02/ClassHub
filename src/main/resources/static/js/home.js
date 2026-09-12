let allBuildings = [];
let currentSelectedRoomId = null;
let currentBusyRooms = [];

function initIcons() {
  if (window.lucide) {
    lucide.createIcons();
  }
}

// 1. ĐỔ DỮ LIỆU TÒA NHÀ (MẶC ĐỊNH CHỌN TẤT CẢ TÒA NHÀ)
function populateBuildingFilter(buildings) {
  const select = document.getElementById("building-select");
  if (!select) return;
  select.innerHTML = '<option value="ALL" selected>Chọn tất cả tòa nhà</option>';

  buildings.forEach((building) => {
    select.innerHTML += `<option value="${building.maToaNha}">${building.tenToaNha}</option>`;
  });
}

// 2. LẤY TẦNG TỪ MÃ PHÒNG (VD: DTD101 -> Tầng 1)
function getRoomFloor(room) {
  const source = room.maPhong || room.tenPhong || "";
  const matchEnd = source.match(/(\d)\d{2}$/);
  if (matchEnd) return matchEnd[1];

  const matchFirst = source.match(/\d/);
  return matchFirst ? matchFirst[0] : null;
}

// 3. ĐỔ DỮ LIỆU TẦNG
function populateFloorFilter(buildings) {
  const select = document.getElementById("floor-select");
  if (!select) return;

  const floors = new Set();
  buildings.forEach((building) => {
    (building.rooms || []).forEach((room) => {
      const floor = getRoomFloor(room);
      if (floor) floors.add(floor);
    });
  });

  const sortedFloors = [...floors].sort((a, b) => Number(a) - Number(b));
  select.innerHTML = '<option value="ALL" selected>Tất cả các tầng</option>';
  sortedFloors.forEach((floor) => {
    select.innerHTML += `<option value="${floor}">Tầng ${floor}</option>`;
  });
}

// 4. LỌC DANH SÁCH THEO TÒA NHÀ
function getBuildingsForSelectedBuilding() {
  const select = document.getElementById("building-select");
  if (!select) return allBuildings;
  const selectedBuildingCode = select.value;
  if (!selectedBuildingCode || selectedBuildingCode === "ALL") return allBuildings;
  return allBuildings.filter((building) => building.maToaNha === selectedBuildingCode);
}

// 5. VẼ DANH SÁCH PHÒNG HỌC RA MÀN HÌNH
function renderRooms(buildingsToRender, selectedShiftText = "") {
  const roomList = document.getElementById("room-list");
  if (!roomList) return;
  roomList.innerHTML = "";

  const hasAnyRoom = buildingsToRender.some(
    (building) => building.rooms && building.rooms.length > 0,
  );

  if (!hasAnyRoom) {
    roomList.innerHTML = `
      <div class="empty-state" style="text-align: center; padding: 40px; color: #64748B;">
        <i data-lucide="search-x" style="width: 48px; height: 48px; margin-bottom: 12px; color: #94A3B8;"></i>
        <p>Không tìm thấy phòng học phù hợp với bộ lọc!</p>
      </div>`;
    initIcons();
    return;
  }

  buildingsToRender.forEach((building) => {
    if (!building.rooms || building.rooms.length === 0) return;

    const section = document.createElement("div");
    section.className = "building-section";

    section.innerHTML = `
        <h2 class="building-title" style="margin-bottom: 20px;">
            <i data-lucide="building"></i> ${building.tenToaNha} 
            ${selectedShiftText ? `<span style="font-size: 0.95rem; font-weight: 500; color: #2563EB; background: #EFF6FF; padding: 4px 12px; border-radius: 20px; border: 1px solid #BFDBFE; margin-left: 10px;">${selectedShiftText}</span>` : ''}
        </h2>
        <div class="room-grid"></div>
    `;

    const grid = section.querySelector(".room-grid");

    building.rooms.forEach((room) => {
      const cleanCode = (room.maPhong || "").replace("-", "").trim().toUpperCase();
      const isBorrowed = currentBusyRooms.includes(cleanCode);

      const card = document.createElement("div");
      card.className = "room-card" + (isBorrowed ? " borrowed-card" : "");

      const actionButton = isBorrowed
        ? `<button class="btn-secondary btn-action" disabled style="cursor: not-allowed; opacity: 0.7; background: #94A3B8;">
                    <i data-lucide="lock"></i> Đang mượn
                </button>`
        : `<button class="btn-primary btn-action" onclick="openModal('${room.maPhong}', '${room.tenPhong}')">
                    Đăng ký mượn
                </button>`;

      let equipmentHtml = "";
      if (room.equipments && room.equipments.length > 0) {
        equipmentHtml = room.equipments
          .map(
            (equipment) => `
                    <li class="equipment-item">
                        <i data-lucide="check-square"></i> ${equipment.soLuong} ${equipment.tenThietBi}
                    </li>
                `,
          )
          .join("");
      } else {
        equipmentHtml = `
            <li class="equipment-item"><i data-lucide="check-square"></i> 1 Điều khiển máy chiếu</li>
            <li class="equipment-item"><i data-lucide="check-square"></i> 1 Điều khiển điều hòa</li>
            <li class="equipment-item"><i data-lucide="check-square"></i> 2 Chìa khóa</li>
            <li class="equipment-item"><i data-lucide="check-square"></i> 1 Bộ loa mic</li>
        `;
      }

      card.innerHTML = `
            <div class="card-header">
                <div class="card-title">${room.tenPhong}</div>
                <div class="card-capacity">
                    <i data-lucide="users"></i> ${room.sucChua || 70}
                </div>
            </div>
            <div class="card-body">
                <div class="equipment-title">Túi đồ bao gồm:</div>
                <ul class="equipment-list">
                    ${equipmentHtml}
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

// 6. XỬ LÝ KHI BẤM NÚT TÌM KIẾM
function applyFilters() {
  const buildingSelect = document.getElementById("building-select");
  const floorSelect = document.getElementById("floor-select");
  const shiftSelect = document.getElementById("shift-select");
  const searchBtn = document.getElementById("btn-search");

  const selectedBuildingCode = buildingSelect ? buildingSelect.value : "ALL";
  const selectedFloor = floorSelect ? floorSelect.value : "ALL";
  const selectedShift = shiftSelect ? shiftSelect.value : "";

  if (searchBtn) {
    searchBtn.disabled = true;
    searchBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" style="width: 1rem; height: 1rem; margin-right: 6px;"></span> Đang tìm...';
  }

  const selectedShiftText = (shiftSelect && selectedShift) ? shiftSelect.options[shiftSelect.selectedIndex].text : "";

  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  const todayStr = `${year}-${month}-${day}`;

  // Kiểm tra phòng bận trong ca học đó
  const apiUrl = selectedShift 
    ? `/api/busy-rooms?caMuon=${encodeURIComponent(selectedShift)}&ngayMuon=${encodeURIComponent(todayStr)}`
    : `/api/busy-rooms?ngayMuon=${encodeURIComponent(todayStr)}`;

  fetch(apiUrl)
    .then((res) => res.json())
    .then((busyRooms) => {
      currentBusyRooms = busyRooms || [];

      let filtered = getBuildingsForSelectedBuilding();
      if (selectedFloor !== "ALL") {
        filtered = filtered
          .map((building) => ({
            ...building,
            rooms: (building.rooms || []).filter(
              (room) => getRoomFloor(room) === selectedFloor,
            ),
          }))
          .filter((building) => building.rooms.length > 0);
      }

      let totalRooms = 0;
      filtered.forEach((b) => { totalRooms += (b.rooms || []).length; });

      setTimeout(() => {
        if (searchBtn) {
          searchBtn.disabled = false;
          searchBtn.innerHTML = '<i data-lucide="search"></i> Tìm kiếm';
          initIcons();
        }

        renderRooms(filtered, selectedShiftText);

        if (totalRooms > 0) {
          showToast(`Tìm thấy ${totalRooms} phòng (${currentBusyRooms.length} phòng đang bận/chờ duyệt)!`, "success");
        } else {
          showToast("Không tìm thấy phòng phù hợp!", "danger");
        }
      }, 300);
    })
    .catch((err) => {
      console.error("Lỗi kiểm tra phòng:", err);
      if (searchBtn) {
        searchBtn.disabled = false;
        searchBtn.innerHTML = '<i data-lucide="search"></i> Tìm kiếm';
        initIcons();
      }
    });
}

// 7. MỞ MODAL ĐĂNG KÝ
function openModal(roomId, roomName) {
  // Chặn nếu sinh viên đang có phòng mượn chưa trả
  const hasActiveEl = document.getElementById("user-has-active-booking");
  if (hasActiveEl && hasActiveEl.value === "true") {
    showToast("Bạn đang có phòng mượn chưa trả! Vui lòng vào Lịch sử để trả phòng cũ trước.", "danger");
    return;
  }

  const shiftSelect = document.getElementById("shift-select");
  const shiftValue = shiftSelect ? shiftSelect.value : "";

  if (!shiftValue) {
    showToast("Vui lòng chọn Ca học ở bộ lọc trước khi đăng ký mượn!", "danger");
    if (shiftSelect) shiftSelect.focus();
    return;
  }

  currentSelectedRoomId = roomId;
  document.getElementById("modal-room-name").textContent = roomName;

  const now = new Date();
  const selectedShiftText = shiftSelect.options[shiftSelect.selectedIndex].text;

  document.getElementById("modal-date").textContent = now.toLocaleDateString("vi-VN");
  document.getElementById("modal-shift").textContent = selectedShiftText;

  document.getElementById("borrow-modal").classList.add("active");
}

function closeModal() {
  currentSelectedRoomId = null;
  const modal = document.getElementById("borrow-modal");
  if (modal) modal.classList.remove("active");
}

// 8. XÁC NHẬN MƯỢN PHÒNG
function confirmBorrow() {
  if (currentSelectedRoomId) {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    const selectedDate = `${year}-${month}-${day}`;

    const shiftSelect = document.getElementById("shift-select");
    const shiftValue = shiftSelect ? shiftSelect.value : "1";

    const requestData = {
      maPhong: currentSelectedRoomId,
      ngayMuon: selectedDate,
      caMuon: parseInt(shiftValue) || 1,
    };

    fetch("/api/bookings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(requestData),
    })
      .then(async (response) => {
        const data = await response.json();
        if (response.status === 401) {
          window.location.href = "/login";
          throw new Error("Chưa đăng nhập");
        }
        if (!response.ok) {
          throw new Error(data.message || "Có lỗi xảy ra");
        }
        return data;
      })
      .then(() => {
        closeModal();
        showToast("Đăng ký mượn phòng thành công!", "success");

        setTimeout(() => {
          window.location.href = "/lich-su";
        }, 500);
      })
      .catch((error) => {
        closeModal();
        showToast(error.message, "danger");
        console.error(error);
      });
  }
}

// 9. THÔNG BÁO TOAST
function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  const toastIconContainer = document.getElementById("toast-icon-container");

  if (!toast) return;

  document.getElementById("toast-message").textContent = message;

  if (type === "danger") {
    toast.style.backgroundColor = "var(--danger, #ef4444)";
    if (toastIconContainer) toastIconContainer.innerHTML = '<i data-lucide="alert-circle"></i>';
  } else {
    toast.style.backgroundColor = "var(--success, #10b981)";
    if (toastIconContainer) toastIconContainer.innerHTML = '<i data-lucide="check-circle"></i>';
  }

  initIcons();
  toast.classList.add("show");
  setTimeout(() => {
    toast.classList.remove("show");
  }, 3000);
}

// =========================================================================
// KHỞI TẠO KHI TẢI TRANG (HIỆN NGAY TOÀN BỘ PHÒNG HỌC BAN ĐẦU)
// =========================================================================
document.addEventListener("DOMContentLoaded", () => {
  // TẢI TOÀN BỘ DANH SÁCH PHÒNG VÀ VẼ RA MÀN HÌNH NGAY LẬP TỨC
  fetch("/api/buildings")
    .then((response) => response.json())
    .then((data) => {
      allBuildings = data;
      populateBuildingFilter(allBuildings);
      populateFloorFilter(allBuildings);
      renderRooms(allBuildings); // <-- HIỆN LẠI DANH SÁCH CÁC PHÒNG NGAY KHI VỪA MỞ TRANG
    })
    .catch((error) => {
      console.error("Lỗi lấy dữ liệu:", error);
      const roomList = document.getElementById("room-list");
      if (roomList) roomList.innerHTML = '<div class="empty-state">Lỗi kết nối máy chủ!</div>';
    });

  // GẮN SỰ KIỆN NÚT TÌM KIẾM
  const searchBtn = document.getElementById("btn-search");
  if (searchBtn) {
    searchBtn.addEventListener("click", (e) => {
      e.preventDefault();
      applyFilters();
    });
  }

  // Tự cập nhật tầng khi đổi tòa nhà
  const buildingSelect = document.getElementById("building-select");
  if (buildingSelect) {
    buildingSelect.addEventListener("change", () => {
      populateFloorFilter(getBuildingsForSelectedBuilding());
    });
  }

  // Các nút Modal
  const closeBtn = document.getElementById("btn-close-modal");
  const cancelBtn = document.getElementById("btn-cancel-modal");
  const confirmBtn = document.getElementById("btn-confirm-modal");

  if (closeBtn) closeBtn.addEventListener("click", closeModal);
  if (cancelBtn) cancelBtn.addEventListener("click", closeModal);
  if (confirmBtn) confirmBtn.addEventListener("click", confirmBorrow);

  window.addEventListener("click", (e) => {
    if (e.target.id === "borrow-modal") closeModal();
  });
});

window.openModal = openModal;
window.applyFilters = applyFilters;