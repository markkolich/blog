After setting up my mobile blog at http://mobi.koli.ch, I started looking for ways to improve the content delivery speed of this service to mobile devices.  On the server-side, I'm fairly sure I implemented just about every hack imaginable to squeeze every bit of performance I could from the PHP engine.  At this point, my only bottleneck was the actual content delivery chain.  I can't control how fast my data is transferred over a wireless (2G/3G?) network because of factors outside of my control, but I can help grease the skid.  To do so, I activated Apache's [mod_deflate](http://httpd.apache.org/docs/2.2/mod/mod_deflate.html) extension which compresses the content of an HTTP response before its delivered to the client.

The `mod_deflate` module uses `gzip` to deflate the HTTP response body.  Obviously, since the response is compressed, that means there's less data to transfer.  Hence, it takes less "cycles" (packets, octets, whatever) to get the data to the client.  In short, using compression within the confines of HTTP can often dramatically improve the "speed" of your site or mobile portal on a wireless network.  Of course, not only wireless clients benefit from the performance improvement, but typically wireless devices see more of an improvement than an average broadband user.

First, I verified that `mod_deflate` is installed and pre-activated with Apache on my CentOS 5 box:

```
#/~> cat /etc/httpd/conf/httpd.conf | grep mod_deflate
LoadModule deflate_module modules/mod_deflate.so
```

Second, I hacked my http://mobi.koli.ch `VirtualHost` configuration a bit to activate the `mod_deflate` output filter.  Using the instructions on the [mod_deflate homepage](http://httpd.apache.org/docs/2.2/mod/mod_deflate.html), I configured my `mobi.koli.ch` `VirtualHost` to use the highest compression level possible on every response except for binary image data.  I also defined a custom "deflate" log so I can check the compression ratio on any responses:

```
<VirtualHost *:80>

  DocumentRoot /my/server/root/kolich.mobi/
  ServerName www.kolich.mobi
  ServerAlias kolich.mobi
  ServerAdmin support@example.com

  DeflateCompressionLevel 9
  SetOutputFilter DEFLATE
  BrowserMatch ^Mozilla/4 gzip-only-text/html
  BrowserMatch \bMSIE !no-gzip !gzip-only-text/html
  SetEnvIfNoCase Request_URI \.(?:gif|jpe?g|png|ico)$ no-gzip dont-vary
  Header append Vary User-Agent env=!dont-vary

  DeflateFilterNote ratio
  LogFormat '"%r" %b (%{ratio}n%%) "%{User-agent}i"' deflate
  CustomLog logs/kolich.mobi-deflate_log deflate

  ErrorLog logs/kolich.mobi-error_log
  CustomLog logs/kolich.mobi-access_log combined

</VirtualHost>
```

This works quite nicely.  Looking at the custom deflate log, my `mobi.koli.ch` content compression ratio is about 40-50% on average.  Not bad for less than 5 minutes of work!

Some things to consider when using `mod_deflate`:

* It wasn't immediately clear to me if `mod_deflate` looked for the `Accept-Encoding` HTTP request header to verify that the client supports `gzip` compression.  At least, there was no mention of this on the `mod_deflate` homepage &mdash; but as it turns out, yes, `mod_deflate` does look through the HTTP request headers for `Accept-Encoding: gzip` and will not compress the response if the client does not support it.
* Of course, not all clients support compression.
* Compression will use additional CPU cycles on the client and the server.  If a large majority of your target audience is using slow mobile devices, then you might want to think twice about HTTP compression.  Generally speaking though, todays mobile clients should have plenty of CPU power to handle simple `gzip` compression.
* High-traffic sites will want to think twice about compression.  You wouldn't want the added CPU consumption from compression to impact your overall site experience.

Cheers.