# mindwave-bluetooth
Bluetooth stream parser for NeuroSky Mindwave Mobile EEG headset.

Purpose
========
mindwave-bluetooth consumes the Bluetooth serial stream from one or more [NeuroSky Mindwave Mobile EEG headsets](http://amzn.to/1c9lEeU). As a headset sends data, mindwave-bluetooth raises typed events to listeners. For example, when a headset sends a new Attention value, mindwave-bluetooth raises a new AttentionEvent containing the new value and the ID (MAC address) of the headset that sent the value.

Example
========
```java
BluetoothSocket socket = new BluetoothSocket(new MindwaveEventListener() {
		
		  @Override
		  public void onEvent(Event event) {
			
			  // Put your code here
			  // You can check which headset generated this event with event.getDeviceAddress(), which returns the headset's Bluetooth MAC address
			  // You can determine event type with event.getEventType()
			  // You can cast to a specific event class to access more information
			  if(EventType.ATTENTION.equals(event.getEventType())) {
				  AttentionEvent attentionEvent = (AttentionEvent)event;
				  System.out.println(attentionEvent.getValue()); // Prints the headset's Attention percentage from 0 to 100
			  } else if(EventType.MEDITATION.equals(event.getEventType())) {
				  MeditationEvent meditationEvent = (MeditationEvent)event;
				  System.out.println(meditationEvent.getValue()); // Prints the headset's Meditation percentage from 0 to 100
			  } else if(EventType.HEADSET_CONNECTED.equals(event.getEventType())) {
			  	System.out.println("Headset connected");
			  } else if(EventType.EIGHT_BIT_RAW_WAVE.equals(event.getEventType())) {
				  RawEvent rawEvent = (RawEvent)event;
				  System.out.println(rawEvent.getValues()); // rawEvent.getValues() returns an array containing an int value for each brain wave
			  } else if(EventType.POOR_SIGNAL_QUALITY.equals(event.getEventType())) {
				  System.out.println("Headset signal is too degraded to read"); // This event is fired when the headset connection to the user's forehead/ear is too nondeterministic to adequately calculate EEG and Attention/Meditation values
			  }
			
		  }
	  }).start();
```

You can also use helper methods to manage headset connections in a more granular way:
```java
BluetoothSocket socket = new BluetoothSocket();
socket.addListener(...);
socket.start(); // attempts to connect to all previously-paired headsets in range, begins streaming data, and begins raising data events for all headsets
socket.start("091A2B3C4D5E"); // attempts to connect to the headset with the MAC address 091A2B3C4D5E
socket.stop("091A2B3C4D5E"); // disconnects from the headset with the MAC address 091A2B3C4D5E
socket.stop(); // disconnects from all connected headsets
socket.getConnectionByHeadsetId("091A2B3C4D5E"); // tries to find an established connection for the MAC address 091A2B3C4D5E. If found, returns a BluetoothConnection object that gets you access to the raw input and output streams for this headset.
``` 
	
Dependencies
======
mindwave-bluetooth requires a Bluetooth driver to connect to Bluetooth devices. The most stable Java driver I could find was BlueCove, which hasn't been maintained for a long time but purportedly chooses the platform-appropriate Bluetooth stack automatically. Although you can theoretically use any standard Bluetooth driver you want, the BlueCove jars are provided for you in the lib folder:
- bluecove-2.1.1-SNAPSHOT.jar
- bluecove-bluez-2.1.1-SNAPSHOT.jar
- bluecove-gpl-2.1.1-SNAPSHOT.jar

What Bluetooth adapters can I use with the NeuroSky Mindwave headsets?
======
After trying out a bunch of external dongles purchased for that purpose, and others from past projects, I found the most stable connection to be from my MacBook Pro's internal Bluetooth receiver.

In case you'd rather not go into hock just to buy a computer to connect to the headsets, NeuroSky suggests these USB adapters (http://support.neurosky.com/kb/store/where-can-i-purchase-a-bluetooth-dongle):
IOGEAR GBU521, $12 (http://www.amazon.com/IOGEAR-Bluetooth-Micro-Adapter-GBU521/dp/B007GFX0PY)
The drivers are available from the IOGEAR website but are 139MB, 200MB, and 235MB (ouch! https://www.iogear.com/support/dm/driver/GBU521#display). Windows may already have a compatible driver that's not enormous.


Why would you create a project like this?
======
At this time, the NeuroSky ThinkGear Connector Windows service does not provide a way to identify which headset produced data. If you have multiple headsets, you can't figure out which one produced a specific value. I needed to connect four Mindwave Mobile headsets to one Bluetooth receiver and show EEG data specific to each headset. mindwave-bluetooth is the result - a Bluetooth stream parser that allows any number of listeners to receive headset-specific events.

See [http://steveperkins.info/projects.html](http://steveperkins.info/projects.html) for more info.

What events does mindwave-bluetooth raise?
======
* Attention (calculated by the headset using a proprietary formula)
* Meditation (same as above)
* Poor Signal (happens a lot)
* EEG (delta, theta, low alpha, high alpha, low beta, high beta, low gamma, and mid gamma wave readings)
* Raw (8-byte EEG readings)

mindwave-bluetooth does NOT produce "blink" events. Those are calculated by the ThinkGear Connector service, not by the headsets themselves, so they're not available to anything just reading the Bluetooth stream.

How are events raised?
=====
In my tests, each headset sends Attention, Meditation, and 16-byte EEG values as one packet about every second. Raw EEG data is sent almost constantly, so if you listen for RawEvents you will see a LOT of activity.

With four Mindwave Mobile headsets connected and actively streaming, the performance dropped slightly - about one Attention/Meditation/16-byte EEG packet every 1.1 to 1.2 seconds. To be fair, this was not an isolated test. The four headsets were connected and streaming to a UI while another listener pushed the same data up to Microsoft Power BI. You may see slightly better performance running standalone or standalone with a UI.

I want to use the Mindwave Mobile and mindwave-bluetooth on Android. Will it work?
=====
Yep. I wrote an Android consumer with mindwave-bluetooth and it showed the same performance as the desktop consumer. NeuroSky now offers an Android-specific SDK (iOS and Mac OS are no longer left out either) you might check out as well. Either way, the one-second resolution appears to be a limitation of the data each Mindwave Mobile is sending rather than the speed of the connection or the code used to read the connection, so if performance is your interest, a library that reads the stream directly will probably be very slightly faster than anything else.

License
====
This project's license is copyleft non-commercial with attribution, so as long as you're not selling a product that uses the library and you attribute the use of the library back to the GitHub repo or me personally (http://steveperkins.info), you have a perpetual license to use it.
