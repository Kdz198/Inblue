package fpt.org.inblue.repository;

import fpt.org.inblue.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
