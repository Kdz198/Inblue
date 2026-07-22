package fpt.org.inblue.repository;

import fpt.org.inblue.model.Company;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByJobDescriptionsId(Long jdId);
}
