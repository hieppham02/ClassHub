package vn.edu.eaut.ems.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "phong_hoc")
public class Room {

    @Id
    @Column(name = "ma_phong", length = 50)
    private String maPhong; 

    @Column(name = "ten_phong", nullable = false)
    private String tenPhong; 

    @Column(name = "suc_chua")
    private Integer sucChua;

    @Column(name = "trang_thai")
    private String trangThai; 

    public Room() {
    }

    public Room(String maPhong, String tenPhong, Integer sucChua, String trangThai) {
        this.maPhong = maPhong;
        this.tenPhong = tenPhong;
        this.sucChua = sucChua;
        this.trangThai = trangThai;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        this.tenPhong = tenPhong;
    }

    public Integer getSucChua() {
        return sucChua;
    }

    public void setSucChua(Integer sucChua) {
        this.sucChua = sucChua;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}