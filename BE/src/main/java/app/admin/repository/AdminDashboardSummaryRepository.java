package app.admin.repository;

import app.admin.dto.response.DashboardSummaryProjection;
import app.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDashboardSummaryRepository extends JpaRepository<User, Long> {

    @Query(value = """
        select
            (select count(*) from users u where u.user_role = 'CANDIDATE') as candidateTotal,
            (select count(*) from users u where u.user_role = 'RECRUITER') as recruiterTotal,
            (select count(*) from job_postings j) as jobPostTotal,
            (select count(*) from job_postings j where j.status = 'PUBLISHED') as jobPostPublished,
            (select count(*) from job_applications a) as applicationTotal
        """, nativeQuery = true)
    DashboardSummaryProjection getDashboardSummary();
}
