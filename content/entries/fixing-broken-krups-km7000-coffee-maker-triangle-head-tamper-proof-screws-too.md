In 2008, my wife bought me a Krups KM7000 Grind-and-Brew Coffee Maker for Christmas.  I love it, it grinds and brews an incredible pot of high quality coffee for me each morning.  This past weekend (about 15-months since I got it), my KM7000 unexpectedly stopped brewing mid-cycle.  And to make matters worse, the one-year manufacturers warranty period already expired.  Well, I wasn't about to let this one get away, so I decided to open it up and give it a look-see.  After all, shouldn't a high-end coffee maker last a little longer than just over a year?

With a little effort and some engineering know-how, I found the troublemaker part and repaired my coffee maker.  My KM7000 is as good as new.  Here's how I did it.

### Symptoms

The symptoms of my failing KM7000 were pretty straightforward.  Basically everything on the machine worked great, except the hot plate would not get hot nor would the water pump from the reservoir into the brew-pot.  The grinder worked perfectly, the LCD display was completely functional, but the machine just wouldn't get hot and wouldn't brew any coffee.  Further, when I started a brew-cycle, I could hear a little "click" inside the machine (this is normal) but the coffee maker just sat there doing nothing.

After doing a little research online, I discovered that this is a [very common problem](http://www.jimmysu.net/component/content/article/1-latest/971-oh-the-coffeemaker-is-broken-again) with the KM7000.  Apparently, this unit is well known for dying just past the one-year warranty period.  Interesting.  Time to open this puppy up for a little surgery.

**NOTE: My KM7000 is out of warranty, and could not be replaced.  If you have a KM7000 that's still under warranty, and it's exhibiting the symptoms I described above, you're better off calling Krups for a new replacement unit.  If you open your KM7000, and it's still under warranty, I can say with much certainty that Krups won't give you a free replacement.  Better not risk it.**

**DISCLAIMER: If you attempt to self-fix your KM7000 coffee maker, you should know what you're doing.  If you follow this blog post and destroy your KM7000 or set your house on fire, you should know that I'm in no way responsible.  If you don't feel comfortable doing this by yourself, find a friend or family member who is more mechanically or electrically inclined to help you.**

### Triangle Bit Tamper Proof Screws

I took the coffee maker out to my workbench, and flipped it over (making sure that I poured out all of the water, and emptied the coffee bean bin).  Using a standard Phillips head screwdriver, I removed all but one screw from the underside of the unit.  As it turned out, I discovered that I could not remove one of the screws because it had a non-standard tamper-proof, triangle shaped inset.  Note that the head was round, but the inset/bit was literally an equilateral triangle.  In other words, this was no standard screw.  Krups obviously put this screw in place to prevent people like me from trying to open the unit.  Screw that!  I tried to loosen this screw using a tiny flat-head screwdriver, being careful not to strip the head.  Unfortunately, it was too tight and I gave up fearing that I might strip the head making it impossible for me to remove the screw altogether.

First shot, I called a few local hardware stores looking for a triangle shaped replaceable bit or screwdriver, but they never heard of this type of tamper-proof screw.  I checked online, and found a few places that sell triangle shaped machine bits, but I wasn't about to pay a few bucks plus $10 USD shipping for a screwdriver bit I'll really only use once.  Plus, the online stores I found had 4 sizes of triangular bits so I really had no idea what size I should order, and didn't want to risk wasting my money.

For the record, I found these bits at [McMaster-Carr online](http://www.mcmaster.com/#triangle-screwdriver-bits/=6ce2zm):

<img src="static/entries/fixing-broken-krups-km7000-coffee-maker-triangle-head-tamper-proof-screws-too/triangle-bit.gif">

If you're curious, here's a close up shot of a standard Phillips head (left) and triangular bit tamper-proof screw (right):

<img src="static/entries/fixing-broken-krups-km7000-coffee-maker-triangle-head-tamper-proof-screws-too/phillips-and-triangle-tamper-proof-screw.jpg">

In the end, my solution to this problem involved an old flat-head screwdriver, a vice and metal file.  I snagged an old beat-up flat-head screwdriver, locked it into my vice, and filed the tip into a triangular point.  About 10-minutes later, with a little trial and error, I had myself a pretty sweet triangular screwdriver bit that could easily handle these ridiculous tamper-proof screws!

### Inside the Machine

Once I removed all of the screws from the underside of the unit, the hard plastic bottom slid right out.  Note that there are more "hidden" screws under the little rubber feet; this wasn't immediately obvious to me.  You'll have to pop the rubber plugs out of their sockets to reveal about 5-6 more screws.

The internals of the KM7000 seem pretty basic.  There's a length of flexible orange tubing that connects the water reservoir to the heating coil (and the hot plate!), then upwards to the brew-pot.  A tiny circuit board connects the LCD display to the rest of the unit.  So, as I see it, here's how the machine brews coffee.  Water from the reservoir flows through the orange tubing into the heating ring, connected to the hot-plate with a little thermal adhesive.  From there, the heating element heats the water which forces it upwards through the rest of the orange tubing and into the brew-pot.  Remember, the symptoms of my problem were that the hot-plate would not get hot, nor would the machine brew any coffee.  Well, the heating coil boils the water as it passes through the tube which propels it upwards into the brew-pot.  And, because the heating coil is connected to the underside of the hot plate, the coil warms the hot-plate while it boils the water!  In other words, if the machine isn't boiling any water then the hot plate ain't gonna be hot, and vice versa.  So clearly, the problem was with the heating coil itself or the circuit that powers the heating coil.

### Problematic Thermal Fuses

With a little research, I found [this great blog post](http://www.jimmysu.net/component/content/article/1-latest/971-oh-the-coffeemaker-is-broken-again) describing the same problem, and a do-it-yourself fix.  Turns out there are dual inline one-shot thermal fuses on the circuit that connects the heating coil to the rest of the unit.  Both are Therm-o-disc G4 series Microtemp axial leaded fuses; one is rated up to 216C (~420F), the other is rated to 240C (~464F).  The specs and other details are clearly printed on the fuses themselves.  Based on what others reported elsewhere, and the symptoms of my failing KM7000, these fuses were highly suspect.

At first, I had difficulty discovering the location of the thermal fuses.  As it turns out, they are assembled together in series, hidden under a white plastic sheath on the circuit leading to/from the heating element.  Gently side the white sheath away to expose the fuses.

Using a multimeter, I checked the resistance across both fuses to see which one had bit the dust.  Theoretically speaking, the resistance across an open circuit should be infinite, so my multimeter helped me pinpoint the problematic fuse.  Turns out, the 240C fuse had blown wide open; but the 216C fuse was perfectly in tact.  One would expect that if the unit really was overheating because of an electrical malfunction, the 216C fuse would have blown first given that it's built to trigger at a lower temperature.  In any event, I strongly agree with [Jimmy Su, that these fuses probably aren't made all that well and eventually fail](http://www.jimmysu.net/component/content/article/1-latest/971-oh-the-coffeemaker-is-broken-again) under normal operating conditions.  One would think that a high-end appliance manufacturer wouldn't cheap-out on basic components.  Especially on a one-shot fuse; if it blows, a $150 coffee maker is rendered useless by a $1 part.

To further prove to myself that the 240C thermal fuse was the source of the problem, I used a pair of alligator clips to bypass the fuse all together.  With alligator clips in place on my workbench, I turned the machine on and sure enough the heating coil powered right up, and got hot very quickly.  So, the 240C thermal fuse was definitely the problem!

Looking for a few replacement fuses, Jimmy kindly pointed me to [goodmans.net](http://goodmans.net) where I ordered three [216C](http://www.goodmans.net/get_item_th-tf216c_thermal-fuse-216-degrees-celsius.htm) and three [240C](http://www.goodmans.net/get_item_th-tf240c_thermal-fuse-240-degrees-celsius.htm) thermal fuses.  I ordered three of each just in case I ruined one or two during the repair or needed more to fix a another blown fuse down the road.  In total, the six replacement fuses plus expedited shipping came out to just over 14-bucks.  Not bad at all.  You might ask why didn't I just drive down to the local Radio Shack?  Well, smart guy, turns out Radio Shack does not carry these type of specialty fuses.  I'm sure I could have hunted something down at a local electronics parts supplier, but I had no interest in wasting my time.  In any event, the exact manufacturer part numbers of the replacement fuses I ordered online are as follows:

* Therm-O-Disc G4A01216C (Microtemp G4 Series, Axial Leaded)
* Therm-O-Disc G4A01240C (Microtemp G4 Series, Axial Leaded)

Once my replacement fuses arrived, I grabbed the closest soldering gun and got down to business.  Even though I didn't need to, I opted to replace both fuses.  I felt like that's a safer bet than replacing one, only to find out I need to open up the unit again a month later to replace the other fuse.  Note that if you **opt to solder in the replacement fuses, you should remember to heat sink the fuses so the heat of the soldering gun doesn't accidentally trip one**.  Placing a few alligator clips on each end of the fuse works nicely by helping to direct some heat away from the fuse itself.

I opted to use a few simple butt-connectors between the fuses, and the main line:

<img src="static/entries/fixing-broken-krups-km7000-coffee-maker-triangle-head-tamper-proof-screws-too/km7000-replacement-fuses-butt-connectors.jpg">

With both fuses replaced, I verified each with a multimeter and carefully reassembled my KM7000.  I dusted it off, brought it back into my kitchen, plugged it in, and brewed up one heck of a great tasting pot of coffee.

<img src="static/entries/fixing-broken-krups-km7000-coffee-maker-triangle-head-tamper-proof-screws-too/km7000-fixed-victory-is-mine.jpg">

That's the smell of delicious do-it-yourself satisfaction.  Cheers!

### Additional Resources

* [https://onyx.koli.ch/x3bn] &mdash; Complete set of photos from my repair procedure, including more pictures of those lame triangle bit tamper-proof screws.
* [https://onyx.koli.ch/x3c2] &mdash; Fantastic blog post from Jimmy Su which helped me discover the problem with my KM7000.
* [https://onyx.koli.ch/x14m] &mdash; My archived digital copy of the Krups KM7000 User-Manual.
* [https://onyx.koli.ch/x3c3] &mdash; A PDF spec of the G4 fuse from Therm-O-Disc, the manufacturer of the thermal fuses used in the KM7000.

Very special thanks to [Jimmy Su](http://www.jimmysu.net/component/content/article/1-latest/971-oh-the-coffeemaker-is-broken-again) for all of his help and advice along the way.  Thanks, Jimmy!