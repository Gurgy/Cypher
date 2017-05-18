package com.github.cypher.gui.root.roomcollectionlistitem;

import com.github.cypher.settings.Settings;
import com.github.cypher.gui.CustomListCell;
import com.github.cypher.gui.FXThreadedObservableValueWrapper;
import com.github.cypher.model.Client;
import com.github.cypher.model.RoomCollection;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;


public class RoomCollectionListItemPresenter extends CustomListCell<RoomCollection> {

	@Inject
	private Client client;

	@Inject
	private Settings settings;

	@FXML
	private StackPane root;
	@FXML
	private ImageView imageView;

	@Override
	protected Node getRoot() {
		return root;
	}

	@Override
	protected void updateBindings() {
		imageView.imageProperty().bind(new FXThreadedObservableValueWrapper<>(getModelComponent().getImageProperty()));
	}

	@Override
	protected void clearBindings() {
		imageView.imageProperty().unbind();
	}
}
