package fpt.org.inblue.repository;

import fpt.org.inblue.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {}
