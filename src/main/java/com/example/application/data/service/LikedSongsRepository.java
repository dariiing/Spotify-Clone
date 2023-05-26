package com.example.application.data.service;

import com.example.application.data.entity.LikedSongs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LikedSongsRepository extends JpaRepository<LikedSongs, Long>, JpaSpecificationExecutor<LikedSongs> {

}

