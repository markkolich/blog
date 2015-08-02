When discussing HTTP, standard conjecture these days is that the server side is obligated to read and buffer request bodies in their entirety.  Although not formally required, many antiquated server side frameworks will synchronously read and buffer `POST` and `PUT` bodies into local memory (up to some configurable maximum) before invoking the application layer business logic.

You should note that just because an HTTP client *wants* to send a request body, does *not mean* the server side is required to read it.  Furthermore, it's especially bad form if your server side web-layer blindly buffers a `POST` or `PUT` body into memory, regardless of whether or not the underlying application business logic actually needs it.

### But Someone Has to Read the Bytes!

Actually, no.

For a moment, imagine I'm the server and you're the client.  To initiate an HTTP transaction with me, you open a socket bound to some port, and send me a request.  Your request headers on-the-wire might look something like this:

```
POST /foobar HTTP/1.1
Host: localhost
Content-Type: application/x-www-form-urlencoded
Content-Length: 1073741824
```

As the server, once I've received your request headers, I'm now tasked with deciding what I want to do.  Note, at this point, I haven't read any data from you other than the headers even though you've said you want to send me a 1GB payload (as specified by your `Content-Length` header).

Let's then say I decide that `1073741824` bytes is just too large, and it exceeds the amount of data I'm willing to process.  As such, without reading any data from you, I immediately respond with this:

```
HTTP/1.1 413 Request Entity Too Large
Content-Length: 0
Connection: close
```

In other words, I received your request, but you want to send me too much data.  Without reading anything else from you on-the-wire, I rejected your request.  So you see, on my end I may have had a pointer to an open `InputStream` from which I could have consumed to read your request body, but I chose not to.  In short, this means that you never sent your 1GB payload over-the-wire &mdash; I determined that I didn't want to read it, and therefore, no data was sent.

This is completely legal.

### Bad

In general, the worst way to approach this on the server-side is to blindly buffer the entire request body into memory, regardless of its size and whether the application layer needs the body or not.  This is essentially playing Russian roulette with your application stack.  You're going to try and buffer the entire request body into memory, regardless of its size and whether or not it's needed, and hope you don't fall over in the process.

In Java, assuming you have an open `InputStream` representing a pointer to the bytes on-the-wire:

```java
import org.apache.commons.io.IOUtils;

final InputStream in = getInputStream();
final ByteArrayOutputStream out = new ByteArrayOutputStream();

IOUtils.copyLarge(in, out); // Ouch
```

This JVM is guaranteed to fall over if its heap size is `N` bytes, and it receives a single request whose `Content-Length` is just `N+1` bytes.  It will likely fall over with much less than `N+1` bytes well before an excessively large request is ever received, given other classes and resources are already loaded and held in local memory.

### Better

A *slightly* improved approach to this problem is to buffer the request body up to a fixed size.  Again, this is buffering the request body in memory whether it's needed or not by the application layer &mdash; that is, on any `POST` or `PUT` request that contains a `Content-Length`, it will be buffered locally up to some configured maximum.

```java
import org.apache.commons.io.IOUtils;
import com.google.common.io.ByteStreams; // From Google Guava

final InputStream in = getInputStream();

// Wrap input, limit to 1MB
final InputStream wrapped = ByteStreams.limit(in, 1048576L);

final ByteArrayOutputStream out = new ByteArrayOutputStream();
IOUtils.copyLarge(wrapped, out);
```

This is a bit better &mdash; assuming you limit each request processing thread in your application to only buffer `1MB`, you'll use at most `N * K` bytes where `N` is the maximum number of request bytes to buffer per thread and `K` is the total number of request handler threads.

Now, with a fixed ceiling, we can tune our heap size and number of request threads to be sure that request bodies will never consume enough of the heap to cause our JVM to crash.

### Best

The *right* way to approach this problem is twofold.  Only buffer request bodies:

* up to a configurable maximum size
* and, only if **needed by the application layer**

In theory, any `POST` or `PUT` request can contain a request body with some `Content-Length`.  But, that doesn't mean the web-layer has to read it.

Consider the case where a `POST` request is used to toggle a virtual ON/OFF switch.

```
POST /switch/toggle HTTP/1.1
Host: localhost
Content-Type: application/x-www-form-urlencoded
Content-Length: 1073741824
```

In this instance, everything the application layer needs to make a decision and complete the operation is already provided (hint: it's in the URI `/switch/toggle`).  The excessive payload you see in the `Content-Length` here can be completely ignored &mdash; there's no reason any web-layer should attempt to read and locally buffer this `1GB` payload.

### Curacao

If using an intelligent web-layer toolkit like [Curacao](https://github.com/markkolich/curacao), addressing this problem is trivial.  That is, your application doesn't have to manage decisions around whether or not to buffer the request body, Curacao will do it for you as appropriate.

First, in your `application.conf` configuration file, define a reasonable value for `curacao.mappers.request.max-request-body-size`:

```
curacao {
  mappers.request.max-request-body-size = 1m
}
```

This will instruct Curacao to only ever buffer `1MB` of the request body into memory, as needed.  If unspecified, the default is `512KB`.

Next, define a controller:

```java
@Controller
public final class SwitchController {

  @POST("/switch/toggle")
  public final int toggle() {
    // ... toggle switch, return raw status code.
    return 200;
  }

  @POST("/switch/set")
  public final String set(@RequestBody final String op) {
    final String result;
    if("ON".equals(op)) {
      // ... turn on.
      result = "ON";
    } else if("OFF".equals(op)) {
      // ... turn off.
      result = "OFF";
    } else {
      // ... unknown state, no-op.
      result = "UNKNOWN";
    }
    return result;
  }

}
```

This example controller defines two methods.

1. First, the `toggle()` method will be called on receipt of a `POST:/switch/toggle` request.  Note the `toggle()` method has no arguments &mdash; when Curacao processes the `POST` request, no request body will be read from the client because the application layer is not asking for it.

2. Second, the `set()` method will be called on receipt of a `POST:/switch/set` request.  You'll note the `set()` method has a single argument: `@RequestBody final String op`.  This is Curacao's cue to read and buffer the `POST` request body into memory, up to the configured maximum of `1MB`.

But wait, we can make this even better!

We know that the `set()` method is built to only ever care about values 3-bytes or less: `ON` or `OFF`.  Knowing this, we can further tighten the processing of the request body.  For instance, we can pass a `maxSizeInBytes` argument into the `@RequestBody` annotation, to further limit the number of bytes Curacao will attempt to read before invoking this Java method:

```java
@POST("/switch/set")
public final String set(@RequestBody(maxSizeInBytes=3L) final String op) {
  // ...
}
```

Here, instead of buffering up to the globally configured maximum of `1MB`, just for this one request mapping Curacao will only accept a `Content-Length` that is 3-bytes or less. In the event that a client omits a `Content-Length` request header, Curacao will fallback gracefully and only read at most 3-bytes, then stop.

Pretty neat, eh?

Curacao is free, and open source, on GitHub:

https://github.com/markkolich/curacao

Pull requests welcome.

<!--- tags: curacao, http -->