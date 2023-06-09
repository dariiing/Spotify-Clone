package com.example.application.views.home;

import com.example.application.data.entity.*;
import com.example.application.data.service.LikedSongsService;
import com.example.application.data.service.PlaylistService;
import com.example.application.data.service.SongTableService;
import com.example.application.data.service.UserService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.sound.sampled.*;

@PageTitle("Home")
@Route(value = "home", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class HomeView extends Div {

    private Grid<SongTable> grid;

    private Clip currentClip;
    private String currentPath;
    private final Filters filters;
    private final SongTableService songTableService;

    private final LikedSongsService likedSongsService;

    private final PlaylistService playlistService;
    private final UserService userService;


    public HomeView(SongTableService songTableService, LikedSongsService likedSongsService,PlaylistService playlistService,UserService userService) {
        this.songTableService = songTableService;
        this.likedSongsService = likedSongsService;
        this.currentClip = null;
        this.currentPath = "";
        this.playlistService = playlistService;
        this.userService = userService;

        setSizeFull();
        addClassNames("home-view");

        filters = new Filters(() -> refreshGrid());
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        // Mobile version
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filters");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<SongTable> {

        private final TextField songName = new TextField("Song/Artist Name");

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            songName.setPlaceholder("Song/Artist Name");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                songName.clear();
                onSearch.run();
            });
            Button searchBtn = new Button("Search");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Div actions = new Div(resetBtn, searchBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");

            add(songName, actions);
        }


        @Override
        public Predicate toPredicate(Root<SongTable> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            if (!songName.isEmpty()) {
                String lowerCaseFilter = songName.getValue().toLowerCase();
                Predicate songNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("songName")),
                        lowerCaseFilter + "%");
                Predicate artistNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("artistName")),
                        lowerCaseFilter + "%");
                predicates.add(criteriaBuilder.or(songNameMatch,artistNameMatch));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }
    }

    private Component createGrid() {
        grid = new Grid<>(SongTable.class, false);
        grid.addColumn("songName").setAutoWidth(true);
        grid.addColumn("artistName").setAutoWidth(true);
        grid.addColumn("albumName").setAutoWidth(true);


        grid.setItems(query -> songTableService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        //play song
        grid.addComponentColumn(song -> {
            Button playButton = new Button(new Icon("lumo", "play"));
            playButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            playButton.addClickListener(e -> handleButtonAction(playButton, song.getSongName()));
            return playButton;
        }).setHeader("Play");

        grid.addComponentColumn(person -> {
            Button addToLikedSongsButton = new Button(new Icon(VaadinIcon.HEART));
            addToLikedSongsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addToLikedSongsButton.addClickListener(e ->
                    addToLikedSongs(person)
            );
            return addToLikedSongsButton;
        }).setHeader("Add to Liked Songs");


        //add to playlist
        grid.addComponentColumn(person -> {
            Button addToPlaylistButton = new Button(new Icon("lumo", "plus"));
            addToPlaylistButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addToPlaylistButton.addClickListener(e -> openAddToPlaylistDialog(person));
            return addToPlaylistButton;
        }).setHeader("Add to Playlist");

        return grid;
    }

    private void openAddToPlaylistDialog(SongTable song) {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(true);

        H2 titleLabel = new H2("Add to Playlist");
        MultiSelectListBox<Playlist> playlistListBox = new MultiSelectListBox<>();

        User currentUser = userService.getCurrentUser();
        System.out.println(playlistService.findByUser(currentUser));
        List<Playlist> availablePlaylists = playlistService.findByUser(currentUser);
        playlistListBox.setItems(availablePlaylists);
        playlistListBox.setItemLabelGenerator(Playlist::getPlaylistName);

        Button addButton = new Button("Add");
        addButton.addClickListener(e -> {
            Set<Playlist> selectedPlaylists = playlistListBox.getSelectedItems();
            for (Playlist playlist : selectedPlaylists) {
                try {
                    playlistService.addSongToPlaylist(playlist, song);
                    Notification.show("Song added to playlists");
                } catch (Exception exception) {
                    Notification.show("Already added in this playlist: " + playlist.getPlaylistName(), 2000, Notification.Position.MIDDLE);
                }
            }
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> dialog.close());

        Button deleteButton = new Button("Delete Playlist");
        deleteButton.addClickListener(e -> {
            Set<Playlist> selectedPlaylists = playlistListBox.getSelectedItems();
            for (Playlist playlist : selectedPlaylists) {
                try {
                    playlistService.removePlaylist(playlist);
                    availablePlaylists.remove(playlist);
                    playlistListBox.setItems(availablePlaylists);
                    Notification.show("Playlist deleted: " + playlist.getPlaylistName());
                } catch (Exception exception) {
                    Notification.show("Failed to delete playlist: " + playlist.getPlaylistName(), 2000, Notification.Position.MIDDLE);
                }
            }
            dialog.close();
        });

        HorizontalLayout buttonLayout = new HorizontalLayout(addButton, cancelButton, deleteButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        VerticalLayout contentLayout = new VerticalLayout(titleLabel, playlistListBox, buttonLayout);
        contentLayout.setSizeFull();
        contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        dialog.add(contentLayout);
        dialog.open();
    }



    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void handleButtonAction(Button button, String songFilePath) {
        String selectedPath = songFilePath; // name of the song
        String songPath = "C:\\Users\\daria\\OneDrive\\Desktop\\spoticlone\\src\\main\\resources" + "\\" + songFilePath + ".wav";
        Clip newClip = loadClip(songPath);
        if (newClip == null) {
            Notification.show("Song not downloaded");
            return;
        }
        if (currentClip == null) {
            if (newClip != null) {
                play(newClip);
                currentClip = newClip;
                this.currentPath = selectedPath;
                button.setIcon(VaadinIcon.PAUSE.create());
            }
        } else {
            if (!selectedPath.equalsIgnoreCase(currentPath)) {
                stop(currentClip);
                play(newClip);
                currentClip = newClip;
                this.currentPath = selectedPath;
                button.setIcon(VaadinIcon.PAUSE.create());
            } else {
                if (currentClip.isRunning()) {
                    pause(currentClip);
                    button.setIcon(new Icon("lumo", "play"));
                } else {
                    resume(currentClip);
                    button.setIcon(VaadinIcon.PAUSE.create());
                }
            }
        }
    }

    private Clip loadClip(String songFilePath) {
        try {
            File songFile = new File(songFilePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(songFile);

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            return clip;
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void play(Clip clip) {
        clip.start();
    }

    private void pause(Clip clip) {
        if (clip.isRunning()) {
            clip.stop();
        }
    }
    private void resume(Clip clip) {
        if (!clip.isRunning()) {
            clip.start();
        }
    }
    private void stop(Clip clip) {
        clip.stop();
        clip.setFramePosition(0);
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
}
