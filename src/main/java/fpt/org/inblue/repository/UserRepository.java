package fpt.org.inblue.repository;

import fpt.org.inblue.model.User;
import fpt.org.inblue.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, int id);

    int countUserByRole(Role role);
}
