In July of '09, when I first learned of the "data" URL scheme, I was pumped.  With a little work, my web-applications could use the "data" URL scheme to embed actual base-64 encoded binary image data directly inside of my HTML and CSS.  In the same post, I subsequently commented on why this scheme can be incredibly useful, especially for mobile web-applications or API's that service mobile apps.  Even with significant advances in wireless networks over the past several years, traditional HTTP continues to lag (for the most part) over poor 3G and 4G networks.  For this reason, the "data" URL scheme can be a life saver &mdash; you can embed binary image data directly inside of your HTML and CSS, freeing the device from initiating wasteful HTTP transactions to load these images later.

Today marked yet another personal milestone for my usage of the "data" URL scheme.  Building an API that services a mobile app for the HP/Palm webOS platform, I quickly rediscovered the importance of this scheme.  It turns out I can embed base-64 encoded binary image data in a JSON response payload that is sent directly to a wireless webOS device!  What this means, is that I can build my API resource to send everything the app requested, including any additional external resources like images, in a single HTTP response!

### An Example

Imagine the app is fetching details about a user from my API.  The HTTP request leaving the app might look something like this:

```
GET /user/markkolich.json HTTP/1.1
Host: api.example.com
User-Agent: webOS
```

And, a normal HTTP response might look something like this:

```
HTTP/1.0 200 OK
Date: Fri, 21 Jan 2011 23:09:32 GMT
Content-Type: application/json;charset=UTF-8
Content-Length: 78
Vary: Accept-Encoding,User-Agent
Connection: close

{
 "user":"markkolich",
 "name":"Mark Kolich",
 "avatar":"http://api.example.com/images/markkolich.png"
}
```

Nothing special.  But note that the API response, a JSON object, contains a URL to an avatar image which in all likelihood the app will need to fetch and display later.  The problem here, is that now that the app has the response in its hands, it has to turn around and kick off yet another HTTP transaction to load the image from the provided URL.  And that additional fetch could be very painful over a slow wireless network with quite a bit of latency.

### The "data" URL Scheme to the Rescue

Instead of the API sending just a URL that points to an image, it could also include the actual image data itself, a base-64 encoded "data" URL in the JSON response.  For example:

```json
{
 "user":"markkolich",
 "name":"Mark Kolich",
 "avatar":{
  "url":"http://api.example.com/images/markkolich.png",
  "data_uri":"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAA
    ABCAAAAAA6fptVAAAAAnRSTlMA/1uRIrUAAAACYktHRAD+8Ij8KQAAAAlwSFlz
    AAAASAAAAEgARslrPgAAAAl2cEFnAAAAAQAAAAEAx5Vf7QAAAApJREFUCNdj+A
    8AAQEBABu27lYAAAAASUVORK5CYII="
 }
}
```

I know that this isn't technically valid JSON because I'm line wrapping in the middle of the base-64 encoded image payload; I'm wrapping so that I can fit the entire JSON block into a single column on my blog for display purposes.  For the record, your data payload shouldn't have any line breaks in it.

Looking at the new response, this is far superior to sending just a URL that points to an image.  Yes, we're sending a little more data, but now the app does not have to initiate another HTTP transaction to load the avatar.  Due to the usually poor latency of wireless networks (EDGE, 3G, 4G, etc.), and the natural overhead of HTTP, it's far better to send more data at once in a single transaction than over multiple smaller transactions.

Finally, in JavaScript, sourcing the encoded image data into an actual Image object is trivial:

```javascript
// Assume userObj is the JSON object returned
// in my example response above.
var userObj = { /*...*/ };

// Straight up.
(new Image).src = userObj.avatar.data_uri;

// Maybe you prefer jQuery?
$("<img>").attr("src", userObj.avatar.data_uri);
```

Assuming your app platform supports the "data" URL scheme this strategy wins every time, hands down.

### The "data" URL Format

