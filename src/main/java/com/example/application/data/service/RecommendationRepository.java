package com.example.application.data.service;

import com.example.application.data.entity.Recommendations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RecommendationRepository
        extends JpaRepository<Recommendations, Long>, JpaSpecificationExecutor<Recommendations> {

}
