package vn.edu.eaut.ems.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tai_khoan")
public class Account {
    @Id
    @Column(name = "ma_sv", length = 50)
    private String maSv;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "sdt", length = 15)
    private String sdt;

    @Column(name = "ten_lop", length = 50)
    private String tenLop;

    @Column(name = "vai_tro", nullable = false, length = 20)
    private String vaiTro;

    @Column(name = "mat_khau", nullable = false, length = 100)
    private String matKhau;

    public Account() {
    }

    public Account(String maSv, String matKhau, String hoTen, String email, String sdt) {
        this.maSv = maSv;
        this.matKhau = matKhau;
        this.hoTen = hoTen;
        this.email = email;
        this.sdt = sdt;
    }

    public String getMaSv() {
        return maSv;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public String getHoTen() {
        return hoTen;
    }

    public String getEmail() {
        return email;
    }

    public String getSdt() {
        return sdt;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setMaSv(String maSv) {
        this.maSv = maSv;
    }
}
