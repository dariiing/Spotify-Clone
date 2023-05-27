package com.example.application.data.service;

import com.example.application.data.entity.Recommendations;
import com.example.application.views.recommendations.RecommendationsView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecommendationService {

    private final RecommendationRepository repository;

    public RecommendationService(RecommendationRepository repository) {
        this.repository = repository;
    }

    public Optional<Recommendations> get(Long id) {
        return repository.findById(id);
    }

    public Recommendations save(Recommendations entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Recommendations> list(Pageable pageable, RecommendationsView.Filters filters) {
        return repository.findAll(pageable);
    }


    public int count() {
        return (int) repository.count();
    }

    public List<Recommendations> findAll() {
        return repository.findAll();
    }
}
