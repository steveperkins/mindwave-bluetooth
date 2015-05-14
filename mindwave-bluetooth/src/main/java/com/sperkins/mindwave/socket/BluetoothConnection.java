package com.sperkins.mindwave.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class BluetoothConnection {
	private String deviceAddress;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	
	public BluetoothConnection(String deviceAddress, DataInputStream inputStream) {
		this.deviceAddress = deviceAddress;
		this.inputStream = inputStream;
	}
	public String getDeviceAddress() {
		return deviceAddress;
	}
	public void setBluetoothAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}
	public DataInputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(DataInputStream inputStream) {
		this.inputStream = inputStream;
	}
	public DataOutputStream getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(DataOutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
}
