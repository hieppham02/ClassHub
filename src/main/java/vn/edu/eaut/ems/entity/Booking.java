package vn.edu.eaut.ems.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phieu_muon") // Đúng tên bảng trong SQL
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // INT trong SQL

    // Khóa ngoại nối với bảng tai_khoan qua ma_sv
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_sv", nullable = false)
    private Account account;

    // Khóa ngoại nối với bảng phong_hoc qua ma_phong
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_phong", nullable = false)
    private Room room;

    @Column(name = "ngay_muon", nullable = false)
    private LocalDate ngayMuon;

    @Column(name = "ca_muon", nullable = false)
    private Integer caMuon; // INT trong SQL

    @Column(name = "trang_thai", length = 20)
    private String trangThai; // CHO_DUYET, DA_DUYET, DA_TRA, TU_CHOI

    @CreationTimestamp
    @Column(name = "thoi_gian_tao", updatable = false)
    private LocalDateTime thoiGianTao;

    public Booking() {}

    public Booking(Account account, Room room, LocalDate ngayMuon, Integer caMuon, String trangThai) {
        this.account = account;
        this.room = room;
        this.ngayMuon = ngayMuon;
        this.caMuon = caMuon;
        this.trangThai = trangThai;
    }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public LocalDate getNgayMuon() { return ngayMuon; }
    public void setNgayMuon(LocalDate ngayMuon) { this.ngayMuon = ngayMuon; }
    public Integer getCaMuon() { return caMuon; }
    public void setCaMuon(Integer caMuon) { this.caMuon = caMuon; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }
}