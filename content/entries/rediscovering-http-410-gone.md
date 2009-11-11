This evening, I accomplished another important milestone in the kolich.com migration process.  Since acquiring koli.ch, I've been slowly migrating my blog and all of its resources to my new online home under mark.koli.ch.  This migration began back in September 2009.  Only problem is, I still see a ton of bots and other users requesting resources under kolich.com, even though as far as I'm concerned, it's shut down and there's nothing to see there.

For a month or so, I've been gracefully redirecting traffic with an HTTP `301 Moved Permanently`.  Even so, it appears that Google and other crawlers are still hitting kolich.com looking for stuff that simply doesn't exist there anymore, even though I've been telling them for a solid month to "go look somewhere else."  Time to pull out the big guns.  A quick flip through my handy copy of RFC 2616, that's the HTTP 1.1 spec, lead me to rediscover [HTTP 410 Gone](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.11).  If you haven't met `HTTP 410`, it's the forgotten step child of [HTTP 404 Not Found](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5).  As described, "Error 410 means 'Resource gone', as in, a resource used to exist at this location, but now it's gone. Not only is it gone, but I don't know (or I don't want to tell you) where it went. If I knew where it went, and I wanted to tell you, I would use error 301 ('Permanent redirect') and any smart client would simply redirect to the new address. But 410 means 'Resource gone, no forwarding address'. Train gone sorry."

Looks great, that's exactly what I need.  Time to serve up some 410's.  I configured my local `mark.kolich.com` Apache 2.2.3 virtual host with `mod_rewrite` to return an `HTTP 410 Gone` for most resources:

```
RewriteCond %{REQUEST_URI} !\.(html?)$ [NC]
RewriteCond %{REQUEST_URI} !^/$ [NC]
RewriteRule ^/(.*)$ - [G,L]
```

Note the `[G,L]` on the RewriteRule directive.  `G`, meaning Gone, and `L` meaning the last rule in the chain to apply to this request.  In this case, any request for a resource that doesn't end in .html (or .htm) and isn't aimed at the server root, I immediately respond with an `HTTP 410 Gone`.  I'm handling HTML pages a little differently.  Requests for an actual blog entry itself (a resource that ends in `.html`), are caught an handled a little more gracefully.  I haven't yet decided when to phase out this graceful catch.

In any event, let's see if an HTTP 410 gets the attention of those pesky crawlers and RSS feed readers.