In many web-service infrastructures, it's often desirable to disable the caching of redirects.  Specifically, you might want to set the [Expires](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21) or [Cache-Control](http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9) headers so that your 301 or 302 redirects from Apache's mod_rewrite are never cached upstream.  Off the top of my head, I can think of a number of reasons why you might want to prevent the caching of a redirect:

* Your redirect may change from one request, to the next.  Disable caching so the client (the browser) isn't redirected to the same destination every time.
* Your web-application is behind a reverse caching proxy, and you don't want the caching proxy to cache the redirect.
* In development, you're sitting behind a corporate web-proxy that is notorious for caching content when it really shouldn't.  Disable caching on the redirects so you can verify that your web-application is working as expected during testing (assuming the web-proxy obeys your Cache-Control and Expires headers).
* Your web-application counts how many times someone is redirected.  Disable caching so your click-through statistics are a bit more accurate.

Surprisingly, this seemingly common need isn't well documented in the official Apache docs.  So, here's how to do it.

In this example, I'm redirecting based on the Host.  If the incoming request does not match the Host I require, mod_rewrite triggers a 301 redirect to the correct Host.  Of course, your [RewriteCond](http://httpd.apache.org/docs/2.2/mod/mod_rewrite.html#rewritecond)'s might be different.

```
RewriteCond %{HTTP_HOST} !^mark\.koli\.ch [NC]
RewriteRule ^/(.*)$ http://mark.koli.ch/$1 [R=301,L,E=nocache:1]

## Set the response header if the "nocache" environment variable is set
## in the RewriteRule above.
Header always set Cache-Control "no-store, no-cache, must-revalidate" env=nocache

## Set Expires too ...
Header always set Expires "Thu, 01 Jan 1970 00:00:00 GMT" env=nocache
```

In this example, when the RewriteRule is fired the "nocache" environment variable is set.  Note the **E=nocache:1** [rewrite flag](http://httpd.apache.org/docs/2.2/mod/mod_rewrite.html#rewriteflags) in the RewriteRule.  Subsequently, mod_headers will set the `Cache-Control` and `Expires` headers only if this "nocache" environment variable is set.  In other words, "nocache" is only set on a 301 redirect from the RewriteRule.

This works nicely.

```
GET /wombat HTTP/1.1
Host: koli.ch

HTTP/1.1 301 Moved Permanently
Date: Sat, 11 Dec 2010 19:36:09 GMT
Location: http://mark.koli.ch/wombat
Server: Apache
Cache-Control: no-store, no-cache, must-revalidate
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Content-Length: 230
Content-Type: text/html; charset=utf-8
Connection: close
```

Yay for HTTP.