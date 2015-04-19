Yesterday, I realized the importance of setting a properly computed `Content-Length` header in your HTTP response.

You may know that the `Content-Length` header is the length of the response body in octets (8-bit bytes), and not the number of characters.  In Java, I was mistakenly computing the `Content-Length` using the `String.length()` method, which returns a count of the number of characters in the String (assuming each character was only one-byte).  Well, in many cases, using the `String` length as the `Content-Length` is entirely wrong, especially when dealing with UTF-8 encoded characters in your `String`.

UTF-8 encodes each character (code point) in 1 to 4 octets (8-bit bytes).  Meaning that if you have UTF-8 encoded characters in your `String`, then those characters may use more than a single byte to represent themselves.  But, when you call `String.length()`, you're only going to get back the number of characters, not the number of bytes used to represent those characters (e.g., what the HTTP `Content-Length` header needs).

So, here's the situation: I was working with some XML, where one of the entries happened to contain a special [registered trademark (r) symbol](http://en.wikipedia.org/wiki/Registered_trademark_symbol).  My HTTP response was returning the data contained in this XML element, and on each response, the data was truncated by one-byte.  Though a useful pair programming exercise, a colleague and I looked into the problem, and found that I was improperly computing the `Content-Length`.

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/remember-kids-an-http-content-length-is-the-number-of-bytes-not-the-number-of-characters/utf-8-registered-trademark.png" width="400">

Here's why.  According to the [proper Unicode documentation/chart on unicode.org](http://www.unicode.org/charts/PDF/U0080.pdf), we see that the registered trademark symbol uses two bytes to represent itself: 0x00 AE.  But we know that in UTF-8 land, the [first byte should indicate that we're dealing with a two-byte character sequence](http://en.wikipedia.org/wiki/UTF-8#Description).  Hence, the UTF-8 encoding for the registered trademark symbol is: 0xC2 AE.  Using a trusty hex editor, we can verify that this indeed the correct encoding, by examining the byte sequence for the the UTF-8 encoded registered trademark symbol in the XML:

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/remember-kids-an-http-content-length-is-the-number-of-bytes-not-the-number-of-characters/registered-trademark-utf8-encoding.png" width="400">

Yep, sure enough, `0xC2 AE`.  So this character (r) uses two-bytes to represent itself in UTF-8.  In other words, even though the (r) registered trademark symbol is a single code point in my response, it needs two bytes to properly represent itself.

Now, say you include this XML element in an HTTP response of some kind, but computed the `Content-Length` using `String`'s `length()` method.  Like me, you might find that your HTTP response is truncated, given that your computed `Content-Length` is one byte less than it should be:

```java
// WRONG: because String.length returns the number of
// characters, not the number of bytes like you would
// expect for UTF-8 encoded characters,
String response = // ... insert XML with UTF-8 characters here
this.contentLength_ = response.length();
```

To fix this, convert the string to a sequence of UTF-8 encoded bytes, and compute the content length using the length of the resulting byte array:

```java
// CORRECT: get a UTF-8 encoded byte array from the response
// String and set the content-length to the length of the
// resulting byte array.
String response = [insert XML with UTF-8 characters here];
byte[] responseBytes;
try {
  responseBytes = response.getBytes("UTF-8");
} catch ( UnsupportedEncodingException e ) {
  System.err.print("I hate UTF-8");
}

this.contentLength_ = responseBytes.length;
```

Not surprisingly, this solved my problem.  So, remember kids, your `Content-Length` header is the number of bytes in your response, not the number of characters in a String.  And, as you just learned, some code points (characters) in UTF-8 land can use up to four bytes to represent themselves.

Back to work.