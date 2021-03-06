package com.github.cypher.gui.root.roomcollection;

import com.github.cypher.eventbus.ToggleEvent;
import com.github.cypher.gui.FXThreadedObservableListWrapper;
import com.github.cypher.gui.FXThreadedObservableValueWrapper;
import com.github.cypher.gui.root.roomcollection.directory.DirectoryView;
import com.github.cypher.gui.root.roomcollection.room.RoomView;
import com.github.cypher.gui.root.roomcollection.roomlistitem.RoomListItemPresenter;
import com.github.cypher.gui.root.roomcollection.roomlistitem.RoomListItemView;
import com.github.cypher.model.Room;
import com.github.cypher.model.Client;
import com.github.cypher.model.RoomCollection;
import com.github.cypher.model.PMCollection;
import com.github.cypher.model.GeneralCollection;
import com.github.cypher.model.Server;
import com.github.cypher.settings.Settings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;
import java.util.Locale;
import java.util.ResourceBundle;

public class RoomCollectionPresenter {

	private final ResourceBundle bundle = ResourceBundle.getBundle(
		"com.github.cypher.gui.root.roomcollection.roomcollection",
		Locale.getDefault()
	);

	@Inject
	private Client client;

	@Inject
	private Settings settings;

	@Inject
	private EventBus eventBus;

	@FXML
	private StackPane rightSideStackPane;

	@FXML
	private ListView<Room> roomListView;

	@FXML
	private Label serverName;

	private Parent directoryPane;
	private boolean showDirectory;

	private FXThreadedObservableListWrapper<Room> backendListForView;

	@FXML
	private void initialize() {
		eventBus.register(this);
		directoryPane = new DirectoryView().getView();
		rightSideStackPane.getChildren().add(directoryPane);
		Parent roomPane = new RoomView().getView();
		rightSideStackPane.getChildren().add(roomPane);

		roomCollectionChanged(client.getSelectedRoomCollection());

		roomListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				eventBus.post(newValue);
				eventBus.post(ToggleEvent.HIDE_ROOM_SETTINGS);
			}
		});
		showDirectory = false;
	}

	// In the future if separate fxml/views/presenters exists for Server/PMCollection/GeneralCollection
	// we change which pane is in the front of the StackPane.
	//
	// Currently while only RoomCollection exists buttons are just enabled and disabled depending on which kind of
	// RoomCollection is currently shown.
	@Subscribe
	private void roomCollectionChanged(RoomCollection roomCollection) {
		Platform.runLater(()->{
			if (backendListForView != null) {
				backendListForView.dispose();
			}
			backendListForView = new FXThreadedObservableListWrapper<>(roomCollection.getRoomsProperty());

			roomListView.setCellFactory(listView -> {
				RoomListItemView roomListItemView = new RoomListItemView();
				roomListItemView.getView();
				return (RoomListItemPresenter) roomListItemView.getPresenter();
			});

			roomListView.setItems(backendListForView.getList());

			serverName.textProperty().unbind();
			if (roomCollection instanceof Server) {
				serverName.textProperty().bind(
					new FXThreadedObservableValueWrapper<>(
						((Server) roomCollection).nameProperty()
					)
				);
			} else if (roomCollection instanceof PMCollection) {
				serverName.textProperty().setValue(bundle.getString("pm"));
			} else if (roomCollection instanceof GeneralCollection) {
				serverName.textProperty().setValue(bundle.getString("general"));
			}

			// Handle room selection (selects the first room in the RoomCollection if the RoomCollection
			// isn't empty and if the previously selected room isn't in the current RoomCollection)
			if (roomCollection.getRoomsProperty().contains(client.getSelectedRoom())) {
				roomListView.getSelectionModel().select(client.getSelectedRoom());
			} else if (roomCollection.getRoomsProperty().size() > 0) {
				eventBus.post(roomCollection.getRoomsProperty().get(0));
				roomListView.getSelectionModel().select(0);
			}
		});
	}

	@Subscribe
	private void handleLoginStateChange(ToggleEvent e) {
		if (e == ToggleEvent.LOGOUT) {
			showDirectory = false;
			directoryPane.toBack();
		}
	}

	@Subscribe
	private void toggleDirectory(ToggleEvent e) {
		Platform.runLater(()-> {
			if (e == ToggleEvent.SHOW_DIRECTORY && !showDirectory){
				directoryPane.toFront();
				showDirectory = true;
			}else if (e == ToggleEvent.HIDE_DIRECTORY && showDirectory){
				directoryPane.toBack();
				showDirectory = false;
			}else if (e == ToggleEvent.TOGGLE_DIRECTORY){
				if (showDirectory){
					directoryPane.toBack();
				}else{
					directoryPane.toFront();
				}
				showDirectory = !showDirectory;
			}
		});
	}

	@FXML
	private void onShowDirectoryClick() {
		eventBus.post(ToggleEvent.TOGGLE_DIRECTORY);
	}
}
