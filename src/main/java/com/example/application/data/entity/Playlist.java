package com.example.application.data.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Playlist extends AbstractEntity {

    private String playlistName;

    @ManyToOne
    private User user;

    @ManyToMany
    @JoinTable(name = "playlist_songs",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    private List<SongTable> songs = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public List<SongTable> getSongs() {
        return songs;
    }

    public void setSongs(List<SongTable> songs) {
        this.songs = songs;
    }
}
