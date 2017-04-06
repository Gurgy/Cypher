package com.github.cypher.root.server.roomlist;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javax.inject.Inject;

// Controller for the root/main pane of the application
public class RoomListPresenter {

	@FXML
	Label label;

	@Inject
	private Integer n1;

	@Inject
	private String s1;

	@FXML
	private void initialize() {
		label.setText(s1);
	}
}
