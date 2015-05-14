package com.sperkins.mindwave.event;

public abstract class AbstractEvent implements Event {
	private String deviceAddress;
	public AbstractEvent(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}
	
	@Override
	public String getDeviceAddress() {
		return deviceAddress;
	}

}
