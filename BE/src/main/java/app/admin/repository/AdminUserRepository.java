package app.admin.repository;

import app.admin.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface AdminUserRepository extends JpaRepository<User, Long> {

    Page<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String name, String email, Pageable pageable
    );
}

