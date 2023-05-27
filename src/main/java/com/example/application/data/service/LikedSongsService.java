package com.example.application.data.service;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LikedSongsService {

    private final LikedSongsRepository repository;

    public LikedSongsService(LikedSongsRepository repository) {
        this.repository = repository;
    }

    public Optional<LikedSongs> get(Long id) {
        return repository.findById(id);
    }

    public LikedSongs update(LikedSongs entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<LikedSongs> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<LikedSongs> list(Pageable pageable, Specification<LikedSongs> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

    public List<LikedSongs> findByUser(User user) {
        return repository.findByUser(user);
    }
}
