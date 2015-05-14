package com.sperkins.mindwave.event;

import com.sperkins.mindwave.Util;

public class BlinkEvent extends AbstractEvent {
	private Integer value;
	public BlinkEvent(String deviceAddress, Integer blinkStrength) {
		super(deviceAddress);
		this.value = blinkStrength;
	}
	
	public Integer getValue() {
		return value;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.BLINK;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s: %d", getDeviceAddress(), getEventType().name(), value);
	}
	
	@Override
	public String toJsonString() {
		return String.format("{ \"deviceAddress\":\"%s\",\"timestamp\":\"%s\",\"blink\":%d }",
				getDeviceAddress(), Util.currentDateAsString(), value);
	}
}
