package com.sperkins.mindwave.event;

public enum EventType {
	CONNECT(0xC0),
	DISCONNECT(0xC1),
	AUTOCONNECT(0xC2),
	HEADSET_CONNECTED(0xD0),
	HEADSET_NOT_FOUND(0xD1),
	HEADSET_DISCONNECTED(0xD2),
	REQUEST_DENIED(0xD3),
	STANDBY_SCAN(0XD4),
	BLINK(0x16),
	EXCODE(0x55),
	POOR_SIGNAL_QUALITY(0x02), // (0-255)
    HEART_RATE(0x03), // (0-255)
    ATTENTION(0x04), // eSense percentage (0 to 100)
    MEDITATION(0X05), // eSense percentage (0 to 100) 
    EIGHT_BIT_RAW_WAVE(0x06), // (0-255)
    RAW_MARKER_SECTION_START(0x07),
    SIXTEEN_BIT_RAW_WAVE(0x80), //2 bytes see http://developer.neurosky.com/docs/doku.php?id=thinkgear_communications_protocol#code_definitions_table
    EEG_POWER(0x81), // 32 bytes: eight big-endian 4-byte IEEE 754 floating point values representing delta, theta, low-alpha, high-alpha, low-beta, high-beta, low-gamma, and mid-gamma EEG band power values
    ASIC_EEG_POWER(0x83),
    R_PEAK_INTERVAL(0x86);
	
	private int hexValue;
	private EventType(int hexValue) {
		this.hexValue = hexValue;
	}
	
	public int getHexValue() {
		return hexValue;
	}
	
	public static EventType fromHex(int hexValue) {
		for(EventType code: EventType.values()) {
			if(hexValue == code.getHexValue()) return code;
		}
		return null;
	}
}
