package com.github.cypher.gui.root.roomcollection.room.chat;

import com.github.cypher.gui.Executor;
import com.github.cypher.gui.FXThreadedObservableListWrapper;
import com.github.cypher.gui.FXThreadedObservableValueWrapper;
import com.github.cypher.gui.root.roomcollection.room.chat.eventlistitem.EventListItemPresenter;
import com.github.cypher.gui.root.roomcollection.room.chat.eventlistitem.EventListItemView;
import com.github.cypher.eventbus.ToggleEvent;
import com.github.cypher.model.Client;
import com.github.cypher.model.Event;
import com.github.cypher.model.Room;
import com.github.cypher.model.SdkException;
import com.github.cypher.settings.Settings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import javax.inject.Inject;
import java.util.Locale;
import java.util.ResourceBundle;

public class ChatPresenter {

	private static final int HISTORY_CHUNK_SIZE = 16;

	@Inject
	private Client client;

	@Inject
	private Settings settings;

	@Inject
	private EventBus eventBus;

	@Inject
	private Executor executor;

	@FXML
	private ListView<Event> eventListView;

	private ScrollBar eventListScrollBar;

	private Integer scrollTo = null;

	private FXThreadedObservableListWrapper<Event> backendListForEventView;

	private final ChangeListener<? super Number> scrollListener = (observable, oldValue, newValue) -> checkMessageHistoryDemand();

	private volatile boolean isLoadingHistory = false;

	@FXML
	private TextArea messageBox;

	@FXML
	private Label roomName;

	@FXML
	private Label roomTopic;

	@FXML
	private SVGPath bufferingIcon;
	private RotateTransition bufferIconAnimation;
	private FadeTransition bufferFadeIn;
	private FadeTransition bufferFadeOut;

	private final ResourceBundle bundle = ResourceBundle.getBundle(
		"com.github.cypher.gui.root.roomcollection.room.chat.chat",
		Locale.getDefault());


	@FXML
	private void initialize() {
		eventBus.register(this);
		messageBox.setDisable(client.getSelectedRoom() == null);

		eventListView.setCellFactory(listView -> {
			EventListItemView memberListItemView = new EventListItemView();
			memberListItemView.getView();
			return (EventListItemPresenter)memberListItemView.getPresenter();
		});

		eventListView.itemsProperty().addListener((observable, oldValue, newValue) -> {

			if(eventListScrollBar != null) {
				eventListScrollBar.valueProperty().removeListener(scrollListener);
			}

			this.eventListScrollBar = getListViewScrollBar(eventListView, Orientation.VERTICAL);

			if(eventListScrollBar != null) {
				eventListScrollBar.valueProperty().addListener(scrollListener);
			}
		});

		// Buffering icon animations
		bufferIconAnimation = new RotateTransition(Duration.millis(1000), bufferingIcon);
		bufferIconAnimation.setCycleCount(Timeline.INDEFINITE);
		bufferIconAnimation.setByAngle(360);
		bufferFadeIn = new FadeTransition(Duration.millis(200), bufferingIcon);
		bufferFadeIn.setFromValue(0.0);
		bufferFadeIn.setToValue(1.0);
		bufferFadeOut = new FadeTransition(Duration.millis(200), bufferingIcon);
		bufferFadeOut.setFromValue(1.0);
		bufferFadeOut.setToValue(0.0);
	}

	private void checkMessageHistoryDemand() {
		if(isLoadingHistory) { return; }

		Room room = client.getSelectedRoom();
		ScrollBar scrollBar = eventListScrollBar;

		if(room != null &&
		   scrollBar != null &&
		   // Is the scroll bar at the top?
		   scrollBar.getValue() == scrollBar.getMin()) {

			// Save current scroll bar position
			scrollTo = backendListForEventView.getList().size() - 1;

			isLoadingHistory = true;
			bufferFadeOut.stop();
			bufferFadeIn.setFromValue(bufferingIcon.getOpacity());
			bufferFadeIn.play();
			bufferIconAnimation.play();

			executor.handle(() -> {
				try {
					// Try to load more history
					boolean more = room.loadEventHistory(HISTORY_CHUNK_SIZE);

					bufferFadeIn.stop();
					bufferFadeOut.setFromValue(bufferingIcon.getOpacity());
					bufferFadeOut.play();
					bufferIconAnimation.stop();
					isLoadingHistory = false;

					if(more) {
						// If not all history is loaded, run method again
						checkMessageHistoryDemand();
					}
				} catch (SdkException e) {
					isLoadingHistory = false;
					System.out.printf("SdkException when trying to get room history: %s\n", e);
				}
			});
		}
	}

	private ScrollBar getListViewScrollBar(ListView listView, Orientation orientation) {
		for(Node node : listView.lookupAll(".scroll-bar")) {
			if(node instanceof ScrollBar &&
			   ((ScrollBar)node).getOrientation() == orientation) {
				return (ScrollBar)node;
			}
		}
		return null;
	}

	@Subscribe
	private void selectedRoomChanged(Room room){
		Platform.runLater(() -> {
			messageBox.setDisable(false);
			(new FXThreadedObservableValueWrapper<>(room.nameProperty())).addListener((invalidated) -> {
				updateRoomName(room);
			} );

			(new FXThreadedObservableValueWrapper<>(room.topicProperty())).addListener((invalidated) -> {
				updateTopicName(room);
			} );

			updateRoomName(room);
			updateTopicName(room);

			if (backendListForEventView != null) {
				backendListForEventView.dispose();
			}
			backendListForEventView = new FXThreadedObservableListWrapper<>(room.getEvents());

			eventListView.setItems(backendListForEventView.getList());
			InvalidationListener l = o -> {
				if(scrollTo != null) {
					eventListView.scrollTo(eventListView.getItems().size() - scrollTo);
				}
			};
			backendListForEventView.getList().addListener(l);

			checkMessageHistoryDemand();
		});
	}

	private void updateRoomName(Room room){
		if (room.getName() == null || room.getName().isEmpty()) {
			roomName.textProperty().setValue(bundle.getString("name.default"));
		}else{
			roomName.textProperty().setValue(room.getName());
		}
	}
	private void updateTopicName(Room room){
		if (room.getTopic() == null || room.getTopic().isEmpty()) {
			roomTopic.textProperty().setValue(bundle.getString("topic.default"));
		}else{
			roomTopic.textProperty().setValue(room.getTopic());
		}
	}

	@Subscribe
	private void handleLoginStateChange(ToggleEvent e) {
		if (e == ToggleEvent.LOGOUT) {
			messageBox.setDisable(true);
		}
	}

	@FXML
	private void showRoomSettings() {
		eventBus.post(ToggleEvent.SHOW_ROOM_SETTINGS);
	}

	@FXML
	private void onMessageBoxKeyPressed(KeyEvent event) {
		if(KeyCode.ENTER.equals(event.getCode())) {
			if (((settings.getControlEnterToSendMessage() && event.isControlDown())
			 || (!settings.getControlEnterToSendMessage() && !event.isShiftDown()))
			 &&  !messageBox.getText().isEmpty()) {

				Room room = client.getSelectedRoom();
				if(room != null) {
					try {
						room.sendMessage(messageBox.getText());
					} catch(SdkException e) {
						System.out.printf("SdkException when trying to send a message: %s\n", e);
					}
				}
				messageBox.clear();
				event.consume();

			} else if(event.isShiftDown()) {
				messageBox.insertText(
						messageBox.getCaretPosition(),
						"\n"
				);
			}
		}
	}
}
