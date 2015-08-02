When dealing with AJAX, you might need to configure Apache to return a HTTP `204 No Content`.  This is useful when your AJAX scripts need to "ping" the server, but you don't want the server to actually return any data in the response (e.g., just acknowledge the request and return and empty response body).  The server might do something behind the scenes though (like log the request) before it returns a 204.  As I understand it, the only difference between a 200 and a 204, is that a [204 response means that "the server has fulfilled the request but does not need to return an entity-body"](http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5).

<img src="https://raw.githubusercontent.com/markkolich/blog/release/content/static/entries/howto-configure-apache-to-return-a-http-204-no-content-for-ajax/apache-gen-http-204-small.png">

I tried to figure out how to configure Apache to return a `204 No Content` using one of the built in modules, like [mod_actions](http://httpd.apache.org/docs/2.2/mod/mod_actions.html), or [mod_headers](http://httpd.apache.org/docs/2.2/mod/mod_headers.html).

It turns out, Andrew Grangaard alerted me [that you can configure RedirectMatch to return an HTTP 204 No Content](http://lowlevelmanager.blogspot.com/2009/07/returning-apache-204.html) with [mod_alias](http://httpd.apache.org/docs/2.2/mod/mod_alias.html).

So there are a few options:

### Use RedirectMatch

As [explained by Andrew Grangaard, you can use Apache's RedirectMatch directive](http://lowlevelmanager.blogspot.com/2009/07/returning-apache-204.html) to trigger on a specific URL pattern and reply with an HTTP 204 No Content response:

```
RedirectMatch 204 tracker(.*)$
```

This triggers on any URI starting with `tracker`.

### Use a Perl Script (Hack)

You could also use Apache's [SetHandler and Action directives](http://httpd.apache.org/docs/2.2/mod/mod_actions.html#action) to intercept the request and then call a Perl/CGI script to return a `204 No Content`.  For example, in your `VirtualHost` configuration, use the `Location` directive to define the URI you want Apache to trigger on:

```
<Location /tracker>
  SetHandler nocontent-handler
  Action nocontent-handler /gen_204.cgi virtual
</Location>
```

So this tells Apache to call `gen_204.cgi` each time a request comes starting with `/tracker`.  Note the `virtual` modifier at the end of the `Action` directive &mdash; defining the action as `virtual` is important because it tells Apache not to check if the requested file/resource actually exists.  For example, `/tracker/foobar` doesn't actually exist (it's not a real resource on the server), so use the `virtual` modifier so Apache will ignore this and return a `404 Not Found`.

Here's a hacky `gen_204.cgi` Perl/CGI script I once used:

```perl
#!/usr/bin/perl -w

use strict;
use warnings;
use CGI;

my $cgi = CGI->new();
print $cgi->header('text/html','204 No Content');

exit;
```

This seems to work nicely, but is quite kludgy.

Better to use `RedirectMatch` instead.

<!--- tags: apache, http -->