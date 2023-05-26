package com.example.application.views.playlists;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.OrderedList;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.MaxWidth;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import jakarta.annotation.security.PermitAll;

@PageTitle("Playlists")
@Route(value = "playlists", layout = MainLayout.class)
@PermitAll
public class PlaylistsView extends Main {

    private OrderedList imageContainer;

    public PlaylistsView() {
        constructUI();

        imageContainer.add(new PlaylistsViewCard("Playlist 1",
                "https://images.unsplash.com/photo-1494232410401-ad00d5433cfa?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80"));
        imageContainer.add(new PlaylistsViewCard("Playlist 2",
                "https://images.unsplash.com/photo-1494232410401-ad00d5433cfa?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80"));
        imageContainer.add(new PlaylistsViewCard("Playlist 3",
                "https://images.unsplash.com/photo-1494232410401-ad00d5433cfa?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80"));
    }

    private void constructUI() {
        addClassNames("playlists-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        H2 header = new H2("Playlists");
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.XLARGE, FontSize.XXXLARGE);
        headerContainer.add(header);

        imageContainer = new OrderedList();
        imageContainer.addClassNames(Gap.MEDIUM, Display.GRID, ListStyleType.NONE, Margin.NONE, Padding.NONE);

        Button createPlaylistButton = new Button("Create Playlist");
        createPlaylistButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createPlaylistButton.addClickListener(e -> showCreatePlaylistDialog());

        container.add(headerContainer, createPlaylistButton);
        add(container, imageContainer);
    }

    private void showCreatePlaylistDialog() {
        Dialog dialog = new Dialog();
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        VerticalLayout layout = new VerticalLayout();
        layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        layout.add(new H2("Create Playlist"));

        TextField nameField = new TextField();
        nameField.setLabel("Playlist Name");

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setUploadButton(new Button("Upload Image"));
        upload.setAcceptedFileTypes("image/jpeg", "image/png");

        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.setSpacing(true);

        Button confirmButton = new Button("Create");
        confirmButton.addClickListener(e -> {
            String playlistName = nameField.getValue();
            String imageUrl = buffer.getFileData() != null ? buffer.getFileData().getFileName() : "https://images.unsplash.com/photo-1494232410401-ad00d5433cfa?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80";
            PlaylistsViewCard newPlaylistCard = new PlaylistsViewCard(playlistName, imageUrl);
            imageContainer.add(newPlaylistCard);
            dialog.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> dialog.close());

        buttonContainer.add(confirmButton, cancelButton);

        layout.add(nameField, upload, buttonContainer);
        dialog.add(layout);
        dialog.open();
    }



}