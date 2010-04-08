While working on some nifty multi-threaded Java recently, a colleague pointed me to a few really useful Java classes: [CountDownLatch](http://java.sun.com/javase/6/docs/api/java/util/concurrent/CountDownLatch.html) and [CyclicBarrier](http://java.sun.com/javase/6/docs/api/java/util/concurrent/CyclicBarrier.html).  My code was quite typical, a parent worker thread spawns a bunch of children to do real work, and needs to wait for the children to finish before continuing.  The catch though, is that the child worker threads may or may not finish successfully, and in all likelihood will finish at different times.  Even so, the parent thread must wait until all of its children have finished because the parent can only make forward progress once the children are complete.  I [whipped up a little demo](static/entries/understanding-javas-countdownlatch/kolich.com-countdownlatch-swing-example.zip) that spawns five worker threads which update a `JProgressBar` at a random interval.  The demo finishes once each progress bar hits 100%.

### CountDownLatch

Meet `CountDownLatch`.

As described in the Java 6 API docs, a `CountDownLatch` is "a synchronization aid that allows one or more threads to wait until a set of operations being performed in other threads completes."  In other words, the developer says new `CountDownLatch(N)` which waits for `N` threads to finish before the latch is "released" allowing the calling thread to make forward progress.  Couldn't be more perfect here.  To make my life a little easier, I wrote a few wrapper classes that encapsulate a `CountDownLatch` which allow me to easily synchronize on a `List<BaseWorker>`, a list of worker threads:

* [ThreadRunner.java](static/entries/understanding-javas-countdownlatch/ThreadRunner.java) &mdash; A class that accepts a `List<BaseWorker>` (a List of `BaseWorker`'s), creates a `new CountDownLatch(list.size())`, starts each `BasedWorker` then allows the developer to `await()` on the runner for all `BaseWorker`'s to finish.
* [BaseWorker.java](static/entries/understanding-javas-countdownlatch/BaseWorker.java) &mdash; An abstract class that represents each worker thread, and defines a set of methods each `BaseWorker` must implement to be used with a `ThreadRunner`.

So, using these wrappers, let's create a new worker:

```java
public final class MyWorker extends BaseWorker {

  private final int someField_;

  public MyWorker(int worker) {
    super();
    someField_ = worker;
    // ...
  }

  @Override
  public void myRun() throws Exception {
    // ...
  }

  @Override
  public String getWorkerName() {
    return String.format("%s #%s", getClass().getSimpleName(), someField_);
  }

}
```

Now let's setup a new `ThreadRunner` that will take a bunch of `BaseWorker`'s, start them, then wait for all to finish:

```java
public final class MyRunner {

  private static final List<BaseWorker> workers__;
  static {
    workers__ = new ArrayList<BaseWorker>();
    workers__.add(new MyWorker(1));
    workers__.add(new MyWorker(2));
    workers__.add(new MyWorker(3));
  }

  public static void main(String[] args) {
    final ThreadRunner runner = new ThreadRunner(workers__);
    // Start all of the threads in this runner.
    runner.start();
    // Wait for all of the threads to finish.
    runner.await();
    // Did all of our workers complete without error?
    if(runner.wasSuccessful()) {
      System.out.println("All workers finished cleanly.");
    } else {
      System.out.println("Not all workers finished cleanly.");
    }
  }

}
```

In this example, I built a List of `BaseWorker`'s, gave the list to the `ThreadRunner` and asked the runner to start them.  Upon calling `runner.await()`, the `ThreadRunner` blocks waiting for all of the workers to finish.  Note that my concept of "finish" here means either successfully, or unsuccessfully (an `Exception` or `Error` case).  Subsequently, I call `runner.wasSuccessful()` to check if all of the workers finished cleanly, basically asking the runner did all of your workers finish without throwing any `Exception`'s or `Error`'s?

If you're interested, you can [download my complete ThreadRunner demo/example](static/entries/understanding-javas-countdownlatch/kolich.com-countdownlatch-swing-example.zip) that further demonstrates the usage of these wrapper classes using Swing and several `JProgressBar`'s.

### CyclicBarrier

A `CyclicBarrier` is similar to a `CountDownLatch`, except that a `CyclicBarrier` is "a synchronization aid that allows a set of threads to all wait for each other to reach a common barrier point."  Like a `CountDownLatch`, a `CyclicBarrier` can be used to synchronize a number of threads.  But instead of exiting upon completion, theads using a `CyclicBarrier` `await()` for all other threads in the pool to finish.  Here's a usage example of a `CyclicBarrier` built around my `BaseWorker` class:

```java
public final class MyCyclicWorker extends BaseWorker {

  private final CyclicBarrier barrier_;

  public MyWorker(CyclicBarrier barrier) {
    super();
    barrier_ = barrier;
    // ...
  }

  @Override
  public void myRun() throws Exception {
    // ...
    // Wait here for all other threads in the CyclicBarrier to finish.
    barrier_.await();
  }

  @Override
  public String getWorkerName() {
    return getClass().getSimpleName();
  }

}
```

Here's the class that starts up a bunch of these `MyCyclicWorkers`, then runs a single "cleanup" thread once all of the workers are done:

```java
public final class CyclicExample {

  private static final int CYCLIC_THREADS = 5;

  public static void main(String[] args) {
    final CyclicBarrier barrier =
            new CyclicBarrier(CYCLIC_THREADS,
              new Runnable() {
                @Override
                public void run() {
                  // Cleanup thread, or completion thread.
                  // Called when all of the worker threads
                  // are finished.
                  // ...
                }
              });
    for(int i=0; i < CYCLIC_THREADS; ++i) {
      new MyCyclicWorker(barrier).start();
    }
    // ...
  }

}
```

Enjoy.