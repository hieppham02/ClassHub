package vn.edu.eaut.ems.entity;

import java.time.LocalDate;

public class BookingRequest {
    private String maPhong;
    private LocalDate ngayMuon;
    private Integer caMuon;

    // Getters & Setters
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public LocalDate getNgayMuon() { return ngayMuon; }
    public void setNgayMuon(LocalDate ngayMuon) { this.ngayMuon = ngayMuon; }
    public Integer getCaMuon() { return caMuon; }
    public void setCaMuon(Integer caMuon) { this.caMuon = caMuon; }
}