package com.github.cypher.model;

import com.github.cypher.sdk.api.RestfulHTTPException;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class Room {
	private final StringProperty id;
	private final StringProperty name;
	private final StringProperty topic;
	private final ObjectProperty<URL> avatarUrl;
	private final ObjectProperty<Image> avatar;
	private final StringProperty canonicalAlias;
	private final ObservableList<Member> members;
	private final IntegerProperty memberCount;
	private final ObservableList<String> aliases;
	private final com.github.cypher.sdk.Room sdkRoom;

	public Room(com.github.cypher.sdk.Room sdkRoom) {
		id = new SimpleStringProperty(sdkRoom.getId());
		name = new SimpleStringProperty(sdkRoom.getName());
		topic = new SimpleStringProperty(sdkRoom.getTopic());
		avatarUrl = new SimpleObjectProperty<>(sdkRoom.getAvatarUrl());
		avatar = new SimpleObjectProperty<>(null);
		updateAvatar(sdkRoom.getAvatar());
		canonicalAlias = new SimpleStringProperty(sdkRoom.getCanonicalAlias());
		members = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
		for (com.github.cypher.sdk.Member sdkMember : sdkRoom.getMembers()) {
			members.add(new Member(sdkMember));
		}

		memberCount = new SimpleIntegerProperty(sdkRoom.getMemberCount());
		aliases = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(sdkRoom.getAliases()));
		this.sdkRoom = sdkRoom;

		sdkRoom.addNameListener((observable, oldValue, newValue) -> {
			name.set(newValue);
		});

		sdkRoom.addTopicListener((observable, oldValue, newValue) -> {
			topic.set(newValue);
		});

		sdkRoom.addAvatarUrlListener((observable, oldValue, newValue) -> {
			avatarUrl.set(newValue);
		});

		sdkRoom.addAvatarListener((observable, oldValue, newValue) -> {
			updateAvatar(newValue);
		});

		sdkRoom.addCanonicalAliasListener((observable, oldValue, newValue) -> {
			canonicalAlias.set(newValue);
		});

		sdkRoom.addMemberListener(change -> {
			while (change.next()) {
				if (change.wasAdded()) {
					for (com.github.cypher.sdk.Member sdkMember : change.getAddedSubList()) {
						members.add(new Member(sdkMember));
					}
				}
				if (change.wasRemoved()) {
					for (com.github.cypher.sdk.Member sdkMember : change.getRemoved()) {
						Optional<Member> optionalMember =  members.stream().filter(m -> sdkMember.getUser().getId().equals(m.getUser().getId())).findAny();
						members.remove(optionalMember.get());
					}
				}
			}
			memberCount.set(change.getList().size());
		});

		sdkRoom.addAliasesListener((change -> {
			aliases.setAll(change.getList());
		}));
	}

	public void sendMessage(String body) throws SdkException {
		try {
			sdkRoom.sendTextMessage(body);
		}catch(RestfulHTTPException | IOException ex){
			throw new SdkException(ex);
		}
	}

	private void updateAvatar(java.awt.Image image) {
		try {
			this.avatar.set(
				image == null ? null : Util.createImage(image)
			);
		} catch (IOException e) {
			System.out.printf("IOException when converting user avatar image: %s\n", e);
		}
	}

	public String getId() {
		return id.get();
	}

	public StringProperty idProperty() {
		return id;
	}

	public String getName() {
		return name.get();
	}

	public StringProperty nameProperty() {
		return name;
	}

	public String getTopic() {
		return topic.get();
	}

	public StringProperty topicProperty() {
		return topic;
	}

	public URL getAvatarUrl() {
		return avatarUrl.get();
	}

	public ObjectProperty<URL> avatarUrlProperty() {
		return avatarUrl;
	}

	public Image getAvatar() {
		return avatar.get();
	}

	public ObjectProperty<Image> avatarProperty() {
		return avatar;
	}

	public String getCanonicalAlias() {
		return canonicalAlias.get();
	}

	public StringProperty canonicalAliasProperty() {
		return canonicalAlias;
	}

	public ObservableList<Member> getMembersProperty() {
		return members;
	}

	public int getMemberCount() {
		return memberCount.get();
	}

	public IntegerProperty memberCountProperty() {
		return memberCount;
	}

	public String[] getAliases() {
		return aliases.toArray(new String[aliases.size()]);
	}

	public ObservableList<String> aliasesList() {
		return aliases;
	}
}
