package vn.edu.eaut.ems;

import java.util.TimeZone;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import vn.edu.eaut.ems.entity.Account;
import vn.edu.eaut.ems.repository.AccountRepository;

@SpringBootApplication
public class EmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmsApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	// =========================================================================
	// TỰ ĐỘNG TẠO / CẬP NHẬT 2 TÀI KHOẢN SINH VIÊN VÀO MYSQL
	// =========================================================================
	@Bean
	public CommandLineRunner initTestData(AccountRepository accountRepository) {
		return args -> {
			// 1. SINH VIÊN 1: HOÀNG VĂN HÒA
			String maSv1 = "20231049";
			Account acc1 = accountRepository.findById(maSv1).orElse(new Account());
			acc1.setMaSv(maSv1);
			acc1.setHoTen("Hoàng Văn Hòa");
			acc1.setEmail(maSv1 + "@eaut.edu.vn");
			acc1.setMatKhau("112233");
			acc1.setVaiTro("SINHVIEN");       // Quyền Sinh viên
			acc1.setTenLop("DCCNTT.14.3");
			acc1.setSdt("0988112233");
			accountRepository.save(acc1);

			// 2. SINH VIÊN 2: 20231111 (ĐÃ CHUYỂN THÀNH SINH VIÊN)
			String maSv2 = "20231111";
			Account acc2 = accountRepository.findById(maSv2).orElse(new Account());
			acc2.setMaSv(maSv2);
			acc2.setHoTen("Sinh Viên 20231111");
			acc2.setEmail(maSv2 + "@eaut.edu.vn");
			acc2.setMatKhau("admintest");      // Mật khẩu
			acc2.setVaiTro("SINHVIEN");       // <--- ĐÃ ĐỔI THÀNH QUYỀN SINH VIÊN (Không phải Admin)
			acc2.setTenLop("DCCNTT.14.3");
			acc2.setSdt("0911112222");
			accountRepository.save(acc2);

			System.out.println(">>> ĐÃ CẬP NHẬT TÀI KHOẢN 20231111 THÀNH: SINH VIÊN | Mật khẩu: admintest");
		};
	}
}