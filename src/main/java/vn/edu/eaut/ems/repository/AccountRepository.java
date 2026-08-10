package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.eaut.ems.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
    // Để trống trơn thế này luôn! 
    // Spring Data JPA sẽ tự động cung cấp sẵn cho ông các hàm save(), findById(), existsById()...
}