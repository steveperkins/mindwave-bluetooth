package com.sperkins.mindwave.event;


public class EegEvent extends AbstractEvent {
	
	private Integer delta;
	private Integer theta;
	private Integer lowAlpha;
	private Integer highAlpha;
	private Integer lowBeta;
	private Integer highBeta;
	private Integer lowGamma;
	private Integer midGamma;

	public EegEvent(String deviceAddress, Integer delta, Integer theta, Integer lowAlpha, Integer highAlpha, Integer lowBeta, Integer highBeta, Integer lowGamma, Integer midGamma) {
		super(deviceAddress);
		this.delta = delta;
		this.theta = theta;
		this.lowAlpha = lowAlpha;
		this.highAlpha = highAlpha;
		this.lowBeta = lowBeta;
		this.highBeta = highBeta;
		this.lowGamma = lowGamma;
		this.midGamma = midGamma;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.SIXTEEN_BIT_RAW_WAVE;
	}

	public Integer getDelta() {
		return delta;
	}

	public Integer getTheta() {
		return theta;
	}

	public Integer getLowAlpha() {
		return lowAlpha;
	}

	public Integer getHighAlpha() {
		return highAlpha;
	}

	public Integer getLowBeta() {
		return lowBeta;
	}

	public Integer getHighBeta() {
		return highBeta;
	}

	public Integer getLowGamma() {
		return lowGamma;
	}

	public Integer getMidGamma() {
		return midGamma;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.format("%s %s:", getDeviceAddress(), getEventType().name()));
		sb.append(String.format("\n\tDelta: %d", getDelta()))
		.append(String.format("\n\tTheta: %d", getTheta()))
		.append(String.format("\n\tHigh alpha: %d", getHighAlpha()))
		.append(String.format("\n\tLow alpha: %d", getLowAlpha()))
		.append(String.format("\n\tHigh beta: %d", getHighBeta()))
		.append(String.format("\n\tLow beta: %d", getLowBeta()))
		.append(String.format("\n\tMid gamma: %d", getMidGamma()))
		.append(String.format("\n\tLow gamma: %d", getLowGamma()));
		return sb.toString();
	}
	
	@Override
	public String toJsonString() {
		throw new RuntimeException("Not implemented for EegEvent");
	}
}
