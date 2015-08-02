Common problem: I need to share a bunch of files with a friend, or co-worker, but I can't send them via email because the files are greater than 10MB in total size.  I could send them to a local web-server, or post them on kolich.com, but that involves starting a new SSH session, some SCP's, what a mess.  I wish I had a really lightweight simple web-server that I can simply copy into any directory, and start with one command.  Once started, the web-server will simply serve files right from the directory I started it from (e.g., the web-server root becomes the current working directory).  Then I can tell my co-workers to visit `http://somehost:8080`, for example, to download the files.

A few weeks ago, I went through a Restlet tutorial for a project at work, and knew that Restlet supported its own internal HTTP web-server for serving up static files.  I wanted to learn a little more about this, so I created Cappuccino.  Cappuccino is a lightweight server that uses Restlet's internal HTTP web-server to serve up static files from the directory it's started from.  Technically speaking, Cappuccino is just a handsome wrapper of Restlet's internal HTTP server.

Using [Simon Tuff's one-jar](http://one-jar.sourceforge.net/), I packaged all required libraries and resources into a single `.jar` file.  As a result, you can start Cappuccino with a one-liner (assuming you have a good JRE installed in your `$PATH`):

```
#/> java -jar cappuccino.jar
```

Once started, Cappuccino serves up static files in the current directory on port 8080 by default.  Of course, you can change the default port:

```
#/> java -jar cappuccino.jar 8099
```

### On GitHub

Cappuccino is available on GitHub https://github.com/markkolich/cappuccino.

<!--- tags: restlet -->