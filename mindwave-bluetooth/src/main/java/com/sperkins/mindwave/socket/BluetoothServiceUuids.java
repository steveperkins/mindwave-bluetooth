package com.sperkins.mindwave.socket;

public enum BluetoothServiceUuids {
	SDP(0x0001),
	RFCOMM(0x0003),
	OBEX(0x0008),	
	HTTP(0x000C),	
	L2CAP(0x0100),	
	BNEP(0x000F),	
	SERIAL_PORT(0x1101),
//	ServiceDiscoveryServerServiceClassID	0x1000	16-bit
//	BrowseGroupDescriptorServiceClassID	0x1001	16-bit
	PUBLIC_BROWSE_GROUP(0x1002),
	OBEX_OBJECT_PUSH(0x1105),
	OBEX_FILE_TRANSFER(0x1106),
	PERSONAL_AREA_NETWORKING_USER(0x1115),
	NETWORK_ACCESS_POINT(0x1116),
	GROUP_NETWORK(0x1117);
	
	long hexValue;
	private BluetoothServiceUuids(long hexValue) {
		this.hexValue = hexValue;
	}
	
	public long getHexValue() {
		return hexValue;
	}
}
