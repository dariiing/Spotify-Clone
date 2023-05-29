package com.example.application.views.recommendations;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.Recommendations;
import com.example.application.data.entity.SongTable;
import com.example.application.data.entity.User;
import com.example.application.data.service.LikedSongsService;
import com.example.application.data.service.RecommendationService;
import com.example.application.data.service.SongTableService;
import com.example.application.data.service.UserService;
import com.example.application.security.AuthenticatedUser;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
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

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.sound.sampled.*;


@PageTitle("Recommendations")
@Route(value = "recommendations", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class RecommendationsView extends Div {

    private Grid<Recommendations> grid;
    private Clip currentClip;
    private String currentPath;
    private Filters filters;
//    private final SongTableService songTableService;
    private final LikedSongsService likedSongsService;

    private final RecommendationService recommendationService;
    private final UserService userService;


    public RecommendationsView(LikedSongsService likedSongsService, RecommendationService recommendationService,UserService userService) {
//        this.songTableService = songTableService;
        this.likedSongsService = likedSongsService;
        this.recommendationService = recommendationService;
        this.userService = userService;
        this.currentClip = null;
        this.currentPath = "";
        setSizeFull();
        addClassNames("recommendations-view");

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

    public static class Filters extends Div implements Specification<Recommendations> {


        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            Label label = new Label("Here are some song recommendations based on your liked songs");
            Div actions = new Div(label);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");
            add(actions);
        }

        @Override
        public Predicate toPredicate(Root<Recommendations> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        }
    }

    private Component createGrid() {
        grid = new Grid<>(Recommendations.class, false);
        grid.setItems(recommendationService.findAll());

        User currentUser = userService.getCurrentUser();
        List<Recommendations> recs = recommendationService.findByUser(currentUser);
        grid.addColumn("songName").setAutoWidth(true);
        grid.addColumn("artistName").setAutoWidth(true);
        grid.addColumn("albumName").setAutoWidth(true);

        grid.setItems(recs);
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
            Button addToPlaylistButton = new Button(new Icon(VaadinIcon.HEART));
            addToPlaylistButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addToPlaylistButton.addClickListener(e -> addToLikedSongs(person));
            return addToPlaylistButton;
        }).setHeader("Add to Liked Songs");

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void handleButtonAction(Button button, String songFilePath) {
        String selectedPath = songFilePath; // name of the song
        String songPath = "D:\\facultate\\anul 2\\Semestrul 2\\programare avansata(java)\\Proiect\\Spotify-Clone\\src\\main\\resources" + "\\" + songFilePath + ".wav";
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

    private void addToLikedSongs(Recommendations song) {
        User currentUser = userService.getCurrentUser();
        LikedSongs likedSong = new LikedSongs();
        likedSong.setSongName(song.getSongName());
        likedSong.setArtistName(song.getArtistName());
        likedSong.setAlbumName(song.getAlbumName());
        likedSong.setUser(currentUser);
        likedSongsService.update(likedSong);
        Notification.show("Song added to Liked Songs");
        }
}
