package com.englishlearning.repository;

import com.englishlearning.domain.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUserId(Long userId);

    void deleteByEndpoint(String endpoint);

    boolean existsByUserIdAndEndpoint(Long userId, String endpoint);
}
