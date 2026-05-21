package fpt.org.inblue.repository;

import fpt.org.inblue.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findAllByUserId(int userId);
}
