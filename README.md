# mindwave-bluetooth
Bluetooth stream parser for NeuroSky Mindwave Mobile EEG headset.

Purpose
========
mindwave-bluetooth consumes the Bluetooth serial stream from one or more [NeuroSky Mindwave Mobile EEG headsets](http://amzn.to/1c9lEeU). As a headset sends data, mindwave-bluetooth raises typed events to listeners. For example, when a headset sends a new Attention value, mindwave-bluetooth raises a new AttentionEvent containing the new value and the ID (MAC address) of the headset that sent the value.

Why would you create a project like this?
======
At this time, the NeuroSky ThinkGear Connector Windows service does not provide a way to identify which headset produced data. If you have multiple headsets, you can't figure out which one produced a specific value. I needed to connect four Mindwave Mobile headsets to one Bluetooth receiver and show EEG data specific to each headset. mindwave-bluetooth is the result - a Bluetooth stream parser that allows any number of listeners to receive headset-specific events.

See [http://blog.steveperkins.info/index.php/blog/2015/05/hacking-the-mindwave-mobile-bluetooth-stream](http://blog.steveperkins.info/index.php/blog/2015/05/hacking-the-mindwave-mobile-bluetooth-stream) for more info.

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
