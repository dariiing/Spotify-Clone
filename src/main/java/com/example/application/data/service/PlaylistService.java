package com.example.application.data.service;

import com.example.application.data.entity.Playlist;
import com.example.application.data.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    private final PlaylistRepository repository;

    public PlaylistService(PlaylistRepository repository) {
        this.repository = repository;
    }

    public Optional<Playlist> get(Long id) {
        return repository.findById(id);
    }

    public Playlist save(Playlist playlist) {
        return repository.save(playlist);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Playlist> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<Playlist> findByUser(User user) {
        return repository.findByUser(user);
    }

    public int count() {
        return (int) repository.count();
    }

}