Putting it all together, the [RFC 2397](http://tools.ietf.org/html/rfc2397) says the accepted syntax/format of data URI's are as follows:

```
data:[<mediatype>][;base64],<data>
```

So you'll need to define a media type (the `Content-Type`), declare that the data is base-64 encoded and provide an encoded payload.

### Determining the Content-Type

If you don't already know the `Content-Type` of the image you plan to base-64 encode, then you'll have to discover it.  This isn't too hard, and involves writing a bit of code that checks the header of your image to determine its type.  If you examine the specs of each image format you plan to support, you'll probably find that:

a. Every JPEG-image starts with a quick 2-byte "Start of Image" (SOI) marker:
```
0xFF D8
```

b. Every PNG-image starts with a fixed 8-byte signature:
```
0x89 50 4E 47 0D 0A 1A 0A
```

c. Every GIF-image starts with a fixed 3-byte signature:
```
0x47 49 46
```

I'm not going to post any sample solutions here, because iterating over `byte[]` arrays and comparing values to determine an image format is trivial.  However, in Java, it may help you to think of each image format as a value in an enumeration:

```java
public enum ImageContentType {
  
  PNG("image/png", new byte[]{
    (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, 
    (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A
    }),

  JPEG("image/jpeg", new byte[]{
    (byte)0xFF, (byte)0xD8
    }),

  GIF("image/gif", new byte[]{
    (byte)0x47, (byte)0x49, (byte)0x46
    });
  
  private String contentType_;
  private byte[] header_;
  
  private ImageContentType(String contentType, byte[] header) {
    contentType_ = contentType;
    header_ = header;
  }
  
  public byte[] getHeader() {
    return header_;
  }
  
  public String getContentType() {
    return contentType_;
  }
  
  @Override
  public String toString() {
    return getContentType();
  }
  
  public static final ImageContentType getContentType(final byte[] image) {
    ImageContentType ict = null;
    for(final ImageContentType type : ImageContentType.values()) {
      /* compare the header of image[] to type.getHeader() */
    }
    return ict;
  }
  
}
```

Of course, you could always just check the file extension of the image you plan to encode, and determine a `Content-Type` based on that.  In other words, if the resource ends in .jpg then you could assume, with reasonable certainty, that the Content-Type is image/jpeg.  However, only relying on the file extension to tell you the format can be a dangerous strategy if you're not careful.

### Encode and Assemble

Now that you know the Content-Type, you can base-64 encode the image payload and assemble a valid "data" URI string.  Don't bother writing a base-64 encoder from scratch, since there are many wonderful open-source implementations available for free.  The most popular seems to be in the [Apache Commons Codec](http://commons.apache.org/codec/) library.  Specifically, take a peek at [org.apache.commons.codec.binary.Base64](http://commons.apache.org/codec/api-release/org/apache/commons/codec/binary/Base64.html).

So, here's some pseudo code illustrating how to put it all together:

```java
import org.apache.commons.codec.binary.Base64;

private static final String DATA_URI_SCHEME = "data:%s;base64,%s";

/* ... */

// Create a new Base64 object, setting the line length to zero
// so that the output is not chunked (e.g., no line breaks).
final Base64 b64 = new Base64(0);

// Some image data, that you've fetched elsewhere.
final byte[] image = ...;

// Discover the image format.
final ImageContentType ict = ImageContentType.getContentType(image);

// Encode the image.
byte[] encoded = b64.encodeBase64(image);

// Convert the encoded byte array into its String representation.
// Base-64 is just ASCII, to this is totally fine.
String imageEncoded = new String(encoded, "UTF-8");

/* ... */

// Build the data URI String for inclusion in our API response.
final String dataUri = String.format(DATA_URI_SCHEME,
  ict.toString(), imageEncoded);
```

Now that you've assembled a valid data URI String, it's simple to attach it to a JSON object and return it to the caller.

<!--- tags: java, rfc2397, http -->