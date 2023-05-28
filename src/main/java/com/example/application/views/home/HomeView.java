package com.example.application.views.home;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.Playlist;
import com.example.application.data.entity.SongTable;
import com.example.application.data.entity.User;
import com.example.application.data.service.LikedSongsService;
import com.example.application.data.service.PlaylistService;
import com.example.application.data.service.SongTableService;
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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
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
    private Filters filters;
    private final SongTableService songTableService;

    private final LikedSongsService likedSongsService;

    private final AuthenticatedUser authenticatedUser;

    private final PlaylistService playlistService;

    public HomeView(AuthenticatedUser authenticatedUser,SongTableService songTableService, LikedSongsService likedSongsService,PlaylistService playlistService) {
        this.authenticatedUser = authenticatedUser;
        this.songTableService = songTableService;
        this.likedSongsService = likedSongsService;
        this.currentClip = null;
        this.currentPath = "";
        this.playlistService = playlistService;
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
//        private final TextField artistName = new TextField("Artist Name");

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            songName.setPlaceholder("Song/Artist Name");
//            artistName.setPlaceholder("Michael Jackson");

            // Action buttons
            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                songName.clear();
//                artistName.clear();
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
//            if (!artistName.isEmpty()) {
//                String lowerCaseFilter = songName.getValue().toLowerCase();
//
//                Predicate artistNameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("artistName")),
//                        lowerCaseFilter + "%");
//                predicates.add(criteriaBuilder.or(artistNameMatch));
//
//            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }

        private String ignoreCharacters(String characters, String in) {
            String result = in;
            for (int i = 0; i < characters.length(); i++) {
                result = result.replace("" + characters.charAt(i), "");
            }
            return result;
        }

        private Expression<String> ignoreCharacters(String characters, CriteriaBuilder criteriaBuilder,
                Expression<String> inExpression) {
            Expression<String> expression = inExpression;
            for (int i = 0; i < characters.length(); i++) {
                expression = criteriaBuilder.function("replace", String.class, expression,
                        criteriaBuilder.literal(characters.charAt(i)), criteriaBuilder.literal(""));
            }
            return expression;
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
            addToLikedSongsButton.addClickListener(e -> addToLikedSongs(person));
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

        Label titleLabel = new Label("Add to Playlist");
//        MultiSelectListBox<Playlist> playlistListBox = new MultiSelectListBox<>();
////        playlistService.getAllPlaylists()
//        List<Playlist> availablePlaylists = null;
//        playlistListBox.setItems(availablePlaylists);

        Button addButton = new Button("Add");
        addButton.addClickListener(e -> {
//            Set<Playlist> selectedPlaylists = playlistListBox.getSelectedItems();
            // Add the song to the selected playlists
//            for (Playlist playlist : selectedPlaylists) {
//                playlist.getSongs().add(song);
//            }
            dialog.close();
            Notification.show("Song added to playlists");
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> dialog.close());

        dialog.add(titleLabel,new HorizontalLayout(addButton, cancelButton));
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
        Optional<User> optionalUser = authenticatedUser.get();
        if (optionalUser.isPresent()) {
            User currentUser = optionalUser.get();
            LikedSongs likedSong = new LikedSongs();
            likedSong.setSongName(song.getSongName());
            likedSong.setArtistName(song.getArtistName());
            likedSong.setAlbumName(song.getAlbumName());
            likedSong.setGenre(song.getGenre());
            likedSong.setUser(currentUser);

            // Save the liked song using the LikedSongsService
            likedSongsService.update(likedSong);

            // Optional: Show a notification or perform any other desired action
            Notification.show("Song added to Liked Songs");
        } else {
            // Handle the case when the current user is not available
            Notification.show("User not logged in");
        }
    }


}
