package vn.edu.eaut.ems.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RoomEquipmentId implements Serializable {

    @Column(name = "ma_phong")
    private String maPhong;

    @Column(name = "thiet_bi_id")
    private Integer thietBiId;

    public RoomEquipmentId() {}

    public RoomEquipmentId(String maPhong, Integer thietBiId) {
        this.maPhong = maPhong;
        this.thietBiId = thietBiId;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public Integer getThietBiId() { return thietBiId; }
    public void setThietBiId(Integer thietBiId) { this.thietBiId = thietBiId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomEquipmentId that)) return false;
        return Objects.equals(maPhong, that.maPhong) && Objects.equals(thietBiId, that.thietBiId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhong, thietBiId);
    }
}
