package fpt.org.inblue.repository;

import fpt.org.inblue.model.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long>, JpaSpecificationExecutor<JobDescription> {

}



