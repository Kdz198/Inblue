package fpt.org.inblue.repository;

import fpt.org.inblue.model.JobDescription;
import fpt.org.inblue.model.enums.JobDescriptionStatus;
import fpt.org.inblue.model.enums.TargetLevel;
import org.springframework.data.jpa.domain.Specification;

public class JobDescriptionSpecification {
    public static Specification<JobDescription> hasStatus(JobDescriptionStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<JobDescription> titleContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
        };
    }

    public static Specification<JobDescription> isNotDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("isDeleted"));
    }

    public static Specification<JobDescription> levelEquals(TargetLevel level) {
        return (root, query, criteriaBuilder) -> {
            if (level == null ) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("level"), level);
        };
    }

    public static Specification<JobDescription> hasSalaryGreaterThanOrEqual(Double salaryMin) {
        return (root, query, criteriaBuilder) -> {
            if (salaryMin == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("salaryMin"), salaryMin);
        };
    }

    public static Specification<JobDescription> hasSalaryLessThanOrEqual(Double salaryMax) {
        return (root, query, criteriaBuilder) -> {
            if (salaryMax == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("salaryMax"), salaryMax);
        };
    }
}
