This past weekend, heavy wind knocked out the power to my home for over an hour or so.  Unfortunately, the Linux server that powers my blog failed to stay on during the outage.  Come to find out, the UPS (uninterruptible power supply, a.k.a., a battery backup) unit it was plugged into is pretty much dead; I suspect the battery is completely trashed.  Once the power returned, I rebooted my server and decided to test the UPS.  I booted up the box, and pulled the plug out from the wall.  The UPS kept my system online for all of about 4-seconds.  So much for being uninterruptible.

<img src="static/entries/picking-the-right-ups-battery-backup-and-figuring-out-how-much-it-costs-to-self-host/ammeter-xw8000.jpg" width="500">

### Gathering the Data

First, I needed to figure out how much current my server actually consumed.  The beast has a 450 W max power supply.  Chances are, at any given moment, I'm not using all 450 W.  And, as it turns out, I don't even come close to maxing out the PSU.  I asked around at work, and found a nice guy who was willing to let me borrow a reliable [ammeter](http://en.wikipedia.org/wiki/Ammeter) (a Fluke 36 Clamp Meter).  This specific ammeter uses [magnetic inductance](http://en.wikipedia.org/wiki/Magnetic_inductance) to determine the amount of current ([amperes](http://en.wikipedia.org/wiki/Ampere)) flowing through the circuit.

Initially, I took the ammeter and simply clamped it around the untouched power cable connecting the UPS to the server.  Surprisingly, the meter showed that the circuit had a load of less than half an amp.  I knew that wasn't right, and so I did a little more digging.  Obviously, physics is heavily involved here.  As current flows through each wire in the twisted pair, it creates a slight magnetic field around the wire.  When the wires are twisted together, their magnetic fields essentially cancel each other out.  This is the way it's supposed to work.  Hence, the ammeter produced a tiny (almost zero) reading.

As it turns out, I had to carefully strip off the outer shell/insulation of the power cable to expose the individual wires.  I used a small wire cutter to strip off the outer shell, and peeled back about 1.5 ft of insulation (as shown below).

<img src="static/entries/picking-the-right-ups-battery-backup-and-figuring-out-how-much-it-costs-to-self-host/ammeter-stripped-cable.jpg" width="500">

With each individual wire in the twisted pair exposed, I was able to separate the wires (white/black=current, green=ground) and wrap the ammeter around them one at a time.  With the wires separated, I got a much more accurate reading of about **1.8 to 2.2 A**.

Below you can see the before and after readings on the ammeter.  When the wires are close together the ammeter shows an almost zero reading.  When they are held apart (2-3 inches from one another), I see more reliable numbers:

<img src="static/entries/picking-the-right-ups-battery-backup-and-figuring-out-how-much-it-costs-to-self-host/ammeter-before-after.jpg" width="500">

A great friend of mine (a physics guy) shared some insight on what's going on here:

"...what you are observing is a result of the **current in the adjacent wires not going through the inductor loop**.  In theory there might be a slightly different induced EMF in the side of the clamp that is closer to the external wire than the other side resulting in a tiny variation in your readings.  However, the magnetic field produced from a wire drops off as the inverse (not squared) of the distance from the wire.  The effect of the (opposite-sides-of-the-)clamp-to-adjacent wires distance drops off rather rapidly.  The difference between the magnetic fields as it applies to the two sides of the clamp that are parallel to the external wire is measurable but probably not significant compared to the effect of the wire inside the loop.  Smaller clamps result in better reading.  More distance from the external wire will result in a better reading.  You are, however comparing x (distance from wired to clamp) with delta (the diameter of the clamp) according to 1/x vs 1/(x+delta).  Vary quickly delta becomes insignificant and you can cancel out the effects of the external wire.  If the clamp had a diameter of 2 cm and the distance to the clamp was 6 cm you would be comparing 1/6 to 1/8 for about a 4% variance in what your true reading should be.  Since your meter is only showing one digit to the right of the decimal you can assume that there is no measurable effect."

### Analysis

Based on my data, I know that my Linux server consumes about 2.0 A on average.  Just for grins, I ran a few disk intensive tests on the system and saw the current flow jump to about 2.2 A with heavy disk activity.  Idling, after being on for 10-15 minutes, the current flow went as low as 1.8 A.

For my calculations, I'm going to assume the system uses roughly 2.0 A when idling.  Using some basic physics, let's calculate how many W (watts) this system consumes knowing that it's plugged into a standard ~120V outlet:

<img src="static/entries/picking-the-right-ups-battery-backup-and-figuring-out-how-much-it-costs-to-self-host/ammeter-wattage.png">

So, my Linux box uses about 240 W of power on average.  Not bad &mdash; no where near the max of 450 W the power supply can dish out though.

**As a side note, voltages are known to fluctuate between 110V and 120V.  So, theoretically, the total wattage could be anywhere from 220 W to 240 W.  For my purposes, I'll assume the worst case scenario: the wall outlet will always deliver a constant 120V.**

## Conclusion

Knowing the wattage is critical because I can now make a more informed purchase.  The unit I actually purchased is an [APC BR1500LCD 1500VA @ 865 Watts]"(http://www.amazon.com/gp/product/B000NDA5E0).  From what I can tell, the **BR1500LCD** will be more than enough to power my HP xw8200 Linux server for almost 30-minutes at an idle 240 W!