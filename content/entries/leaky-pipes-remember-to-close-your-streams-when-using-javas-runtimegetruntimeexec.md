For more than a year, I got away with forgetting to close my standard I/O streams when spawning a process in Java with Runtime.getRuntime().exec().  On Linux, I was using exec() to spawn the df command to check my file system disk space usage.  Standard out from df was piped into the parent (Java) where I parsed the output to see if any partitions were getting full.  Simple enough, right?

In December 2010, I began experimenting with Java's next generation garbage collection engine, aptly named G1 (a.k.a., Garbage First).  Assuming you have Java 6 Update 14 or later you can enable the next-generation G1 garbage collector (still experimental as of Jan 2011) using the following JVM options:

```
-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC
```

This post isn't about G1, so I'm not going to dig into the nitty-gritty on garbage collection.  However, I discovered that G1 isn't as aggressive as the current Java garbage collector with regards to cleaning up streams.  Bug in G1?  Maybe, maybe not.  Regardless, my app ran for a week or two with G1 enabled then I started to see all sorts of silly java.net.SocketException's claiming I had "Too many open files".  Using the trusty `lsof` command, I saw that my Java process had left open a ton of stranded pipes.  Definitely an indication of a leak somewhere ...

```
#/> lsof -p 23064 | grep pipe
...
java    23064 mark  996w  FIFO    0,7    152581 pipe
java    23064 mark  997r  FIFO    0,7    152309 pipe
java    23064 mark  998r  FIFO    0,7    152448 pipe
java    23064 mark  999w  FIFO    0,7    152720 pipe
java    23064 mark 1000w  FIFO    0,7    152859 pipe
java    23064 mark 1001r  FIFO    0,7    152583 pipe
java    23064 mark 1002w  FIFO    0,7    153134 pipe
java    23064 mark 1003r  FIFO    0,7    152722 pipe
java    23064 mark 1004w  FIFO    0,7    154801 pipe
java    23064 mark 1005r  FIFO    0,7    152861 pipe
java    23064 mark 1006w  FIFO    0,7    152997 pipe
java    23064 mark 1007w  FIFO    0,7    153564 pipe
java    23064 mark 1008r  FIFO    0,7    152999 pipe
java    23064 mark 1009r  FIFO    0,7    153136 pipe
java    23064 mark 1010r  FIFO    0,7    153278 pipe
java    23064 mark 1011w  FIFO    0,7    153406 pipe
java    23064 mark 1012w  FIFO    0,7    153713 pipe
...
```

With a little persistence, I crawled through my code looking for any obvious problem spots &mdash; places where I forgot to close a stream &mdash; and discovered that my calls to exec() were problematic.  Calling exec() returns a Process object for the child where all standard I/O ops are redirected to the parent through three streams: `STDOUT`, `STDIN`, `STDERR`.  It turns out you have to explicitly close these streams when you're done with the child otherwise they are left open!  And, as you can see in the lsof output above, I was not closing these streams causing a nasty leak which eventually brought down my application.

Going back to differences in the garbage collectors, it seems that the current default garbage collector cleaned up after my mess (closed the streams for me), but G1 did not.  Hence why I never saw the "Too many open files" exception until I enabled G1.

That said, the undocumented proper way of handing a Process object and its corresponding I/O streams is to wrap the exec() call in a try-finally block, closing the `STDOUT`, `STDIN`, and `STDERR` streams when you're done with the Process object.  The abstract class java.lang.Process exposes these three streams to you via [getOutputStream()](http://download.oracle.com/javase/6/docs/api/java/lang/Process.html#getOutputStream%28%29), [getInputStream()](http://download.oracle.com/javase/6/docs/api/java/lang/Process.html#getInputStream%28%29) and [getErrorStream()](http://download.oracle.com/javase/6/docs/api/java/lang/Process.html#getErrorStream%28%29) which you must explicitly close.

Here's the pseudo code:

```java
import static org.apache.commons.io.IOUtils.closeQuietly;

Process p = null;
try {
  p = Runtime.getRuntime().exec(...);
  // Do something with p.
} finally {
  if(p != null) {
    closeQuietly(p.getOutputStream());
    closeQuietly(p.getInputStream());
    closeQuietly(p.getErrorStream());
  }
}
```
Note that [closeQuietly()](http://commons.apache.org/io/api-1.2/org/apache/commons/io/IOUtils.html) is part of the Apache Commons IOUtils library &mdash; it's a helper method to close a stream ignoring nulls and exceptions.  With this change in place, I redeployed my app and sure enough the problem was resolved.

Lesson learned: regardless of what garbage collector you're using, it's always a good idea to explicitly close the `STDOUT`, `STDIN`, and `STDERR` streams associated with a Process object when you are done with it.

Enjoy.
