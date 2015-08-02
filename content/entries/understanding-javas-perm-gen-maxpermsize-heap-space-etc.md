During my travels at work, I've come across a few interesting memory management issues in Java.  My team has deployed several large web-applications in a single instance of [Apache Tomcat](http://tomcat.apache.org/).  The Linux box running these applications only has about 2GB of physical memory available.  Once the apps are deployed, about 1.8 GB of system memory is consumed by Java alone.  Clearly, we need to improve our memory management a bit.

However, I took a few minutes to do some digging on Java's Permanent Generation (Perm Gen) and how it relates to the Java heap.  Here are some distilled notes from my research that you may find useful when debugging memory management issues in Java:

JVM argument `-Xmx` defines the **maximum heap size**.  The arg `-Xms` defines the **initial heap size**.  For example:

```
-Xmx4g -Xms512m
```

In Tomcat land, these settings would go in your `startup.sh` or init script, depending on how you start and run Tomcat.  With regards to the `MaxPermSize`, this argument adjusts the size of the "permanent generation."  As I understand it, the perm gen holds information about the "stuff" in the heap.  So, the heap stores the objects and the perm gen keeps information about the "stuff" inside of it.  Consequently, the larger the heap, the larger the perm gen needs to be.

Here is an example showing how you might use `MaxPermSize`:

```
-XX:MaxPermSize=512m
```

### Additional Notes

* Use the JVM options `-XX:+TraceClassLoading` and `-XX:+TraceClassUnloading` to see what classes are loaded/un-loaded in real-time.  If you have doubts about excessive class loading in your app; this might help you find out exactly what classes are loaded and where.
* Use `-XX:+UseParallelGC` to tell the JVM to use multi-threaded, one thread per CPU, garbage collection.  This might improve GC performance since the default garbage collector is single-threaded.  Define the number of GC threads to use with the `-XX:ParallelGCThreads=N` option where `N` is the number of GC threads you wish to consume.
* Never call `System.gc()` in your code.  The application doesn't know the best time to garbage-collect, only the JVM really does.
* The JVM option `-XX:+AggressiveHeap` inspects the machine resources (size of memory and number of processors) and attempts to set various heap and memory parameters to be optimal for long-running, memory allocation-intensive jobs.

Cheers.

<!--- tags: java -->