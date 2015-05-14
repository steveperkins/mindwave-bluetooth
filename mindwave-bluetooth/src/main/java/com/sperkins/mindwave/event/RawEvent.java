package com.sperkins.mindwave.event;

import java.util.Arrays;

public class RawEvent extends AbstractEvent {
	private int[] values;
	public RawEvent(String deviceAddress, int[] values) {
		super(deviceAddress);
		this.values = values;
	}
	
	public int[] getValues() {
		return values;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.EIGHT_BIT_RAW_WAVE;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s: %s", getDeviceAddress(), getEventType().name(), Arrays.toString(values));
	}
	
	@Override
	public String toJsonString() {
		throw new RuntimeException("Not implemented for RawEvent");
	}
}
