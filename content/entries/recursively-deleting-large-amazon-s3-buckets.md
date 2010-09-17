My first experience using Amazon Web Services for a production quality project was quite fun, and deeply interesting.  I've played with AWS a bit on my own time, but I recently had a chance to really sink my teeth into it and implement production level code that uses AWS as a real platform for an upcoming web, and mobile application.

Perhaps the most interesting, and frustrating, part of this project involved storing hundreds of thousands of objects in an AWS S3 bucket.  If you're not familiar with S3, it's the AWS equivalent to an online storage web-service.  The concept is simple: you create an S3 "bucket" then shove "objects" into the bucket, creating folders where necessary.  Of course, you can also update and delete objects.  If it helps, think of S3 as a pseudo online file-system that's theoretically capable of storing an unlimited amount of data.  Yes, I'm talking Exabytes of data ... theoretically ... if you're willing to pay Amazon for that much storage.

In any event, I created a new S3 bucket and eventually placed hundreds of thousands of objects into it.  S3 handled this with ease.  The problem, however, was when it came time to delete this bucket and all objects inside of it.  Turns out, there is no native S3 API call that recursively deletes an S3 bucket, or renames it for that matter.  I guess Amazon leaves it up to the developer to implement such functionality?

That said, if you need to recursively delete a very large S3 bucket, you really have 2 options: use a tool like [s3funnel](http://code.google.com/p/s3funnel/) or write your own tool that efficiently deletes multiple objects concurrently.  Note that I say concurrently, otherwise you'll waste a lot of time sitting around waiting for a single-threaded delete to remove objects one at a time, which is horribly inefficient.  Well this sounds like a perfect problem for a thread pool and wouldn't you guess it, even a [CountDownLatch](http://mark.koli.ch/2010/04/understanding-javas-countdownlatch.html)!

The idea here is you'll want to spawn multiple threads from a controlled thread pool where each thread is responsible for deleting a single object.  This way, you can delete 20, 30, 100 objects at a time.  Yay for threads!

Here's the **pseudo** code.  Note that I say **pseudo code** because it's not a complete implementation.  This examples assumes you have an AWS S3 implementation (a library) that's able to list objects in a bucket, delete buckets, and delete objects.

```java
package com.kolich.aws.s3.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class RecursiveS3BucketDelete {

  private static final String AWS_ACCESS_KEY_PROPERTY = "aws.key";
  private static final String AWS_SECRET_PROPERTY = "aws.secret";

  /**
   * The -Daws.key and -Daws.secret system properties should
   * be set like this:
   * -Daws.key=AK7895IH1234X2GW12IQ
   * -Daws.secret=1234567890123456789012345678901234456789
   */

  // Set up a new thread pool to delete 20 objects at a time.
  private static final ExecutorService pool__ =
        Executors.newFixedThreadPool(20);

  public static void main(String[] args) {

    final String accessKey = System.getProperty(AWS_ACCESS_KEY_PROPERTY);
    final String secret = System.getProperty(AWS_SECRET_PROPERTY);
    if(accessKey == null || secret == null) {
      throw new IllegalArgumentException("You're missing the " +
          "-Daws.key and -Daws.secret required VM properties.");
    }

    final String bucketName;
    if(args.length < 1) {
      throw new IllegalArgumentException("Missing required " +
          "program argument: bucket name.");
    }
    bucketName = args[0];

    // ... setup your S3 client here.

    List<S3ObjectSummary> objects = null;
    do {
      objects = s3.listObjects(bucketName).getObjectSummaries();
      // Create a new CountDownLatch with a size of how many objects
      // we fetched.  Each worker thread will decrement the latch on
      // completion; the parent waits until all workers are finished
      // before starting a new batch of delete worker threads.
      final CountDownLatch latch = new CountDownLatch(objects.size());
      for(final S3ObjectSummary object : objects) {
        pool__.execute(new Runnable() {
          @Override
          public void run() {
            try {
              s3.deleteObject(bucketName,
                URLEncoder.encode(object.getKey(), "UTF-8"));
            } catch (Exception e) {
              System.err.println(">>>> FAILED to delete object: (" +
                bucketName + ", " + object.getKey()+ ")");
            } finally {
              latch.countDown();
            }
          }
        });
      }
      // Wait here until the current set of threads
      // are done processing.  This prevents us from shoving too
      // many threads into the thread pool; it's a little more
      // controlled this way.
      try {
        System.out.println("Waiting for threads to finish ...");
        // This blocks the parent until all spawned children
        // have finished.
        latch.await();
      } catch (InterruptedException e) { }
    } while(objects != null && !objects.isEmpty());

    pool__.shutdown();

    // Finally, delete the bucket itself.
    try {
      s3.deleteBucket(bucketName);
    } catch (Exception e) {
      System.err.println("Failed to ultimately delete bucket: " +
          bucketName);
    }

  }

}
```

Additional notes, and warnings:

* If you're not familiar with using a CountDownLatch, you can find my [detailed blog post on it here](understanding-javas-countdownlatch.html}.
* If you're going to delete multiple objects at a time, you should confirm the S3 library you're using is thread safe.  Many S3 libraries I've seen rely on the popular [Apache Commons HttpClient](http://hc.apache.org/httpcomponents-client/) to handle the underlying HTTP communication work with S3.  However, you should note that HttpClient isn't thread safe by default, unless you've explicitly set it up to use a [ThreadSafeClientConnManager](http://hc.apache.org/httpcomponents-client/httpclient/apidocs/org/apache/http/impl/conn/tsccm/ThreadSafeClientConnManager.html).