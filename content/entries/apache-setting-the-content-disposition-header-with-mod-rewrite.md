The title of this post is slightly misleading, since the `Content-Disposition` header cannot be set directly using `mod_rewrite`.  As I understand it, there are a set of headers that `mod_rewrite` understands, and `Content-Disposition` is not one of them.

To accomplish this, I used an interesting combination of `mod_rewrite` and `mod_headers`:

```
## Internally redirect, then..
RewriteRule ^/build/latest /build/dist/build-v1.0.jar [L]
SetEnvIfNoCase Request_URI build\/latest$ build-latest
## ..set the header.
Header set Content-Disposition "attachment; filename=build-latest.jar" env=build-latest
```

Using this method, here's what the `Content-Disposition` header looks like according to HttpFox:

<img src="static/entries/apache-setting-the-content-disposition-header-with-mod-rewrite/httpfox-snapshot-cappuccino-kolich.jpg" width="400">

Cheers.