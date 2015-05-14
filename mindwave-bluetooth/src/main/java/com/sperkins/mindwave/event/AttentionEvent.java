package com.sperkins.mindwave.event;

import com.sperkins.mindwave.Util;

public class AttentionEvent extends AbstractEvent {
	private Integer value;
	public AttentionEvent(String deviceAddress, Integer value) {
		super(deviceAddress);
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.ATTENTION;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s: %d", getDeviceAddress(), getEventType().name(), value);
	}

	@Override
	public String toJsonString() {
		return String.format("{ \"deviceAddress\":\"%s\",\"timestamp\":\"%s\",\"attention\":%d }",
				getDeviceAddress(), Util.currentDateAsString(), value);
	}
	
}
