package com.sperkins.mindwave.socket;

/**
 * Discovers and connects to Bluetooth devices.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sperkins.mindwave.event.EventType;

public class BluetoothConnector  {
	private static LocalDevice bluetoothAdapter;
	
	private final Object inquiryCompletedLock = new Object();
	private final Object serviceSearchCompletedLock = new Object();
	
	private final List<RemoteDevice> remoteDevices = new ArrayList<RemoteDevice>();
	private final List<ServiceRecord> serviceRecords = new ArrayList<ServiceRecord>();
	
	private static final Logger LOG = LoggerFactory.getLogger(BluetoothConnector.class);
	
	private static final BluetoothConnector INSTANCE = new BluetoothConnector();
	
	private static LocalDevice getLocalDevice() throws BluetoothStateException {
		if(null == bluetoothAdapter) {
			bluetoothAdapter = LocalDevice.getLocalDevice();
			LOG.debug("Local device address: " + bluetoothAdapter.getBluetoothAddress());
		}
		return bluetoothAdapter;
	}
	
	/**
	 * Establishes connections to all Mindwave devices paired and in range. Returns the Bluetooth address and input stream for each device.
	 * 
	 * @param bluetoothAddress
	 * @return
	 * @throws BluetoothStateException
	 * @throws InterruptedException
	 */
	public static List<BluetoothConnection> getConnections() throws BluetoothStateException, InterruptedException {
		return INSTANCE.connect();
	}
	
	/**
	 * The current connect() implementation only attempts to connect to headsets previously paired with this device 
	 * to avoid long delays while scanning for new devices. All previously-paired devices will be connected unless you 
	 * use connect(String) to specify a device ID (MAC address). 
	 */
	public List<BluetoothConnection> connect() throws BluetoothStateException, InterruptedException {
		List<BluetoothConnection> connections = new ArrayList<BluetoothConnection>();
		DiscoveryAgent discoveryAgent = getLocalDevice().getDiscoveryAgent();
		DiscoveryListener discoveryListener = new BluetoothDeviceDiscoveryListener();

		// Gets all devices already paired with this device
		RemoteDevice[] preknownRemoteDevices = discoveryAgent.retrieveDevices(DiscoveryAgent.PREKNOWN);
		if(null != preknownRemoteDevices) remoteDevices.addAll(Arrays.asList(preknownRemoteDevices));
		if(remoteDevices.isEmpty()) {
	
			synchronized(inquiryCompletedLock) {
	            boolean started = discoveryAgent.startInquiry(DiscoveryAgent.LIAC, discoveryListener);
	            
	            if (started) {
	            	LOG.debug("Querying devices...");
	                inquiryCompletedLock.wait();
	                LOG.debug(remoteDevices.size() +  " available devices found");
	            }
			} // end synchronized block
		} else {
			LOG.debug(remoteDevices.size() +  " preknown devices found");
		}
		
		UUID serialPortUuid = new UUID(BluetoothServiceUuids.SERIAL_PORT.getHexValue());
		UUID[] desiredServiceUuids = new UUID[] { serialPortUuid };
		
		for(RemoteDevice remoteDevice: remoteDevices) {
			LOG.debug("Locating services on device " + remoteDevice.getBluetoothAddress());
			synchronized(serviceSearchCompletedLock) {
				int result = discoveryAgent.searchServices(null, desiredServiceUuids, remoteDevice, discoveryListener);
				if( DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE == result) {
					LOG.debug("Encountered paired headset " + remoteDevice.getBluetoothAddress() + " but it could not be reached");
					continue;
				}
				serviceSearchCompletedLock.wait();
				
				
				LOG.debug(serviceRecords.size() + " service records found");
				
				if(null != serviceRecords && serviceRecords.size() > 0) {
					for(ServiceRecord serviceRecord: serviceRecords) {
						String connectionUrl = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false); // I have no idea what these args are
						StreamConnection connection;
						try {
							connection = (StreamConnection)Connector.open(connectionUrl);
							DataInputStream inputStream = new DataInputStream(connection.openDataInputStream());
							BluetoothConnection bluetoothConnection = new BluetoothConnection(remoteDevice.getBluetoothAddress(), inputStream);
							bluetoothConnection.setOutputStream(new DataOutputStream(connection.openOutputStream()));
							connections.add(bluetoothConnection);
							LOG.info("Connected to " + remoteDevice.getBluetoothAddress());
						} catch (IOException e) {
							LOG.error("Could not connect to " + remoteDevice.getBluetoothAddress() + ": " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		}
			
		return connections;

	}

	private int port = 1;
	public BluetoothConnection connect(String deviceAddress) throws IOException {
		String uri = "btspp://" + deviceAddress + ":" + (port++) + ";authenticate=false;encrypt=false;master=true;";
		DataInputStream inputStream = Connector.openDataInputStream( uri);
		DataOutputStream outputStream = Connector.openDataOutputStream( uri);
		
		// Update existing connection if one exists
		BluetoothConnection connection = new BluetoothConnection(deviceAddress, inputStream);
		connection.setOutputStream(outputStream);
		return connection;
	}
	
	public static void disconnect(BluetoothConnection connection) throws IOException {
		connection.getOutputStream().write(EventType.DISCONNECT.getHexValue());
	}
	
	class BluetoothDeviceDiscoveryListener implements DiscoveryListener {
		@Override
		public void servicesDiscovered(int transId, ServiceRecord[] serviceRecordsArray) {
			LOG.debug("Services discovered, transId=" + transId);
			serviceRecords.addAll(Arrays.asList(serviceRecordsArray));
		}
		
		@Override
		public void serviceSearchCompleted(int transId, int responseCode) {
			LOG.debug("Search completed, transId=" + transId + ", responseCode=" + responseCode);
			synchronized(serviceSearchCompletedLock) {
				serviceSearchCompletedLock.notifyAll();
			}
		}
		
		@Override
		public void inquiryCompleted(int discoveryType) {
			LOG.debug("Inquiry completed");
			synchronized(inquiryCompletedLock){
                inquiryCompletedLock.notifyAll();
            }
		}
		
		@Override
		public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
			try {
				LOG.debug("Device discovered: " + remoteDevice.getBluetoothAddress() + "/" + remoteDevice.getFriendlyName(false));
				LOG.debug("Device class: " + deviceClass.getMajorDeviceClass() + ":" + deviceClass.getMinorDeviceClass());
				LOG.debug("Services classes: " + deviceClass.getServiceClasses());
				if(remoteDevice.getFriendlyName(false).equals("MindWave Mobile")) remoteDevices.add(remoteDevice);
			} catch (IOException e) {
				LOG.error("Could not get friendly name for device " + remoteDevice.getBluetoothAddress() + ": " + e.getMessage());
				e.printStackTrace();
			}
			
		}
		
	}

}