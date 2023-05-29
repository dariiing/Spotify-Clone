package com.example.application.data.service;

import com.example.application.data.entity.Playlist;
import com.example.application.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUser(User user);
    Playlist findByPlaylistName(String playlistName);


}

