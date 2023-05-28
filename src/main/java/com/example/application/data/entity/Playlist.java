package com.example.application.data.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Playlist extends AbstractEntity {

    private String playlistName;

    @ManyToOne
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToMany
    @JoinTable(name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    private List<LikedSongs> songs = new ArrayList<>();

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public List<LikedSongs> getSongs() {
        return songs;
    }

    public void setSongs(List<LikedSongs> songs) {
        this.songs = songs;
    }
}
