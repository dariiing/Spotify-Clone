package com.example.application.data.service;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LikedSongsRepository extends JpaRepository<LikedSongs, Long>, JpaSpecificationExecutor<LikedSongs> {
    List<LikedSongs> findByUser(User user);
}

