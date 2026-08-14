package vn.edu.eaut.ems.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "thiet_bi")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_thiet_bi")
    private String tenThietBi;

    @Column(name = "so_luong")
    private Integer soLuong;

    @ManyToOne
    @JoinColumn(name = "ma_phong")
    @JsonIgnore
    private Room room;

    public Equipment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTenThietBi() { return tenThietBi; }
    public void setTenThietBi(String tenThietBi) { this.tenThietBi = tenThietBi; }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
}