package com.sperkins.mindwave.event;


public class MeditationEvent extends AbstractEvent {
	private Integer value;
	public MeditationEvent(String deviceAddress, Integer meditationLevel) {
		super(deviceAddress);
		this.value = meditationLevel;
	}
	
	public Integer getValue() {
		return value;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.MEDITATION;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s: %d", getDeviceAddress(), getEventType().name(), value);
	}
	
	@Override
	public String toJsonString() {
		throw new RuntimeException("Not implemented for MeditationEvent");
	}
}
