package com.github.cypher.sdk;

import com.github.cypher.sdk.api.ApiLayer;
import com.google.gson.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

import java.net.MalformedURLException;
import java.net.URL;

public class Message extends Event {
	private final StringProperty body = new SimpleStringProperty("");
	private final StringProperty type = new SimpleStringProperty("");
	private final ObjectProperty<URL> url      = new SimpleObjectProperty<>(null);
	private final StringProperty formattedBody = new SimpleStringProperty(null);
	private final StringProperty formatType    = new SimpleStringProperty(null);

	Message(ApiLayer api, int originServerTs, User sender, String eventId, int age, JsonObject content) {
		super(api, originServerTs, sender, eventId, "m.room.message", age);
		if(content.has("body")) {
			this.body.set(content.get("body").getAsString());
		}
		if(content.has("msgtype")) {
			this.type.set(content.get("msgtype").getAsString());
		}

		if(content.has("url")) {
			try {
				this.url.set(new URL(content.get("url").getAsString()));
			} catch(MalformedURLException e) {
				e.printStackTrace();
			}
		}

		if(content.has("format") &&
		   content.has("formatted_body")) {
			formatType.set(content.get("format").getAsString());
			formattedBody.set(content.get("formatted_body").getAsString());
		}
	}

	public void addBodyListener(ChangeListener<? super String> listener) {
		body.addListener(listener);
	}

	public void removeBodyListener(ChangeListener<? super String> listener) {
		body.removeListener(listener);
	}

	public void addTypeListener(ChangeListener<? super String> listener) {
		type.addListener(listener);
	}

	public void removeTypeListener(ChangeListener<? super String> listener) {
		type.removeListener(listener);
	}

	public void addUrlListener(ChangeListener<? super URL> listener) {
		url.addListener(listener);
	}

	public void removeUrlListener(ChangeListener<? super URL> listener) {
		url.removeListener(listener);
	}

	public void addFormattedBodyListener(ChangeListener<? super String> listener) {
		formattedBody.addListener(listener);
	}

	public void removeFormattedBodyListener(ChangeListener<? super String> listener) {
		formattedBody.removeListener(listener);
	}

	public void addFormatTypeListener(ChangeListener<? super String> listener) {
		formatType.addListener(listener);
	}

	public void removeFormatTypeListener(ChangeListener<? super String> listener) {
		formatType.removeListener(listener);
	}

	public String getBody() { return body.get(); }
	public String getType() { return type.get(); }
	public URL    getUrl()  { return url.get();  }
	public String getFormattedBody() { return formattedBody.get(); }
	public String getFormatType() { return formatType.get(); }
}
