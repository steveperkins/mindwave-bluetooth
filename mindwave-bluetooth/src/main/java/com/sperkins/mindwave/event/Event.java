package com.sperkins.mindwave.event;

public interface Event {
	public String getDeviceAddress();
	public EventType getEventType();
	public String toString();
	public String toJsonString();
}
