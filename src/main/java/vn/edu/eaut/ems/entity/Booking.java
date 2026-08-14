package vn.edu.eaut.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_muon")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ma_sv")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "ma_phong")
    private Room room;

    @Column(name = "ngay_muon")
    private LocalDate ngayMuon;

    @Column(name = "ca_muon")
    private Integer caMuon;

    @Column(name = "trang_thai")
    private String trangThai; // ACTIVE, CANCELED, COMPLETED

    @Column(name = "thoi_gian_tao", insertable = false, updatable = false)
    private LocalDateTime thoiGianTao;

    public Booking() {}

    public Booking(Integer id, Account account, Room room, LocalDate ngayMuon, Integer caMuon, String trangThai,
            LocalDateTime thoiGianTao) {
        this.id = id;
        this.account = account;
        this.room = room;
        this.ngayMuon = ngayMuon;
        this.caMuon = caMuon;
        this.trangThai = trangThai;
        this.thoiGianTao = thoiGianTao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getNgayMuon() {
        return ngayMuon;
    }

    public void setNgayMuon(LocalDate ngayMuon) {
        this.ngayMuon = ngayMuon;
    }

    public Integer getCaMuon() {
        return caMuon;
    }

    public void setCaMuon(Integer caMuon) {
        this.caMuon = caMuon;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}