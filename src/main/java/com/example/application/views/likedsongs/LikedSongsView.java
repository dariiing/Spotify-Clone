package com.example.application.views.likedsongs;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.User;
import com.example.application.data.service.LikedSongsService;
import com.example.application.data.service.UserService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.sound.sampled.*;

@PageTitle("Liked Songs")
@Route(value = "liked-songs", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class LikedSongsView extends Div {

    private Grid<LikedSongs> grid;

    private Clip currentClip;
    private String currentPath;
    private Filters filters;
    private final LikedSongsService likedSongsService;
    private final UserService userService;

    public LikedSongsView(LikedSongsService likedSongsService,UserService userService) {
        this.likedSongsService = likedSongsService;
        this.userService = userService;
        setSizeFull();
        addClassNames("liked-songs-view");
        this.currentClip = null;
        this.currentPath = "";
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

    public static class Filters extends Div implements Specification<LikedSongs> {

        private final TextField songName = new TextField("Song/Artist Name");

        public Filters(Runnable onSearch) {

            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            songName.setPlaceholder("Song/Artist name");


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

            add(songName,actions);
        }

        @Override
        public Predicate toPredicate(Root<LikedSongs> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
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
        grid = new Grid<>(LikedSongs.class, false);

        grid.addColumn("songName").setAutoWidth(true);
        grid.addColumn("artistName").setAutoWidth(true);
        grid.addColumn("albumName").setAutoWidth(true);

        User currentUser = userService.getCurrentUser();
        List<LikedSongs> likedSongs = likedSongsService.findByUser(currentUser);
        grid.setItems(likedSongs);
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        //play song
        grid.addComponentColumn(song -> {
            Button playButton = new Button(new Icon("lumo", "play"));
            playButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            playButton.addClickListener(e -> handleButtonAction(playButton, song.getSongName()));
            return playButton;
        }).setHeader("Play");

        //remove song
        grid.addComponentColumn(likedSong -> {
            Button deleteButton = new Button(new Icon("lumo", "minus"));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            deleteButton.addClickListener(event -> deleteLikedSong(likedSong));
            return deleteButton;
        }).setHeader("Remove");

        return grid;
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

    private void deleteLikedSong(LikedSongs likedSong) {
        likedSongsService.delete(likedSong.getId());
        Notification.show("Song removed from liked songs");
        User currentUser = userService.getCurrentUser();
        List<LikedSongs> likedSongs = likedSongsService.findByUser(currentUser);
        grid.setItems(likedSongs);
        refreshGrid();
    }


    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

}
