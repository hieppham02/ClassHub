package vn.edu.eaut.ems.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "phong_hoc")
public class Room {

    @Id
    @Column(name = "ma_phong")
    private String maPhong;

    @Column(name = "ten_phong")
    private String tenPhong;

    @Column(name = "suc_chua")
    private Integer sucChua;

    @Column(name = "trang_thai")
    private String trangThai;

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    private Set<Equipment> equipments;

    @ManyToOne // N-1
    @JoinColumn(name = "ma_toa_nha") 
    @JsonIgnore
    private Building building;

    public Room(){}

    public Room(String maPhong, String tenPhong, Integer sucChua, String trangThai, Building building) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.sucChua = sucChua;
        this.trangThai = trangThai;
        this.building = building;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    public Integer getSucChua() { return sucChua; }
    public void setSucChua(Integer sucChua) { this.sucChua = sucChua; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Building getBuilding() { return building; }
    public void setBuilding(Building building) { this.building = building; }

    public Set<Equipment> getEquipments() { return equipments; }
    public void setEquipments(Set<Equipment> equipments) { this.equipments = equipments; }
}