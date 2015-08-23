# mark.koli.ch

This is my blog.  There are many like it, but this one is mine.

The content and code herein is documented with reasonable depth in this blog post:

http://mark.koli.ch/a-new-blog-platform-backed-by-github-twitter-bootstrap-and-curacao

This custom blogging platform is built with:

* Java 8
* GitHub and [JGit](http://www.eclipse.org/jgit/)
* [Curacao](https://github.com/markkolich/curacao)
* [Markdown](http://daringfireball.net/projects/markdown/) with [Pegdown](https://github.com/sirthias/pegdown)
* [Twitter Bootstrap](http://getbootstrap.com/)

## Bootstrap

This project is built and managed using Apache Maven.

To clone, build, and run this project you must have <a href="https://maven.apache.org">Maven installed and configured</a>.

To begin, clone the repository.

    #~> git clone https://github.com/markkolich/blog.git

Run `mvn jetty:run` from within your newly cloned *blog* directory to start the local servlet container. 

    #~> cd blog
    #~/blog> mvn jetty:run

In your nearest web-browser, visit <a href="http://localhost:8080/blog">http://localhost:8080/blog</a>.

To stop the development server, press `Ctrl-C`.

To build a deployable WAR for your favorite servlet container, run `mvn package`.

    #~/blog> mvn package

The resulting WAR is placed into the local *dist* directory.  Deploy and enjoy.

## Licensing

Copyright (c) 2015 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this project is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/blog/blob/master/LICENSE">LICENSE</a> for details.
