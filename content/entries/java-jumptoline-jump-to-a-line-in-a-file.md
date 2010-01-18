Seeking to a line number in a text file isn't too hard to implement in Java if you use a few common and trusted API's like [Apache's Commons I/O library](http://commons.apache.org/io/).  Recently I needed some Java that could automatically seek to a given line in a file and then remember the line number of the last line read.  The next time I open the log reader, it should automatically seek itself to the last line read, and let me read any subsequent lines added by another user or process.  This is perfect for log file monitoring: the first invocation of the reader would read lines 1 through X and the next invocation would read lines X+1 through Y, and so on.  Using Apache's Commons I/O API, this isn't difficult at all.  You may or may not know that the Commons I/O API contains a very convenient [LineIterator](http://commons.apache.org/proper/commons-io/javadocs/api-2.4/org/apache/commons/io/LineIterator.html) class, which lets the developer iterate over lines in a file using a [Reader](http://java.sun.com/javase/6/docs/api/java/io/Reader.html).

With that in mind, meet [JumpToLine](https://github.com/markkolich/kolich-common/blob/master/src/main/java/com/kolich/common/util/io/JumpToLine.java), a somewhat hackish class I wrote that wraps Apache's Commons I/O LineIterator in a way that lets you **seek ahead** to a specific line in a file, and is smart enough to remember the last line read (so that you don't read the same line twice).

### Example #1

Here's how you might use `JumpToLine` to seek to line 10 in `mylog.log`, then read that line and every line after it:

```java
final JumpToLine jtl = new JumpToLine(new File("mylog.log"));

try {
  // Open the underlying reader and LineIterator.
  jtl.open();
  // Seek to line 10; will throw a NoSuchElementException if
  // out of range.
  jtl.seek(10);
  // While there are any lines after and including line 10,
  // read them.
  while(jtl.hasNext()) {
    final String line = jtl.readLine();
    System.out.println(line);
  }
} catch (Exception e) {
  e.printStackTrace(System.err);
} finally {
  // Close the underlying reader and LineIterator.
  jtl.close();
}
```

### Example #2

Here's how you might use `JumpToLine` to seek to the last line read in `mylog.log`, and then read any subsequent lines added to the file since it was last read:

```java
final JumpToLine jtl = new JumpToLine(new File("mylog.log"));

try {
  // Open the underlying reader and LineIterator.
  jtl.open();
  // Seek to the last line read since we last tried to
  // read any lines from this file.
  jtl.seek();
  // While there are any more lines to read from the last
  // line read position, then read them.
  while(jtl.hasNext()) {
    final String line = jtl.readLine();
    System.out.println(line);
  }
  // For grins, what is the last line number we read?
  System.out.println("Last line number read: " + jtl.getLastLineRead());
} catch (Exception e) {
  e.printStackTrace(System.err);
} finally {
  // Close the underlying reader and LineIterator.
  jtl.close();
}
```

`JumpToLine` is now formally part of my `kolich-common` Java library [available on Github](https://github.com/markkolich/kolich-common).