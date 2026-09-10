package vn.edu.eaut.ems.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "phong_hoc_thiet_bi")
public class RoomEquipment {

    @EmbeddedId
    @JsonIgnore
    private RoomEquipmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maPhong")
    @JoinColumn(name = "ma_phong")
    @JsonIgnore
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("thietBiId")
    @JoinColumn(name = "thiet_bi_id")
    @JsonIgnore
    private Equipment equipment;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    public RoomEquipment() {}

    public RoomEquipmentId getId() { return id; }
    public void setId(RoomEquipmentId id) { this.id = id; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public String getTenThietBi() {
        return equipment != null ? equipment.getTenThietBi() : null;
    }
}
