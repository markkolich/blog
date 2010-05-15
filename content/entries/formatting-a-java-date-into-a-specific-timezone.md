Date objects in Java, and probably most other robust languages, simply represent a snapshot of a point in time.  In other words, [java.util.Date](http://java.sun.com/javase/6/docs/api/java/util/Date.html) knows nothing about the time zone you're referring to when instantiating or manipulating a Date object.  Fact is, java.util.Date does not have to care about your time zone, because internally a Date is really nothing more than a count of the number of milliseconds since the standard base time known as ["the epoch"](http://en.wikipedia.org/wiki/Unix_time), namely January 1, 1970, 00:00:00 GMT.

If it helps, think about it this way: X milliseconds since the epoch is X milliseconds since the epoch in the `US-Pacific` time zone, X milliseconds since the epoch in GMT-0 (London), X milliseconds since the epoch in India, etc.  In short, when it's 1273947282085 milliseconds since the epoch, it's 1273947282085 milliseconds since the epoch everywhere in the world at the same time regardless of what time zone you're sitting in.  And since Java's `util.Date` is simply a snapshot of the number of milliseconds at a specific point in time, you can see why Date doesn't care about your time zone.  It's irrelevant.

But, how do I convert a `java.util.Date` into a different time zone?  You can't, and that question makes no sense.  That's like asking me to "take a picture of the sound."  Here's some crap code that you should not use, but I've put it here for illustrative purposes:

```java
// Do NOT use this, it does nothing and makes no sense.
public static final Date convertIntoTimeZone(Date date, TimeZone tz) {
  final Calendar cal = Calendar.getInstance();
  cal.setTime(date);
  cal.setTimeZone(tz);
  return cal.getTime();
}
```

You can't convert a `Date` into a different time zone, but you can use Java's handy [DateFormat](http://java.sun.com/javase/6/docs/api/java/text/DateFormat.html) class to format a `Date` into the time zone of your choice.  To put it differently, let `Date` do its thing &mdash; then, when you're ready to display or print out a `String` representation of `Date`, that's when you tell DateFormat what time zone you want it in.  So, here's some code that makes sense, and actually works:

```java
final Date currentTime = new Date();

final SimpleDateFormat sdf =
        new SimpleDateFormat("EEE, MMM d, yyyy hh:mm:ss a z");

// Give it to me in US-Pacific time.
sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
System.out.println("US-Pacific time: " + sdf.format(currentTime));

// Give it to me in GMT-0 time.
sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
System.out.println("GMT time: " + sdf.format(currentTime));

// Or maybe Zagreb local time.
sdf.setTimeZone(TimeZone.getTimeZone("Europe/Zagreb"));
System.out.println("Zagreb time: " + sdf.format(currentTime));

// Even 10 hours and 10 minutes ahead of GMT
sdf.setTimeZone(TimeZone.getTimeZone("GMT+0010"));
System.out.println("10/10 ahead time: " + sdf.format(currentTime));
```

Cheers.