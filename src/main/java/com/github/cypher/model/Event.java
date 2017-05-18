package com.github.cypher.model;

public class Event {
	private final String eventId;
	private final long originServerTimesStamp;
	private final User sender;

	Event(Client client,com.github.cypher.sdk.Event sdkEvent){
		eventId = sdkEvent.getEventId();
		originServerTimesStamp = sdkEvent.getOriginServerTs();
		sender = client.getUser(sdkEvent.getSender().getId());
	}

	public String getEventId() {
		return eventId;
	}

	public long getOriginServerTimesStamp() {
		return originServerTimesStamp;
	}

	public User getSender() {
		return sender;
	}
}