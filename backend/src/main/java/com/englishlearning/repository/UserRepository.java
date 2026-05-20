package com.englishlearning.repository;

import com.englishlearning.domain.enums.Role;
import com.englishlearning.domain.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    @Query("SELECT u, s FROM User u LEFT JOIN UserStreak s ON u.id = s.userId ORDER BY s.totalXp DESC NULLS LAST, u.displayName ASC")
    List<Object[]> findLeaderboard(Pageable pageable);

    @Query("SELECT u, s FROM User u LEFT JOIN UserStreak s ON u.id = s.userId ORDER BY u.createdAt DESC")
    List<Object[]> findAllWithStats();

    @Query("SELECT u.id FROM User u")
    List<Long> findAllIds();
}
