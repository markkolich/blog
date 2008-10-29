On the web, malicious hackers typically try to exploit bugs or holes in un-patched versions of public web-servers.  The [Apache web-server](http://httpd.apache.org/) is an obvious target, given that as of [June 2008 Apache served 49.12% of all websites on the Internet](http://news.netcraft.com/archives/2008/06/22/june_2008_web_server_survey.html).  In fact, the Apache web-server is powering this blog and my network of other domains.

When a client (most often a browser) makes an HTTP request to a web-server, the server responds with an HTTP response.  The response contains a status line with a status code (e.g., HTTP/1.1 200 OK) and a set of response headers.  Surprisingly, the Apache web-server embeds version information about itself in these HTTP response headers.  If you are concerned about exposing the version of Apache you are running to the world, you may want to disable this.  Hackers often look for specific versions of Apache with known bugs to pick-on, then target the site with various attack methods.  Blocking this Apache version information in the HTTP response headers can make it more difficult for hackers to identify the version of Apache you are running and compromise your system(s).

The trick is to adjust or add a few Apache directives (a.k.a. options) to your `httpd.conf` file.  On a standard Fedora/Red Hat/CentOS install, the `httpd.conf` file can be found at `/etc/httpd/conf/httpd.conf`.  Set [ServerSignature Off](http://httpd.apache.org/docs/2.2/mod/core.html#serversignature) and [ServerTokens Prod](http://httpd.apache.org/docs/2.2/mod/core.html#servertokens) in your `httpd.conf` file:

```
ServerSignature Off
ServerTokens Prod
```

From the Apache documentation:

"The ServerSignature directive allows the configuration of a trailing footer line under server-generated documents (error messages, mod_proxy ftp directory listings, mod_info output, ...).  The ServerTokens directive controls whether the Server response header field which is sent back to clients includes a description of the generic OS-type of the server as well as information about compiled-in modules."

I have yet to encounter a need to actually enable a Server Signature or provide information about the Apache version in the HTTP response headers.

Oh, if you want to read up on why most admins hate the Apache web-server, take a look at [Why I Hate The Apache Web Server](http://people.apache.org/~rbowen/presentations/apacheconEU2005/hate_apache.pdf).

<img src="static/entries/howto-hide-apache-server-version-for-security-using-servertokens-and-serversignature/http_response.png">

Cheers.