let allBuildings = [];
let currentSelectedRoomId = null;

function initIcons() {
  if (window.lucide) {
    lucide.createIcons();
  }
}

function populateBuildingFilter(buildings) {
  const select = document.getElementById("building-select");
  select.innerHTML = '<option value="ALL">Chọn tất cả</option>';

  buildings.forEach((building) => {
    select.innerHTML += `<option value="${building.maToaNha}">${building.tenToaNha}</option>`;
  });
}

function getRoomFloor(room) {
  const source = room.maPhong || room.tenPhong || "";
  const match = source.match(/\d/);
  return match ? match[0] : null;
}

function populateFloorFilter(buildings) {
  const select = document.getElementById("floor-select");
  const selectedFloor = select.value;
  const floors = new Set();

  buildings.forEach((building) => {
    (building.rooms || []).forEach((room) => {
      const floor = getRoomFloor(room);
      if (floor) floors.add(floor);
    });
  });

  const sortedFloors = [...floors].sort((a, b) => Number(a) - Number(b));
  select.innerHTML = '<option value="ALL">Chọn tất cả</option>';
  sortedFloors.forEach((floor) => {
    select.innerHTML += `<option value="${floor}">Tầng ${floor}</option>`;
  });

  select.value = sortedFloors.includes(selectedFloor) ? selectedFloor : "ALL";
}

function getBuildingsForSelectedBuilding() {
  const selectedBuildingCode = document.getElementById("building-select").value;
  if (selectedBuildingCode === "ALL") return allBuildings;
  return allBuildings.filter((building) => building.maToaNha === selectedBuildingCode);
}

function renderRooms(buildingsToRender) {
  const roomList = document.getElementById("room-list");
  roomList.innerHTML = "";

  const hasAnyRoom = buildingsToRender.some(
    (building) => building.rooms && building.rooms.length > 0,
  );

  if (!hasAnyRoom) {
    roomList.innerHTML = '<p class="no-data empty-state">Không tìm thấy phòng phù hợp.</p>';
    return;
  }

  buildingsToRender.forEach((building) => {
    if (!building.rooms || building.rooms.length === 0) return;

    const section = document.createElement("div");
    section.className = "building-section";

    section.innerHTML = `
            <h2 class="building-title"><i data-lucide="building"></i> ${building.tenToaNha}</h2>
            <div class="room-grid"></div>
        `;

    const grid = section.querySelector(".room-grid");

    building.rooms.forEach((room) => {
      const isBorrowed = room.trangThai !== "0";
      const card = document.createElement("div");
      card.className = "room-card" + (isBorrowed ? " borrowed-card" : "");

      const actionButton = isBorrowed
        ? `<button class="btn-secondary btn-action" disabled style="cursor: not-allowed; opacity: 0.7;">
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
        equipmentHtml = '<li class="equipment-item" style="color: var(--danger);">Phòng trống (Không có đồ)</li>';
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

function applyFilters() {
  const selectedFloor = document.getElementById("floor-select").value;
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

  renderRooms(filtered);
}

function openModal(roomId, roomName) {
  const shiftSelect = document.getElementById("shift-select");
  const shiftValue = shiftSelect.value;

  if (!shiftValue) {
    showToast("Vui lòng chọn ca học trước khi đăng ký mượn!", "danger");
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
  document.getElementById("borrow-modal").classList.remove("active");
}

function confirmBorrow() {
  if (currentSelectedRoomId) {
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    const selectedDate = `${year}-${month}-${day}`;

    const shiftValue = document.getElementById("shift-select").value;

    const requestData = {
      maPhong: currentSelectedRoomId,
      ngayMuon: selectedDate,
      caMuon: parseInt(shiftValue),
    };

    fetch("/api/bookings", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(requestData),
    })
      .then((response) => {
        if (response.status === 401) {
          window.location.href = "/login";
          throw new Error("Chưa đăng nhập");
        }
        if (!response.ok) throw new Error("Có lỗi xảy ra");
        return response.json();
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
        showToast(
          error.message === "Chưa đăng nhập"
            ? "Vui lòng đăng nhập"
            : "Lỗi không thể mượn phòng",
          "danger",
        );
        console.error(error);
      });
  }
}

function showToast(message, type = "success") {
  const toast = document.getElementById("toast");
  const toastIconContainer = document.getElementById("toast-icon-container");

  document.getElementById("toast-message").textContent = message;

  if (type === "danger") {
    toast.style.backgroundColor = "var(--danger)";
    toastIconContainer.innerHTML = '<i data-lucide="alert-circle"></i>';
  } else {
    toast.style.backgroundColor = "var(--success)";
    toastIconContainer.innerHTML = '<i data-lucide="check-circle"></i>';
  }

  initIcons();
  toast.classList.add("show");
  setTimeout(() => {
    toast.classList.remove("show");
  }, 3000);
}

document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("btn-close-modal").addEventListener("click", closeModal);
  document.getElementById("btn-cancel-modal").addEventListener("click", closeModal);
  document.getElementById("btn-confirm-modal").addEventListener("click", confirmBorrow);

  window.addEventListener("click", (e) => {
    if (e.target.id === "borrow-modal") closeModal();
  });

  document.getElementById("btn-search").addEventListener("click", () => {
    applyFilters();
    showToast("Đã cập nhật danh sách phòng", "success");
  });

  document.getElementById("building-select").addEventListener("change", () => {
    populateFloorFilter(getBuildingsForSelectedBuilding());
    applyFilters();
  });

  document.getElementById("floor-select").addEventListener("change", applyFilters);

  fetch("/api/buildings")
    .then((response) => response.json())
    .then((data) => {
      allBuildings = data;
      populateBuildingFilter(allBuildings);
      populateFloorFilter(allBuildings);
      renderRooms(allBuildings);
    })
    .catch((error) => console.error("Lỗi lấy dữ liệu từ API:", error));
});

window.openModal = openModal;
