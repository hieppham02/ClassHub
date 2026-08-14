package vn.edu.eaut.ems.entity;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "toa_nha")
public class Building {
    
    @Id
    @Column(name = "ma_toa_nha")
    private String maToaNha;
    
    @Column(name = "ten_toa_nha")
    private String tenToaNha;

    @OneToMany(mappedBy = "building", fetch = FetchType.EAGER) // 1-N
    private List<Room> rooms;

    public Building() {}

    // Getters & Setters
    public String getMaToaNha() { return maToaNha; }
    public void setMaToaNha(String maToaNha) { this.maToaNha = maToaNha; }
    
    public String getTenToaNha() { return tenToaNha; }
    public void setTenToaNha(String tenToaNha) { this.tenToaNha = tenToaNha; }
    
    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
}