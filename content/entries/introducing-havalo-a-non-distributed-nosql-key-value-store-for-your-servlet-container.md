Someone recently asked me, "why spend time building your own key-value store when other trusted solutions like Redis, Mongo, and CouchDB are available off-the-shelf?"

Because I can!

### Some History

It all started last year when I began building my wedding web-site.  My wife and I tied the knot in September 2012, and I was chartered with building a web-site for the big day.  Naturally, being a tech savvy couple, we opted to allow our guests to RSVP online on our wedding web-site.  Steak, or fish?

So, the search for a data store began.

I had enough of JDBC, and the monstrosities that came with it -- that is, Hibernate and iBATIS.  And, I had no intention of firing up a traditional database to store something relatively trivial like a set of RSVP's to a wedding.  Further, given that my wedding web-site was built on Spring 3.2 and ran in a traditional Servlet container, I yearned for a solution that wouldn't require any additional services or deployment steps.  I wanted a data store that would fire up along side of my existing web-applications whenever I started my Servlet container, and just work.

### Meet Havalo

[Oh, hey!](https://github.com/markkolich/havalo-kvs "Havalo at GitHub")

Written in Java 7, Havalo is a zero configuration, non-distributed NoSQL key-value store that runs in any Servlet 3.0 compatible container.

Sometimes you just need fast NoSQL storage, but don't need full redundancy and scalability (that's right, `localhost` will do just fine). With Havalo, simply drop a WAR into your favorite Servlet 3.0 compatible container and with almost no configuration you'll have access to a fast and lightweight K,V store backed by any local mount point for persistent storage. And, Havalo has a pleasantly simple RESTful API for your added enjoyment.

Havalo is built around raw asynchronous Servlets and runs inside of any Servlet 3.0 container.  Further, Havalo does not use any bloated frameworks or toolkits, it is as minimal and as lightweight as I could build it.

Oh, and [Havalo is open source](https://github.com/markkolich/havalo-kvs "Havalo at GitHub"), licensed for free to the world under the popular MIT License.

### Technical Challenges

There were several technical challenges I overcame while I built Havalo:

* **Resource Locking** - When you "PUT" (upload) an object into Havalo, the object is eventually saved to a disk platter as a vanilla file.  Attempting to retrieve this file later in a multi-threaded application lead to strange behavior depending on which file system and operating system you were using.  For example, on Windows (NTFS), if one thread in the JVM has a open FileInputStream to a file on disk but another thread in the same JVM comes along and tries to delete that file, the delete will immediately fail.  However, on Linux (ext3), the delete will succeed but the file will not be physically removed from the platters until all open file handles pointing to the file are closed.  To work around this inconsistent behavior, Havalo does not rely on the file system to manage resource locking.  Instead, Havalo uses [kolich-bolt](https://github.com/markkolich/kolich-bolt "kolich-bolt at GitHub") internally to manage a set of `ReentrantReadWriteLock`'s in memory, which manages reader/writer access to the raw object files on disk.

* **In Memory Indexing** - I spent quite a while trying to identify the right data structures to use internally that would allow fast object insertion and very fast searching of existing object keys.  For example, say a user inserted three objects into their bucket, with keys "foo", "foobar", and "dog".  I needed a data structure that would let me quickly find objects that started with a given prefix.  In this case, if a user asked for all objects that started with "fo", naturally Havalo would return "foo", and "foobar".  In the end, I used a Trie data structure, which worked beautifully.  In fact, Havalo uses the patricia-trie implementation exclusively.

* **Filename and filename length** - If you've ever dealt with an application that saves files to disk, you may be familiar with the restrictions file systems place on the length of your filenames.  That is, the length of the path to a file on disk.  Most modern file systems, including ext3 and NTFS, strictly enforce a maximum filename length of 255-bytes.  So, if Havalo was to accept objects identified by keys longer than 255-bytes, a workaround was required.  Fortunately, this was easily solved by base-32 encoding a hash of the given key to produce a normalized filename.  Yes, that's not a typo, base-32.  This has an added benefit, in that base-32 only uses the digits 0-9 and uppercase letters A-Z.  As a result, this worked around buggy file systems that ignore filename case (e.g., on NTFS a file named "xyz" is the same as "XyZ").  So, using filenames that only contain the digits 0-9 and uppercase letters A-Z completely dances around this problem.

* **Java Client Bindings** - The Havalo API is a RESTful service which deals exclusively in JSON.  To facilitate adoption of the API, literally to make it easier to consume the service, I wrote a robust set of Java bindings (a client) for the Havalo API.  You'll find the `havalo-kvs-client` project is an easy off-the-shelf solution which can be integrated into your own application.

### Download It

You can find instructions and additional technical details on how to download and configure Havalo for your own environment on my Havalo GitHub page at [https://github.com/markkolich/havalo-kvs](https://github.com/markkolich/havalo-kvs).

Havalo is open source, so you are always welcome to browse the source and submit a pull request if you'd like to contribute.

Enjoy!

<!--- tags: havalo-kvs, curacao, servlet, java -->