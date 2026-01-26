package app.admin.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import app.auth.model.User;

import java.util.List;

@Component
public interface AdminDashboardRecentActivityRepository extends Repository<User, Long> {

    @Query(value = """
        select 
            ja.id as application_id,
            u.full_name as candidate_name,
            c.name as company_name,
            ja.created_at as created_at
        from job_applications ja
        join users u on u.id = ja.candidate_id
        join job_postings jp on jp.id = ja.job_posting_id
        join companies c on c.id = jp.company_id
        order by ja.created_at desc
        limit :limit
    """, nativeQuery = true)
    List<Object[]> findRecentApplicationActivities(@Param("limit") int limit);
}
