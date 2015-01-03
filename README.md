# mark.koli.ch

This is my blog.  There are many like it, but this one is mine.

The content and code herein is documented with reasonable depth in this blog post:

http://mark.koli.ch/a-new-blog-platform-backed-by-github-twitter-bootstrap-and-curacao

This blogging platform is built around:

* Java 7
* GitHub and [JGit](http://www.eclipse.org/jgit/)
* [Curacao](https://github.com/markkolich/curacao)
* [Markdown](http://daringfireball.net/projects/markdown/) with [Pegdown](https://github.com/sirthias/pegdown)
* [Twitter Bootstrap](http://getbootstrap.com/)

## Bootstrap

This project is built and managed using <a href="http://www.scala-sbt.org">SBT</a>.

To clone and build this project, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT installed and configured</a>.

To start, clone the repository.

    #~> git clone git://github.com/markkolich/blog.git

Run `sbt` from within your newly cloned *blog* directory.

    #~> cd blog
    #~/blog> sbt
    ...
    blog:1.0-SNAPSHOT>

You will see a `blog` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `start` to start the local Servlet container.  By default the server listens on **port 8080**.

    blog:1.0-SNAPSHOT> start
    [info] jetty-9.1.0.v20131115
    [info] Started ServerConnector@369620a9{HTTP/1.1}{0.0.0.0:8080}

In your nearest web-browser, visit <a href="http://localhost:8080/blog">http://localhost:8080/blog</a> and you should see a local version of this blog in your browser.

To stop the development server, run `stop`.

To build a deployable WAR for your favorite Servlet container, run `war` in SBT.

    blog:1.0-SNAPSHOT> war
    Compiling CSS...
    Compiling JavaScript...
    Compiling 2 file(s) with 42 extern(s)
    0 error(s), 0 warning(s)
    [info] Packaging ~/dev/github/blog/dist/blog-1.0-SNAPSHOT.war ...
    [info] Done packaging.
    [success] Total time: 3 s, completed Feb 3, 2014 6:26:07 PM

Note the resulting WAR is placed into the **blog/dist** directory.  Deploy and enjoy.

To create an Intellij IDEA project for this codebase, run `gen-idea` in SBT.

    blog:1.0-SNAPSHOT> gen-idea

Open the resulting project using your favorite IntelliJ IDEA instance.

## Licensing

Copyright (c) 2015 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/blog/blob/master/LICENSE">LICENSE</a> for details.
