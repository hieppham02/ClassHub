package vn.edu.eaut.ems.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "thiet_bi", uniqueConstraints = @UniqueConstraint(name = "uq_thiet_bi_ten", columnNames = "ten_thiet_bi"))
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_thiet_bi", nullable = false, length = 100)
    private String tenThietBi;

    public Equipment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTenThietBi() { return tenThietBi; }
    public void setTenThietBi(String tenThietBi) { this.tenThietBi = tenThietBi; }
}
