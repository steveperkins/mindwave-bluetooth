package com.sperkins.mindwave.event;


public class PoorSignalEvent extends AbstractEvent {
	private Integer value;
	public PoorSignalEvent(String deviceAddress, Integer poorSignalLevel) {
		super(deviceAddress);
		this.value = poorSignalLevel;
	}
	
	public Integer getValue() {
		return value;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.POOR_SIGNAL_QUALITY;
	}
	
	@Override
	public String toString() {
		return String.format("%s %s: %d", getDeviceAddress(), getEventType().name(), value);
	}
	
	@Override
	public String toJsonString() {
		throw new RuntimeException("Not implemented for PoorSignalEvent");
	}
}
