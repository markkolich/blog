It's long been rumored that exposing the HTTP [TRACE](http://www.cgisecurity.com/questions/httptrace.shtml) and [TRACK](http://osvdb.org/5648) methods on your web-server can open the door to a number of miscellaneous vulnerabilities, including cookie thefts and other [cross-site tracing attacks](http://www.karakas-online.de/forum/viewtopic.php?t=341).  Many resources out there claim you should configure you web-server to flat-out reject TRACE and TRACK requests, and I agree with them.  Generally speaking, there's really no good need (that I've found) that would require or make use of `TRACE` or `TRACK`.  With that said, if you're running Apache, it's fairly easy to reject `TRACE` and `TRACK` using `mod_rewrite`:

```
RewriteCond %{REQUEST_METHOD} ^TRACE [NC,OR]
RewriteCond %{REQUEST_METHOD} ^TRACK [NC]
RewriteRule ^/(.*)$ - [F,L]
```

You can prove to yourself that this works, by using a tool like curl to issue an HTTP `TRACE` and `TRACK` to your newly secured web-server.  Use the `-X` option with `curl` to specify the HTTP request type:

```
#/> curl -v -X TRACE mark.koli.ch
* About to connect() to mark.koli.ch port 80 (#0)
*   Trying 24.130.215.240... connected
* Connected to mark.koli.ch (24.130.215.240) port 80 (#0)
> TRACE / HTTP/1.1
> User-Agent: Curl
> Host: mark.koli.ch
> Accept: */*
>
< HTTP/1.1 403 Forbidden
< Date: Sat, 14 Nov 2009 18:53:06 GMT
< Server: Apache
< Content-Length: 202
< Connection: close
< Content-Type: text/html; charset=iso-8859-1
<
<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML 2.0//EN">
<html><head>
<title>403 Forbidden</title>
</head><body>
<h1>Forbidden</h1>
<p>You don't have permission to access / on this server.</p>
</body></html>
* Closing connection #0
```

Yep, works nicely.  One thing that slightly annoys me, however, is that the HTTP `OPTIONS` method still reports that my server supports `TRACE`, even though I clearly don't anymore.  A quick Google search reports that many other folks have had the same concern, with no clear resolution.

<!--- tags: apache, mod_rewrite, security -->