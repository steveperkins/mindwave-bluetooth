package com.sperkins.mindwave.socket;

/**
 * Reads raw data values in from the Mindwave headset. Parses inbound packets and raises events to registered listeners.
 * 
 * On calling BluetoothSocket.start(), BluetoothSocket will attempt to connect to all headsets previously paired with this device.
 * Once a device is connected, listeners will immediately be notified of any new events.
 * 
 * Use BluetoothSocket.stop() to shut down all headset streams and connections.
 * 
 */

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sperkins.mindwave.event.Event;
import com.sperkins.mindwave.event.MindwaveEventListener;

public class BluetoothSocket  {
	private final static Logger LOG = LoggerFactory.getLogger(BluetoothSocket.class.getName()); 
	private final List<MindwaveEventListener> listeners = new ArrayList<MindwaveEventListener>();
	private List<BluetoothConnection> connections;
	private List<BluetoothStreamParseThread> streamThreads;

	private boolean running = true;
	
	public BluetoothSocket() {}
	
	public BluetoothSocket(MindwaveEventListener listener) {
		addListener(listener);
	}

	public void addListener(MindwaveEventListener listener) {
		getListeners().add(listener);
	}
	
	protected List<MindwaveEventListener> getListeners() {
		return listeners;
	}
	
	protected void notifyListeners(Event event) {
		for(MindwaveEventListener listener: getListeners()) {
			listener.onEvent(event);
		}
	}
	
	protected void notifyListeners(List<Event> events) {
		if(null == events || events.isEmpty()) return;
		for(Event event: events) {
			for(MindwaveEventListener listener: getListeners()) {
				listener.onEvent(event);
			}
		}
	}

	public void start() throws IOException, InterruptedException {
		connections = BluetoothConnector.getConnections();
		if(null == connections || connections.isEmpty()) {
			throw new NullPointerException("No Bluetooth connections found!");
		}
		startStreaming();
	}

	public void stop() {
		LOG.debug("Stopping Bluetooth socket!");
		if (running) {
			if(null != streamThreads) {
				for(BluetoothStreamParseThread thread: streamThreads) {
					thread.stopThread();
				}
			}
			for(BluetoothConnection connection: connections) {
				try {
					LOG.debug("Closing connection to " + connection.getDeviceAddress());
					connection.getInputStream().close();
				} catch (IOException e) {
					LOG.error("Exception while closing headset input stream", e);
				}
			}

		}
		running = false;
	}

	private void startStreaming() {
		if (null != connections && !connections.isEmpty()) {
			// Connect a packet parser to each device
			streamThreads = new ArrayList<BluetoothStreamParseThread>();
			for(BluetoothConnection connection: connections) {
				BluetoothStreamParseThread t = new BluetoothStreamParseThread(connection);
				t.start();
				streamThreads.add(t);
			}
		} else {
			LOG.debug("Stopping data stream thread");
			running = false;
		}
	}

	/**
	 * You can expect a large number of packets to fail checksum validation. You should not attempt to parse packets that fail validation.
	 * @param payload
	 * @param expectedChecksum
	 * @return
	 */
	private boolean validateChecksum(byte[] payload, byte expectedChecksum) {
		int checkSumTotal = 0;
		for (int x = 0; x < payload.length; x++) {        
		    checkSumTotal += payload[x];
		}
		
		// Ignore high bits
		checkSumTotal &= 0xFF;
		// Take one's complement
		checkSumTotal = ~checkSumTotal &0xFF;
		// Ignore high bits again for some reason
		byte checkSum = (byte)(checkSumTotal & 0xFF);
		
		if(checkSum != expectedChecksum) {
			LOG.debug(String.format("CHECKSUM FAILED! Expected checksum %02X, got checksum %02X", expectedChecksum, checkSum));
			return false;
		}
		
		return true;
	}
	
	class BluetoothStreamParseThread extends Thread {
		private BluetoothConnection connection;
		private boolean keepGoing = true;
		
		public BluetoothStreamParseThread(BluetoothConnection connection) {
			this.connection = connection;
		}
		
		BluetoothConnector bluetoothConnector;
		@Override
		public void run() {
			// If there are custom settings to be written to each headset, add them at this point
//			try {
//				connection.getOutputStreamWriter().write(MindwaveSerialPacket.Codes.DISCONNECT.getHexValue());
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
			final byte SYNC = (byte) 0xAA;
			final int MAX_PAYLOAD_LENGTH = 169;
			while (keepGoing) {
				byte b;
				int payloadLength = 0;
				try {
					// Each packet must start with two SYNC bytes in a row
					try {
						// Look for the first SYNC byte
						if ((b = connection.getInputStream().readByte()) != SYNC) {
							continue;
						}
					} catch(EOFException e) {
						// EOFException will be thrown if headset is disconnected. Try to re-connect one time.
						if(null == bluetoothConnector) bluetoothConnector = new BluetoothConnector();
						connection = bluetoothConnector.connect(connection.getDeviceAddress());
					}

					// Look for the second SYNC byte
					if ((b = connection.getInputStream().readByte()) != SYNC)
						continue;

					LOG.debug("New packet found");
					payloadLength = (connection.getInputStream().readByte() & 0xFF);

					// Ignore any packets that have a zero data length as there's no data to evaluate
					if (payloadLength == 0) {
						LOG.debug("Encountered 0-length payload; ignoring packet");
						continue;
					}

					 // Ignore any that are more than 169 bytes, which is the maximum size a payload can be
					if (payloadLength > MAX_PAYLOAD_LENGTH) {
						LOG.debug("Invalid payload length: " + payloadLength);
						continue;
					}

					// Read in the packet's payload
					LOG.debug("Payload length: " + payloadLength);
					byte[] payload = new byte[payloadLength];
					connection.getInputStream().read(payload);

					// Verify the checksum to ensure that the payload is not corrupt
					byte packetCheckSum = connection.getInputStream().readByte();
					if (!validateChecksum(payload, packetCheckSum)) {
						continue;
					}

					// Parse the packet's payload data
					List<Event> events = new MindwaveSerialPacket(connection.getDeviceAddress(), payload).getEvents();
					if (null != events && !events.isEmpty()) notifyListeners(events);
				} catch (IOException e) {
					LOG.error("Could not read stream", e);
				}
			}
			
			// If we reach this point the thread is shutting down, so try to disconnect from the headset
			try {
				BluetoothConnector.disconnect(connection);
			} catch (IOException e) {
				LOG.debug("Could not disconnect from device " + connection.getDeviceAddress() + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		public void stopThread() {
			keepGoing = false;
		}
		
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			
			if(null != connection) {
				if(null != connection.getInputStream()) {
					connection.getInputStream().close();
				}
				if(null != connection.getOutputStream()) {
					try {
						connection.getOutputStream().flush();
					} catch(IOException e) { e.printStackTrace(); }
					connection.getOutputStream().close();
				}
			}
		}
		
	}
}