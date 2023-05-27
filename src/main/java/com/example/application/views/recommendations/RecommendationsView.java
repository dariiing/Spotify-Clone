package com.example.application.views.recommendations;

import com.example.application.data.entity.LikedSongs;
import com.example.application.data.entity.Recommendations;
import com.example.application.data.entity.SongTable;
import com.example.application.data.entity.User;
import com.example.application.data.service.LikedSongsService;
import com.example.application.data.service.RecommendationService;
import com.example.application.data.service.SongTableService;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

@PageTitle("Recommendations")
@Route(value = "recommendations", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class RecommendationsView extends Div {

    private Grid<Recommendations> grid;

    private Filters filters;
    private final SongTableService songTableService;
    private final LikedSongsService likedSongsService;

    private final AuthenticatedUser authenticatedUser;

    private final RecommendationService recommendationService;

    public RecommendationsView(AuthenticatedUser authenticatedUser, SongTableService songTableService,
                               LikedSongsService likedSongsService, RecommendationService recommendationService) {
        this.authenticatedUser = authenticatedUser;
        this.songTableService = songTableService;
        this.likedSongsService = likedSongsService;
        this.recommendationService = recommendationService;

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

            // Action buttons
            Button resetBtn = new Button("Refresh");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            resetBtn.addClickListener(e -> {
                onSearch.run();
            });

            Label label = new Label("Song recommendations based on your liked songs");

            Div actions = new Div(label, resetBtn);
            actions.addClassName(LumoUtility.Gap.SMALL);
            actions.addClassName("actions");
            add(actions);
        }

        @Override
        public Predicate toPredicate(Root<Recommendations> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            List<Predicate> predicates = new ArrayList<>();

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
        grid = new Grid<>(Recommendations.class, false);
        grid.setItems(recommendationService.findAll()); // Set the items from the recommendation table

        grid.addColumn("songName").setAutoWidth(true);
        grid.addColumn("artistName").setAutoWidth(true);
        grid.addColumn("albumName").setAutoWidth(true);

        grid.setItems(query -> recommendationService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)),
                filters).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.addClassNames(LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);

        //play song
        grid.addComponentColumn(person -> {
            Button addToPlaylistButton = new Button(new Icon("lumo", "play"));
            addToPlaylistButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            return addToPlaylistButton;
        }).setHeader("Play");

        grid.addComponentColumn(person -> {
            Button addToPlaylistButton = new Button(new Icon(VaadinIcon.HEART));
            addToPlaylistButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addToPlaylistButton.addClickListener(e -> addToLikedSongs(person));
            return addToPlaylistButton;
        }).setHeader("Add to Liked Songs");

        //add to playlist
        grid.addComponentColumn(person -> {
            Button addToPlaylistButton = new Button(new Icon("lumo", "plus"));
            addToPlaylistButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            return addToPlaylistButton;
        }).setHeader("Add to Playlist");

        return grid;
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void addToLikedSongs(Recommendations song) {
        Optional<User> optionalUser = authenticatedUser.get();
        if (optionalUser.isPresent()) {
            User currentUser = optionalUser.get();
            LikedSongs likedSong = new LikedSongs();
            likedSong.setSongName(song.getSongName());
            likedSong.setArtistName(song.getArtistName());
            likedSong.setAlbumName(song.getAlbumName());
            likedSong.setUser(currentUser);

            likedSongsService.update(likedSong);

            // Optional: Show a notification or perform any other desired action
            Notification.show("Song added to Liked Songs");
        } else {
            // Handle the case when the current user is not available
            Notification.show("User not logged in");
        }
    }
}
