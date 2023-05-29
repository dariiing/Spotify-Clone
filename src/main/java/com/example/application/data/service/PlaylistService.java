package com.example.application.data.service;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.Playlist;
import com.example.application.data.entity.SongTable;
import com.example.application.data.entity.User;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlaylistService {

    private final PlaylistRepository repository;
    private final songTableRepository songTableRepository;

    public PlaylistService(PlaylistRepository repository, songTableRepository songTableRepository) {
        this.repository = repository;
        this.songTableRepository = songTableRepository;
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

    @Transactional
    public void addSongToPlaylist(Playlist playlist, SongTable song) {
        Playlist playlistToUpdate = repository.findById(playlist.getId())
                .orElseThrow(() -> new IllegalArgumentException("Playlist not found"));

        Hibernate.initialize(playlistToUpdate.getSongs());
        List<SongTable> songs = playlistToUpdate.getSongs();
        songs.add(song);
        playlistToUpdate.setSongs(songs);

        repository.save(playlistToUpdate);
    }


}
