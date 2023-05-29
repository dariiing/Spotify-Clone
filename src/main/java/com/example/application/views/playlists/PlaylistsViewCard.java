package com.example.application.views.playlists;

import com.example.application.data.entity.SongTable;
import com.example.application.data.service.PlaylistService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.List;

public class PlaylistsViewCard extends ListItem {

    private final PlaylistService playlistService;
    public PlaylistsViewCard(String text, String url,PlaylistService playlistService) {
        this.playlistService = playlistService;
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

//        Button deleteButton = new Button("Delete playlist");
//        deleteButton.addClickListener(e -> showDeleteConfirmationDialog());

        add(div, header, subtitle);

        addClickListener(e -> showSongsDialog(text));
    }

    private void showSongsDialog(String playlistName) {
        Grid<SongTable> grid = new Grid<>(SongTable.class, false);
        grid.addColumn(SongTable::getSongName).setHeader("Song Name").setAutoWidth(true);
        grid.addColumn(SongTable::getArtistName).setHeader("Artist Name").setAutoWidth(true);
        grid.addColumn(SongTable::getAlbumName).setHeader("Album Name").setAutoWidth(true);

        List<SongTable> songs = playlistService.findSongsByPlaylistName(playlistName);
        grid.setItems(songs); // Set the entire songs list to the grid

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
            // addToLikedSongsButton.addClickListener(e -> addToLikedSongs(song));
            return addToLikedSongsButton;
        }).setHeader("Add to Liked Songs");

        //delete from playlist button
        grid.addComponentColumn(song -> {
            Button addToLikedSongsButton = new Button(new Icon("lumo", "minus"));
            addToLikedSongsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            // addToLikedSongsButton.addClickListener(e -> addToLikedSongs(song));
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



    private void showDeleteConfirmationDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.add(new Span("Are you sure you want to delete this playlist?"));
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Button confirmButton = new Button("Delete");
        confirmButton.addClickListener(e -> {
            Component parent = getParent().orElse(null);
            if (parent instanceof VerticalLayout) {
                ((VerticalLayout) parent).remove(this);
            }
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> dialog.close());

        HorizontalLayout buttonContainer = new HorizontalLayout(confirmButton, cancelButton);
        buttonContainer.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        buttonContainer.setWidthFull();

        layout.add(buttonContainer);
        dialog.add(layout);
        dialog.open();
    }
}