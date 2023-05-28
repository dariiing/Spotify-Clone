package com.example.application.views.playlists;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class PlaylistsViewCard extends ListItem {

    public PlaylistsViewCard(String text, String url) {
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
        // Create a dialog to display the songs
        Dialog dialog = new Dialog();
        dialog.setWidth("800px");
        dialog.setHeight("600px");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(true);

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);

        H2 playlistHeading = new H2(playlistName);
        layout.add(playlistHeading);

        // Create a scrollable layout for the songs
        Div songsContainer = new Div();
        songsContainer.getStyle().set("max-height", "300px");
        songsContainer.getStyle().set("overflow-y", "auto");

        OrderedList songList = new OrderedList();
        songList.add(new ListItem("Song 1"));
        songList.add(new ListItem("Song 2"));
        // Add more songs to the list

        songsContainer.add(songList);
        layout.add(songsContainer);

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