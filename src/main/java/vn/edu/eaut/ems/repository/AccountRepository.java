package vn.edu.eaut.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.eaut.ems.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existsByEmail(String email);
}
