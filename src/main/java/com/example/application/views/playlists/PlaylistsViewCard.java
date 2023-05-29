package com.example.application.views.playlists;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.Playlist;
import com.example.application.data.entity.SongTable;
import com.example.application.data.entity.User;
import com.example.application.data.service.LikedSongsService;
import com.example.application.data.service.PlaylistService;
import com.example.application.data.service.UserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;
import java.util.Optional;

public class PlaylistsViewCard extends ListItem {

    private final PlaylistService playlistService;
    private final UserService userService;
    private final LikedSongsService likedSongsService;
    public PlaylistsViewCard(String text, String url,PlaylistService playlistService,UserService userService,LikedSongsService likedSongsService) {
        this.playlistService = playlistService;
        this.userService = userService;
        this.likedSongsService = likedSongsService;
        addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.AlignItems.START, LumoUtility.Padding.MEDIUM, LumoUtility.BorderRadius.LARGE);

        Div div = new Div();
        div.addClassNames(LumoUtility.Background.CONTRAST, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER,
                LumoUtility.JustifyContent.CENTER, LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Overflow.HIDDEN,
                LumoUtility.BorderRadius.MEDIUM, LumoUtility.Width.FULL);
        div.setHeight("160px");

        Image image = new Image();
        image.setWidth("100%");
        image.setSrc(url);
        image.setAlt(text);

        div.add(image);

        Span header = new Span();
        header.addClassNames(LumoUtility.FontSize.XLARGE, LumoUtility.FontWeight.SEMIBOLD);
        header.setText(text);

        Span subtitle = new Span();
        subtitle.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        add(div, header, subtitle);

        addClickListener(e -> showSongsDialog(text));
    }

    private void showSongsDialog(String playlistName) {
        Grid<SongTable> grid = new Grid<>(SongTable.class, false);
        grid.addColumn(SongTable::getSongName).setHeader("Song Name").setAutoWidth(true);
        grid.addColumn(SongTable::getArtistName).setHeader("Artist Name").setAutoWidth(true);
        grid.addColumn(SongTable::getAlbumName).setHeader("Album Name").setAutoWidth(true);

        List<SongTable> songs = playlistService.findSongsByPlaylistName(playlistName);
        grid.setItems(songs);

        Dialog dialog = new Dialog();
        dialog.setWidth("1100px");
        dialog.setHeight("900px");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        H2 playlistHeading = new H2(playlistName);
        layout.add(playlistHeading);

        grid.addComponentColumn(song -> {
            Button playButton = new Button(new Icon("lumo", "play"));
            playButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            // playButton.addClickListener(e -> handleButtonAction(playButton, song.getSongName()));
            return playButton;
        }).setHeader("Play");

        grid.addComponentColumn(song -> {
            Button addToLikedSongsButton = new Button(new Icon(VaadinIcon.HEART));
            addToLikedSongsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
             addToLikedSongsButton.addClickListener(e -> addToLikedSongs(song));
            return addToLikedSongsButton;
        }).setHeader("Add to Liked Songs");

        //delete from playlist button
        grid.addComponentColumn(song -> {
            Button addToLikedSongsButton = new Button(new Icon("lumo", "minus"));
            addToLikedSongsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
             addToLikedSongsButton.addClickListener(e -> deleteSong(playlistName,song,grid));
            return addToLikedSongsButton;
        }).setHeader("Remove");

        layout.add(grid);

        Button closeButton = new Button("Close");
        closeButton.addClickListener(event -> dialog.close());
        HorizontalLayout buttonContainer = new HorizontalLayout(closeButton);
        buttonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layout.add(buttonContainer);

        dialog.add(layout);
        dialog.open();
    }
    private void addToLikedSongs(SongTable song) {
        User currentUser = userService.getCurrentUser();
        LikedSongs likedSong = new LikedSongs();
        likedSong.setSongName(song.getSongName());
        likedSong.setArtistName(song.getArtistName());
        likedSong.setAlbumName(song.getAlbumName());
        likedSong.setGenre(song.getGenre());
        likedSong.setUser(currentUser);
        likedSongsService.update(likedSong);
        Notification.show("Song added to Liked Songs");
        }
        private void deleteSong(String playlist, SongTable song,Grid grid){
            Playlist playlist1 = playlistService.findPlaylistByName(playlist);
            playlistService.removeSongFromPlaylist(playlist1, song);
            List<SongTable> updatedSongs = playlistService.findSongsByPlaylistName(playlist);
            grid.setItems(updatedSongs);
            Notification.show("Song removed from playlist");
        }
}