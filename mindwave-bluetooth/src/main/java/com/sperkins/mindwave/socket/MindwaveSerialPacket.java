package com.sperkins.mindwave.socket;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sperkins.mindwave.event.AttentionEvent;
import com.sperkins.mindwave.event.EegEvent;
import com.sperkins.mindwave.event.Event;
import com.sperkins.mindwave.event.EventType;
import com.sperkins.mindwave.event.HeadsetStatusEvent;
import com.sperkins.mindwave.event.MeditationEvent;
import com.sperkins.mindwave.event.PoorSignalEvent;
import com.sperkins.mindwave.event.RawEvent;

public class MindwaveSerialPacket {
	private final static Logger LOG = LoggerFactory.getLogger(MindwaveSerialPacket.class.getName());
	private static final byte EXCODE = (byte) 0x55;
	private List<Event> events = new ArrayList<Event>();
	
	/**
	 * Does NOT validate checksum. Do that before creating a packet.
	 * @param rawPacket
	 */
	public MindwaveSerialPacket(String deviceAddress, byte[] payload) {
		int parsedByteCount = 0;
		while (parsedByteCount < payload.length) {
	        LOG.debug(String.format("Parsing byte %d = %02X", 
	        		parsedByteCount, payload[parsedByteCount]));
	        
	        while (payload[parsedByteCount] == EXCODE) {
	        	parsedByteCount++;
	        }
	        
	        // Ignore high bits of event type code
	        int code = payload[parsedByteCount++] & 0xFF;
	        LOG.debug(String.format("Encountered code: %02X", code));
	        byte[] data;
	        byte dataLength;
	        
	        EventType staticCode = EventType.fromHex(code);
	        if(null == staticCode) {
	        	LOG.debug(String.format("Could not parse code: %02X", code));
	        	return;
	        }
	        
	        switch (staticCode) {
	          case POOR_SIGNAL_QUALITY:
	        	  int value = payload[parsedByteCount++];
	        	  events.add(new PoorSignalEvent(deviceAddress, (int)(value & 0xFF)));
	            break;
	          case ATTENTION:
	            value = payload[parsedByteCount++];
	            events.add(new AttentionEvent(deviceAddress, value & 0xFF));
	            break;
	          case MEDITATION:
	        	  value = payload[parsedByteCount++];
		          events.add(new MeditationEvent(deviceAddress, value & 0xFF));
	            break;
	          case HEADSET_CONNECTED:
	          case HEADSET_NOT_FOUND:
	          case HEADSET_DISCONNECTED:
	          case REQUEST_DENIED:
	          case STANDBY_SCAN:
	            dataLength = payload[parsedByteCount++];
	            data = new byte[dataLength];
	            
	            for (int i = 0; i < dataLength; i++) 
	              data[i] = payload[parsedByteCount++];
	            
	            events.add(new HeadsetStatusEvent(deviceAddress, String.valueOf((data[0] << 8) + data[1]), staticCode));
	            break;
	          case SIXTEEN_BIT_RAW_WAVE:
	            dataLength = payload[parsedByteCount++];
	            int[] intData = new int[dataLength];
	            
	            for (int i = 0; i < dataLength; i++) 
	            	intData[i] = payload[parsedByteCount++];
	            
	            events.add(new RawEvent(deviceAddress, intData));
	            break;
	          case ASIC_EEG_POWER:
	            dataLength = payload[parsedByteCount++];
	            data = new byte[dataLength];
	            
	            for (int i = 0; i < dataLength; i++) 
	              data[i] = payload[parsedByteCount++];
	            
	            // Three bytes each in little-Endian format. Don't trust the documentation on this point.
	            int n = 0;
	            int[] eegValues = new int[8];
	            for (int i = 0; i < 8; i++) {
	            	eegValues[i] = data[n++];
	            	eegValues[i] += (data[n++] << 8);
	            	eegValues[i] += (data[n++] << 16);
	                
	            	eegValues[i] = Math.abs(eegValues[i] / 10000);
	              }
	            
	            events.add(new EegEvent(deviceAddress, eegValues[0], eegValues[1], eegValues[2], eegValues[3], eegValues[4], eegValues[5], eegValues[6], eegValues[7]));
	            break;
	          default:
	        	  LOG.debug("Could not find case for event type code " + code);
	            break;
	        }
	      }
	}

	public List<Event> getEvents() {
		return events;
	}

}
