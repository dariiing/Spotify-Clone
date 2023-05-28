package com.example.application.views.playlists;

import com.example.application.data.entity.Playlist;
import com.example.application.data.entity.User;
import com.example.application.data.service.PlaylistService;
import com.example.application.data.service.UserService;
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

import java.util.List;

@PageTitle("Playlists")
@Route(value = "playlists", layout = MainLayout.class)
@PermitAll
public class PlaylistsView extends Main {

    private OrderedList imageContainer;
    private final PlaylistService playlistService;
    private final UserService userService;

    public PlaylistsView(PlaylistService playlistService, UserService userService) {
        this.playlistService = playlistService;
        this.userService = userService;
        constructUI();
    }
    private void constructUI() {
        imageContainer = new OrderedList();
        imageContainer.addClassNames(
                Gap.SMALL,
                Display.GRID,
                ListStyleType.NONE,
                Margin.NONE,
                Padding.NONE,
                "playlist-cards"
        );
        User currentUser = userService.getCurrentUser();
        List<Playlist> playlists = playlistService.findByUser(currentUser);

        // Create playlist cards dynamically based on user's playlists
        for (Playlist playlist : playlists) {
            imageContainer.add(new PlaylistsViewCard(playlist.getPlaylistName(), "https://images.unsplash.com/photo-1494232410401-ad00d5433cfa?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80"));
        }

        addClassNames("playlists-view");
        addClassNames(MaxWidth.SCREEN_LARGE, Margin.Horizontal.AUTO, Padding.Bottom.LARGE, Padding.Horizontal.LARGE);

        HorizontalLayout container = new HorizontalLayout();
        container.addClassNames(AlignItems.CENTER, JustifyContent.BETWEEN);

        VerticalLayout headerContainer = new VerticalLayout();
        H2 header = new H2("Playlists");
        header.addClassNames(Margin.Bottom.NONE, Margin.Top.XLARGE, FontSize.XXXLARGE);
        headerContainer.add(header);

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
        nameField.setRequiredIndicatorVisible(true); // Set the field as mandatory

        HorizontalLayout buttonContainer = new HorizontalLayout();
        buttonContainer.setSpacing(true);

        Button confirmButton = new Button("Create");
        confirmButton.addClickListener(e -> {
            String playlistName = nameField.getValue();
            if (playlistName.isEmpty()) {
                nameField.setInvalid(true);
                nameField.setErrorMessage("Playlist name is required");
            } else {
                String imageUrl = "https://images.unsplash.com/photo-1494232410401-ad00d5433cfa?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1170&q=80";
                User currentUser = userService.getCurrentUser();

                Playlist newPlaylist = new Playlist();
                newPlaylist.setPlaylistName(playlistName);
                newPlaylist.setUser(currentUser);
                playlistService.save(newPlaylist);

                PlaylistsViewCard newPlaylistCard = new PlaylistsViewCard(playlistName, imageUrl);
                imageContainer.add(newPlaylistCard);
                dialog.close();
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(e -> dialog.close());

        buttonContainer.add(confirmButton, cancelButton);

        layout.add(nameField, buttonContainer);
        dialog.add(layout);
        dialog.open();
    }





}