Parsing an RSS feed can be tricky.  Your code has to gracefully handle all sorts of strange corner cases; everything from malformed XML to an unexpected byte sequence in the feed prolog.  I recently worked on a problem that dealt with the latter: I was trying to parse an RSS feed in Java, and kept hitting an `org.xml.sax.SAXParseException: Content is not allowed in prolog`.  The prolog is anything before the opening `<?xml` tag at the start of the feed.  I dug into it a little further, and discovered that [many UTF-8 encoded files include a three-byte UTF-8 Byte-order mark](http://en.wikipedia.org/wiki/Byte_Order_Mark).  When dealing with a UTF-8 encoded RSS feed, this three-byte pattern (`0xEF 0xBB 0xBF`) in the prolog can cause all sorts of interesting XML parsing problems, including a `SAXParseException: Content is not allowed in prolog`.

One solution is to use a quick-and-dirty regular expression to cleanup the XML prolog before feeding it into a parser.

First, I wanted to confirm my suspicion about the UTF-8 Byte-order mark.  I used `wget` to download the feed in question [http://www.hp.com/hpinfo/stories.xml](http://www.hp.com/hpinfo/stories.xml) and opened it up using `khexedit`.  Sure enough, the first three bytes are `EF BB BF`:

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/resolving-orgxmlsaxsaxparseexception-content-is-not-allowed-in-prolog/rss-feed-extra-bytes.png">

Because these extra three bytes are present in the prolog, you might see an exception that looks something like this when trying to parse the XML:

```
Caused by: org.jdom.input.JDOMParseException: Error on line 1:
                      Content is not allowed in prolog.
     at org.jdom.input.SAXBuilder.build(SAXBuilder.java:468)
     at org.jdom.input.SAXBuilder.build(SAXBuilder.java:851)
     at com.sun.syndication.io.WireFeedInput.build(WireFeedInput.java:178)
     ... 188 more
Caused by: org.xml.sax.SAXParseException: Content is not allowed in prolog.
     ... 190 more
```

As mentioned, a quick-and-dirty solution to this problem is to build a regular expression to strip off any junk in the prolog before feeding the XML into a parser.  Here's an example that strips off any non-word characters in the prolog:

```java
String xml = "<?xml ...";
Matcher junkMatcher = (Pattern.compile("^([\\W]+)<")).matcher( xml.trim() );
xml = junkMatcher.replaceFirst("<");
```

As of Java 1.4, you could also try something a little cleaner:

```java
String xml = "<?xml ...";
xml = xml.trim().replaceFirst("^([\\W]+)<","<");
```

Note that calling `String.trim()` on the XML isn't good enough, because `trim()` only handles leading and trailing white space.  Once I got rid of the UTF-8 Byte-order mark, my XML parser handled the feed with no issues.

<!--- tags: java, xml -->