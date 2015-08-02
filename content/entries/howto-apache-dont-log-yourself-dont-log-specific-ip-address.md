In some Apache web-server configurations, it might be useful avoid logging requests from specific IP addresses or `User-Agent`'s.  For example, if you regularly check your own site from your home network you probably don't want to record your own visit in your Apache `access_log`'s.  For `mark.koli.ch`, I stopped logging requests from my home network since I was filling up my own log files with redundant junk.

### httpd.conf Specific Configuration

Here's how you can stop logging requests from a specific IP address, or a request for a specific resource (goes in your `httpd.conf` file):

```
## Dont log myself (requests from my own network), requests
## for my robots.txt file.
SetEnvIf Remote_Addr "192\.168\.1\." dontlog
SetEnvIf Request_URI "^/robots\.txt$" dontlog

## I don't use this, but I have it here as an example.  Why
## I avoid using this is explained below.
##SetEnvIfNoCase User-Agent "(msnbot|googlebot|slurp)" dontlog

## Normal logging directives.  Note the env!=dontlog at the
## end of CustomLog.
ErrorLog logs/mark.koli.ch-error_log
CustomLog logs/mark.koli.ch-access_log combined env=!dontlog
```

### Why Should I NOT Use SetEnvIfNoCase User-Agent to Avoid Logging Requests From Specific User-Agents?

You can use `SetEnvIfNoCase User-Agent` if you want to stop logging requests from specific `User-Agent`'s.  However, I would recommend that you avoid using this feature because it is extremely easy to forge/fake the `User-Agent` header of an HTTP request.  If a hacker tries to probe or attack your site disguised as the "GoogleBot", and your Apache server is configured to not log requests from clients that claim they are the "GoogleBot", you won't see the probe attacks in your log files.  In short, Apache will think the request is from the GoogleBot, when in fact, it could be a hacker or malicious user masquerading as a web-crawler.

Cheers.

<!--- tags: apache, security -->