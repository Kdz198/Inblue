package fpt.org.inblue.repository;

import fpt.org.inblue.model.Kiosk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KioskRepository extends JpaRepository<Kiosk, Long> {
    List<Kiosk> findAllByIsActiveTrue();
}
