[Clok](http://clok.koli.ch) is a "very fuzzy" world-clock I built, modeled after Caskey Dickson's idea and Java Clok implementation.  Clok is not built to give you the exact time in every time zone; for that, get another, more accurate, world clock.  Instead, [my version of Clok](http://clok.koli.ch) is used to help me answer basic time related questions, like:

* Several of my co-workers are based in London; can I email them at the office, or are they at home?
* I need to call my wife, but she's in Manila visiting family.  Is it daytime there?
* Some interesting world news just broke in Dubai; roughly what time is there?

These questions are all a slight variation of the timeless (pun intended), "what time is it?"  The latest version of Clok can be found at http://clok.koli.ch.

###  How Do I Read this Clok?

Each column represents a single hour, in a 24-hour day.  Each hour is color coded, according to the various pieces, or periods, of a typical day.  As explained here, "sleeping is obviously the least productive and so that is represented in black. The morning is the time between sleeping and lunch, lunch is a time of recovery and planning for the rest of the day. Then there is the afternoon, which for many is the time when the majority of their work gets done. Finally comes evening, which is personal time, time spent with family, or time taking care of those responsibilities that have to do with running our lives and not earning a paycheck."  The colors of each hour in the day directly correspond to these periods.

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/clok-a-new-way-to-view-time/clok-main-snap.jpg">

Each row is a different time zone, modeled after [this list on Wikipedia](http://en.wikipedia.org/wiki/Time_zone#Standard_time_zones).  And finally, the red line is the approximate current time in that time zone.

Clok automatically updates itself every minute while open; no browser refresh is required.

### The User Interface

You may have noticed that when you mouseover each row on the Clok, four tiny buttons appear on the far right of each time zone.  These represent the various modes: H for human, K for hacker/developer/coder, S for server, and R for raccoon (nocturnal).  When clicked, these buttons change the mode of Clok.  If you're a relatively normal individual with decent sleeping habits, you will find Human mode most useful.  If you, or a friend across the world participates in a more nocturnal lifestyle, then you might enjoy Raccoon mode.

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/clok-a-new-way-to-view-time/clok-mode-snap.jpg">

If you find the time zone annotations annoying, and would like to hide them, click anywhere in the orange header bar and the name/UTC ID of each time zone should disappear.

### Technical Details

My version of Clok is pure CSS, JavaScript, and PHP (no Flash).  When the DOM is ready, AJAX is used behind the scenes to call a PHP controller on the server, which actually does the work of computing the time in every requested time zone.  This controller returns a block of JSON that maps a time zone to the position of its red "current time" bar.  The JavaScript loops over these JSON objects in the response, and uses [jQuery to move (animate)](http://docs.jquery.com/Effects/animate) the red bars into position.  To stay current, an interval fires once every 60,000 ms (1 minute) which triggers Clok to refresh itself.

### Where Can I Find this Clok?

http://clok.koli.ch

Enjoy.