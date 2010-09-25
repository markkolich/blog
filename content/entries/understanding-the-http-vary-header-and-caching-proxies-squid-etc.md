I never paid much attention to the [HTTP Vary header](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.44).  In fact, I've been fortunate enough to avoid it for this long and never really had to care much about it.  Well, it turns out when you're configuring a high-performance [reverse proxy](http://en.wikipedia.org/wiki/Reverse_proxy), understanding the `Vary` header and what it means to your reverse proxy caching policies is absolutely crucial.

Here's an interesting problem I recently solved that dealt with Squid, Apache, and that elusive `Vary` response header.

### The Vary Basics

Popular caching proxies, like [Squid](http://www.squid-cache.org/), usually generate a hash of the request from a number of inputs including the URI and the contents of the Vary response header.  When a caching proxy receives a request for a resource, it gathers these inputs, generates a hash, then checks its cache to see if it already has a resource sitting on disk, or in memory, that matches the computed hash.  This is how Squid, and other caching proxies, fundamentally know if they have a cache HIT or MISS (e.g., can Squid return the content it has cached or does it need to revalidate the request against the destination server).

That in mind, you can probably see how the `Vary` header is quite important when a caching proxy is looking for a cache HIT or MISS.  The Vary header is a way for the web-server to tell any intermediaries (caching proxies) what they should use, if necessary, to figure out if the requested resource is fresh or stale.  Sample `Vary` headers include:

```
Vary: Accept-Encoding
Vary: Accept-Encoding,User-Agent
Vary: X-Some-Custom-Header,Host
Vary: *
```

According to the HTTP spec, "the Vary field value indicates the set of request-header fields that fully determines, while the response is fresh, whether a cache is permitted to use the response to reply to a subsequent request without revalidation."  Yep, that's pretty important (I discovered this the hard way).

### The Caching Problem

I configured Squid to act as a round-robin load balancer and caching proxy, sitting in front of about four Apache web-servers.  Each Apache web-server was running a copy of my web-application, which I intended to have Squid cache where possible.  Certain requests, were for large JSON objects, and I explicitly configured Squid to cache requests ending in .json for 24-hours.

I opened a web-browser and visited a URL I expected to be cached (should have already been in the cache from a previous request, notice the HIT):

```
GET /path/big.json HTTP/1.1
Host: app.kolich.local
User-Agent: Firefox

HTTP/1.0 200 OK
Date: Fri, 24 Sep 2010 23:09:32 GMT
Content-Type: application/json;charset=UTF-8
Content-Language: en-US
Vary: Accept-Encoding,User-Agent
Age: 1235
X-Cache: HIT from cache.kolich.local
X-Cache-Lookup: HIT from cache.kolich.local:80
Content-Length: 25090
Connection: close
```

Ok, looks good!  I opened a 2nd web-browser on a different machine (hint: with a different `User-Agent`) and tried again.  This time, notice the `X-Cache: MISS`:

```
GET /path/big.json HTTP/1.1
Host: app.kolich.local
User-Agent: Chrome

HTTP/1.0 200 OK
Date: Fri, 24 Sep 2010 23:11:45 GMT
Content-Type: application/json;charset=UTF-8
Content-Language: en-US
Vary: Accept-Encoding,User-Agent
Age: 4
X-Cache: MISS from cache.kolich.local
X-Cache-Lookup: MISS from cache.kolich.local:80
Content-Length: 25090
Connection: close
```

Wow, look at that.  I requested exactly the same resource, just from a different browser, and I saw a cache MISS.  This is obviously not what I want, I need the same cached resource to be served up from the cache regardless of who's making the request.  If left alone, this is only caching a response per User-Agent, not globally per resource.

### Solution: Check Your Vary Headers

Remember how I said the contents of the `Vary` header are important for caching proxies?

In both requests above, note the `User-Agent` request headers and the contents of the `Vary` response headers.  Although each request was for exactly the same resource, Squid determined that they were very different as far as its cache was concerned.  How did this happen?  Well, take a peek at a `Vary` response header:

```
Vary: Accept-Encoding,User-Agent
```

This tells Squid that the request URI, the `Accept-Encoding` request header, and the `User-Agent` request header should be included in a hash when determining if an object is available in its cache, or not.  Obviously, any reasonable hash of (URI, `Accept-Encoding`, "Firefox") should not match the hash of (URI, `Accept-Encoding`, "Chrome").  Hence why Squid seemed to think the request was for different objects.

To fix this, I located the source of the annoying `User-Agent` addition to my `Vary` response header, which happened to come from Apache's very own `mod_deflate` module.  The recommended mod_deflate configuration involves appending `User-Agent` to the `Vary` response header on any response that is not compressed by `mod_deflate`.  I don't really see why this is necessary, but the Apache folks seemed to think this was important.  Here's the relevant lines from the Apache suggested `mod_deflate` configuration:

```
SetEnvIfNoCase Request_URI \.(?:gif|jpe?g|png|ico)$ no-gzip dont-vary
Header append Vary User-Agent env=!dont-vary
```

In any event, I removed the 2nd line above, restarted Apache and Squid began caching beautifully regardless of which client issued the request.  Essentially, I told Squid to stop caring about the `User-Agent` by removing `User-Agent` from my `Vary` response header, and problem solved!

The joys of HTTP.