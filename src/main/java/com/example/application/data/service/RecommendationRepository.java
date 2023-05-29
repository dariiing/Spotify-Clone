package com.example.application.data.service;

import com.example.application.data.entity.Recommendations;
import com.example.application.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface RecommendationRepository
        extends JpaRepository<Recommendations, Long>, JpaSpecificationExecutor<Recommendations> {
    List<Recommendations> findByUser(User user);
}
