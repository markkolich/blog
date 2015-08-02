A blog reader recently contacted me via email and asked, "hey, how does your `koli.ch` tiny URL thing work?"  Well, I would be happy to explain.  First, I'm not using Apache's `mod_rewrite` engine.  No, `koli.ch` tiny URL's are using an interesting combination of Apache's `mod_rewrite` `RewriteRule` directive and [Onyx](https://onyx.koli.ch), which is basically PHP talking to a MySQL database.

So, let's follow [http://koli.ch/x3dh](http://koli.ch/x3dh) through the redirection process.  The "3dh" you see on the end of each URL is a [base-36 encoded number](http://mark.koli.ch/2009/10/base36-encoding-for-tiny-urls-with-php.html), that maps to a unique bookmark resource stored inside of [Onyx](https://onyx.koli.ch).

### Step 1 - koli.ch to mark.koli.ch

First, you may have noticed that http://koli.ch always redirects to http://mark.koli.ch by default.  This is accomplished using Apache's mod_rewrite in my virtual host configuration:

```
RewriteCond %{HTTP_HOST} !^mark\.koli\.ch [NC]
RewriteRule ^/(.*)$ http://mark.koli.ch/$1 [R=301,L]
```

You'll notice that the Perl compatible regular expression in the `RewriteRule` directive captures the URL-path content after the first slash, allowing me to gracefully forward URL's like [http://koli.ch/about](http://koli.ch/about) to [http://mark.koli.ch/about](http://mark.koli.ch/about).  Consequently, tiny URL's like [http://koli.ch/x3dh] are forwarded to [http://mark.koli.ch/x3dh].  And, this redirect is a `301 Moved Permanently` instead of a default `302 Found` redirect.

### Step 2 - mark.koli.ch to onyx.koli.ch

Next, in the same virtual host configuration, I use `mod_rewrite`'s `RewriteRule` directive to match URL-paths that represent a tiny URL.  As far as [Onyx](https://onyx.koli.ch) is concerned, all of my tiny URL's all start with "x" and contain at least one, but no more than ten, word characters following the "x":

```
RewriteCond %{REQUEST_URI} !\.(htm?l)$ [NC]
RewriteRule ^/x(\w{1,10})$ https://onyx.koli.ch/x$1 [L]
```

Notice the regular expression group, I'm only capturing the word-characters following the "x" in the request URI, then appending them onto [https://onyx.koli.ch/x] before handing it off to Onyx.  Note that I have not specified the redirect response code in the RewriteRule options.  In this case, Apache will [302 Found](http://en.wikipedia.org/wiki/HTTP_302) redirect to Onyx, which tells search engines and other bots that this redirect is much less permanent than a [301 Moved Permanently](http://en.wikipedia.org/wiki/HTTP_301).

### Step 3 - onyx.koli.ch to ?

Once the browser hits [https://onyx.koli.ch/x], I use `RewriteRule` one last time to call a redirection controller I wrote in PHP which actually takes the tiny URL, sanitizes the request URI then looks it up in a MySQL database to retrieve the final URL to redirect to.  This is part of Onyx.  Here's the pseudo code:

```php
<?php

$uri = (isset($_SERVER['REQUEST_URI'])) ? $_SERVER['REQUEST_URI'] : null;
if(empty($uri)) {
  throw new InternalErrorException();
}

// Strip the leading / from the front of the URI
$uri = preg_replace("/^\//","",$uri);
@list(, $resource) = preg_split("/[\/\?]/", $uri, -1, PREG_SPLIT_NO_EMPTY);
if(!empty($resource)) {
  $resource = preg_replace("/\D/","",$resource);
} else {
  throw new BadRequestException();
}

// ... do some MySQL things here, look up the $resource in the DB.
$url = ... some URL from the database

// Redirect the user to their destination, via an HTTP 302 Found
header(HTTP_302_FOUND);

// Unset the Pragma HTTP response header; to avoid
// redirection issues on IE.
header(PRAGMA.": ");
header(CACHE_CONTROL.": no-store, no-cache");

header(LOCATION_HEADER.': '.$url);

?>
```

That's it.  Cheers.

<!--- tags: php -->