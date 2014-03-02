I finally dumped [Movable Type](http://movabletype.org) and decided to invest a bit of engineering time into a new blogging platform for myself.

I started blogging in 2008, and at that time, the self-hosted blogging platform options were somewhat slim &mdash; choose Movable Type (built on `Perl` and `CGI`) or choose Wordpress (built on `PHP`, riddled with security issues).  Given its poor track record, I didn't trust Wordpress, so the choice was obvious: Movable Type.  However, as the web progressed, I quickly found myself using an outdated blogging tool *entirely due to my own laziness*; I started with Movable Type version-4.21 and never bothered to upgrade it over the course of 6-years.  Frankly, each time I mustered up the courage to upgrade my Movable Type install, I just gave up and poured myself a drink &mdash; it just wasn't something I wanted to tackle, full of gotch-ya's and landmines.  I had better things to do.

The straw-that-broke-the-camel's-back came last August in the form of this post, [Introducing Havalo, A Non-Distributed NoSQL Key-Value Store for your Servlet Container](introducing-havalo-a-non-distributed-nosql-key-value-store-for-your-servlet-container).  I recall using Movable Type's buggy editor to hammer out that blog post, and kept asking myself, "Why am I doing this?  Can't I just write this blog post using [Markdown](http://daringfireball.net/projects/markdown/)?  And why the hell am I still using Perl/CGI?"  Basically, 2008 called, and wanted its blogging platform back.  And so, the crusade for something better began.

Most folks would have immediately jumped to Blogger, or *[insert name of popular hosted blogging service here]*.  Apparently I like *reinventing wheels*, and this was an opportunity to really dig into some technologies I've been interested in for a while but didn't have the need to formally explore.  It was finally time to build my own open source blogging platform, from scratch, using the technologies I love.

### GitHub Integration & JGit

I love Git, and [GitHub](https://github.com/markkolich).

So, it felt completely natural to use GitHub as the underlying datastore for all of my blog content, not just the source code behind it.  Here's the content creation workflow I desired:

* Create new Markdown file in GitHub hosted repository: `touch new-entry-with-some-name.md`
* Write blog entry using Markdown in any editor I choose: `vi new-entry-with-some-name.md`
* Commit new blog entry to repository: `git commit -a -m "Title of new blog post"`
* Push file to the remote, to publish new entry: `git push origin master`

Then, once pushed, the JVM based web-app serving my blog to the world shall `git pull` and automagically update itself to show the new entry on the web.  In other words, I can just write, `commit`, `push`, and grab a Snickers.  Done.

Well, this is [exactly what I built](https://github.com/markkolich/blog/tree/master/content).

On the server side, the JVM-based web-app behind my blog uses [JGit](http://www.eclipse.org/jgit/) to initially `clone` on startup, and then subsequently `pull` on a fixed interval to keep itself up-to-date &mdash; a local `clone` of my blog repository is kept and managed on disk by the web-app.  Blog entries are sorted and stored in-memory based on their `commit` date, so the newest entry is always first.

### Markdown support with Pegdown

I love [Markdown](http://daringfireball.net/projects/markdown/).

Please, no more crappy "rich HTML editors", okay?  I just want to be able to create content using the powerful Markdown syntax I'm familiar with:

```markdown
### Some Heading
A paragraph.
1. A numbered
2. list
3. with some **bold** text.
<img src="an-image.png"/>
[A link to somewhere awesome](http://awesome.example.com)
```

I pulled in [Pegdown](https://github.com/sirthias/pegdown), a pure-Java Markdown processor based on a parboiled PEG parser.  It integrated with my app beautifully &mdash; all content, pages and entries, could now be written in Markdown and I wasn't tied to some awful web-interface with a buggy editor.

Page templating was still important, so I integrated [FreeMarker](http://freemarker.org) into the mix for common page components.  Pegdown consumes Markdown and spits out HTML, which is then piped into a FreeMarker template to produce near final HTML:

```
<#include "common/header.ftl">

  <h2 class="title">${title}</h2>
  <p class="hash"><a href="https://github.com/markkolich/blog/commit/${commit}">${commit}</a></p>
  <p class="date">${date}</p>

  <!-- Markdown generated HTML content -->
  <article>${content}</article>

<#include "common/footer.ftl">
```

Lastly, the [HTML Compressor](https://code.google.com/p/htmlcompressor/) was brought in to "compress" the FreeMarker generated HTML, removing wasted bytes like line breaks and other spacing characters between HTML tags.  The final HTML of each page is quite minimal and compresses nicely over a GZIP'ed transport stream, like GZIP'ed compressed HTTP.

### Responsive UI from Twitter Bootstrap

I love [Twitter Bootstrap](http://getbootstrap.com).

One wheel I didn't reinvent was the "responsive" UI layer for my new blogging platform.  I wanted something simple, beautiful, and off-the-shelf.  I snagged the latest version of Bootstrap, and chose its [Spacelab theme](http://bootswatch.com/spacelab/).  Given the flexible and extensible nature of Bootstrap, I can in theory, replace this theme with any other for a completely different look without any code rewrites.

Let's not forget Bootstrap's responsive design &mdash; the act of gracefully degrading or seamlessly transitioning to a view suitable for any device using [CSS3 media queries](https://developer.mozilla.org/en-US/docs/Web/Guide/CSS/Media_queries).  For years I've been maintaining a "mobile" version of my blog at http://mobi.koli.ch.  Given Bootstrap's built in responsiveness, I was finally able to shut down this dedicated mobile portal for good.  When you view my new blog on on a mobile device, like an iPhone or an iPad, you'll notice the view gracefully hides the right most column and displays just the page content.  When on a larger device, like a notebook, the right column is visible with more screen real estate.

In short, the mobile version of my blog is now "built in, for free" &mdash; whatever device you happen to be on, you'll see the most appropriate view that's optimized for your device.  And, more importantly, I no longer have to run and maintain a separate web-app just for mobile devices.

### Highly Optimized JavaScript with jQuery

I love [jQuery](http://jquery.com).

Bootstrap's core is written around jQuery, so it was a natural fit.

All [JavaScript was a simple extension of the closure pattern](https://github.com/markkolich/blog/tree/master/content/static/js), which made it easy for me to write robust and modular code.  At build time, my JavaScript is packed, highly optimized, and minified using [Google's Closure Compiler](https://developers.google.com/closure/compiler/).  Similarly, all CSS is packed and minified using the [YUI Compressor](http://yui.github.io/yuicompressor/).  Closure Compiler and YUI Compressor support is integrated directly into my `SBT` [Build.scala](https://github.com/markkolich/blog/blob/master/project/Build.scala) file, such that all JavaScript and CSS is packaged anytime the `compile` task is launched.

### Async Servlet Web-Layer with Curacao

I love the JVM, and JVM based web-applications.

When choosing a server side web-layer for this project, I mulled over several options:

* [Spring Framework](http://spring.io) &mdash; Too "enterprisey", too bloated, thread based, meh.
* [Play Framework](http://www.playframework.com/) &mdash; Akka Actor based, asynchronous overkill, too "off-the-shelf", meh.
* [Spray](http://spray.io) &mdash; Scala only, Akka Actor based, poorly documented, too "academic", meh.
* [Raw Servlet 3.0](https://jcp.org/aboutJava/communityprocess/final/jsr315/) &mdash; Thread based, asynchronous, too "raw", meh.
* Write my own?

I didn't need Akka Actor's for this project, that's complete overkill &mdash; threads will do just fine.  On the other hand, I like Spring (for the most part) but I really wanted to avoid all of the awful XML configuration and silly annotations.  In the end, I wrote my own asynchronous Servlet web-layer.

Meet [Curacao](https://github.com/markkolich/curacao), a open source toolkit for building REST/HTTP-based integration layers on top of asynchronous Servlet's:

https://github.com/markkolich/curacao

Curacao borrows concepts from Play, Spray, and Spring &mdash; I "merged" what I felt was the best of these worlds into a single toolkit.  Note I say "toolkit" and **not** framework, because Curacao is not built to be an end all-be all framework.  I intentionally avoided things like ORM, JDBC, AOP, etc.

With Curacao I can write powerful and completely asynchronous thread based web-services on top of any Servlet 3.0 compatible container.  Here's a sample `@Controller` implementation in Curacao:

```java
@Controller
public final class Blog {

  private final EntryCache cache_;

  @Injectable
  public Blog(final EntryCache cache) {
    cache_ = cache;
  }

  @GET("/")
  public final Entry index() {
    return cache_.get("index");
  }

  @GET("/{name}/**")
  public final Entry entry(@Path("name") final String name) {
    return cache_.get(name);
  }

}
```

This blog is deployed on Tomcat, but Curacao works with [Jetty](http://www.eclipse.org/jetty/), [Resin](http://www.caucho.com/resin-web-server/), and [Undertow](http://undertow.io/) too.

### Caching with Apache's `mod_cache`

No web-service would be complete without some reasonable level of caching.  For that, I turned to Apache's [mod_cache](http://httpd.apache.org/docs/current/mod/mod_cache.html).  I'm already running Tomcat behind Apache's [mod_proxy_ajp](http://httpd.apache.org/docs/current/mod/mod_proxy_ajp.html) so configuring [mod_cache](http://httpd.apache.org/docs/current/mod/mod_cache.html) in conjunction with [mod_expires](http://httpd.apache.org/docs/current/mod/mod_expires.html) was trivial.

First, `mod_expires` is in charge of setting an appropriate `Expires` HTTP response header on each response.  This is important for `mod_cache` given it keys on the `Expires` header to identify what resources it can/should cache, and for how long.  Here's my `mod_expires` configuration within Apache:

```
ExpiresActive On

## Default expiry is 5-minutes.
ExpiresDefault "access plus 5 minutes"

## Static content, images, fonts, etc. cache for 1-hour.
ExpiresByType image/jpeg "access plus 1 hour"
ExpiresByType image/png "access plus 1 hour"
ExpiresByType image/x-icon "access plus 1 hour"
ExpiresByType application/font-woff "access plus 1 hour"
ExpiresByType application/x-font-ttf "access plus 1 hour"

## For Atom/RSS feed, and XML sitemap.
ExpiresByType text/xml "access plus 1 hour"

## For robots.txt caching.
ExpiresByType text/plain "access plus 1 hour"
```

The caching of images, fonts, and dynamic resources like `atom.xml` and `robots.txt` are slightly more aggressive given they rarely change and are requested more often by crawlers and bots.

To hold cached content, I configured a large [RAM disk](http://en.wikipedia.org/wiki/RAM_drive) mounted at `/mem` for `mod_cache`:

```
CacheRoot /mem
CacheEnable disk /

## The default duration to cache a document when no expiry date is specified.
CacheDefaultExpire 300
## The maximum time in seconds to cache a document.
CacheMaxExpire 300

CacheIgnoreNoLastMod On
CacheIgnoreCacheControl On

## The maximum size (in bytes) of a document to be placed in the cache.
CacheMaxFileSize 104857600

## NOTE: CacheDirLevels * CacheDirLength must not be > 20
CacheDirLevels 10
CacheDirLength 2
```

A RAM disk is a filesystem mounted in volatile memory &mdash; access to files and resources cached in a RAM disk is almost always significantly faster than hitting a spinning disk platter.  The goal here being that common requests for static resources, and other content that rarely changes, will usually not hit application code.  Instead, they would be fresh in the cache.

### This blog, and all its content, is open source

Last but not least, all code and content (everything you're reading here) is open source on GitHub at:

https://github.com/markkolich/blog

Of course, you can `clone` your own local copy of my blog and all of its content anytime you wish:

```
git clone https://github.com/markkolich/blog.git
```

Pull requests welcome!

Enjoy.