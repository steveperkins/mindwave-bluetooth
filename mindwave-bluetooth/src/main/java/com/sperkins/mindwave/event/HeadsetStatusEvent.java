package com.sperkins.mindwave.event;


public class HeadsetStatusEvent extends AbstractEvent {
	private EventType eventType;
	private String reportedAddress;
	
	public HeadsetStatusEvent(String deviceAddress, String reportedAddress, EventType eventType) {
		super(deviceAddress);
		this.setReportedAddress(reportedAddress);
		this.eventType = eventType;
	}
	
	public String getReportedAddress() {
		return reportedAddress;
	}

	public void setReportedAddress(String reportedAddress) {
		this.reportedAddress = reportedAddress;
	}

	@Override
	public EventType getEventType() {
		return eventType;
	}
	
	@Override
	public String toString() {
		return String.format("%s\t%s(%s)", getDeviceAddress(), getEventType().name(), getReportedAddress());
	}
	
	@Override
	public String toJsonString() {
		throw new RuntimeException("Not implemented for HeadsetStatusEvent");
	}
}
