Concurrency is difficult, and generally tough to get right.  Fortunately, there are tools that can *somewhat* ease this pain.  For instance, take Java's [ReentrantReadWriteLock](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html) &mdash; a useful and foundational class that helps any highly concurrent Java application manage a set of readers and writers that need to access a critical block of code.  When using a `ReentrantReadWriteLock` you can have any number of simultaneous readers, but the **write lock** is exclusive.  In other words:

* If any thread holds the write lock, all readers are forced to wait (or fail hard) until the thread that holds the write lock releases the lock.
* If the write lock is *not* held, any number of readers are allowed to access the protected critical block concurrently &mdash; and any incoming writers are forced to wait (or fail hard) until all readers are done.

In short, this is the classic [ReadWriteLock](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReadWriteLock.html) paradigm.

This is great, except that a vanilla `ReentrantReadWriteLock` is missing a few key features:

1. Conditionally wait, or fail immediately, if the desired lock is not available.  In other words, let me define upfront what I want to do if the lock I want to "grab" is not available &mdash; fail now, or wait indefinitely?
2. And, execute a callback function only upon successful execution of a transaction.  Here, we define a **transaction** to mean successfully acquiring the lock, doing work (without failure), and releasing the lock.

I wanted these features, so I implemented [Bolt](https://github.com/markkolich/kolich-bolt) &mdash; a very tiny wrapper around Java's `ReentrantReadWriteLock` with better wait, cleaner fail, and transactional callback support.

### LockableEntity

Using [Bolt](https://github.com/markkolich/kolich-bolt), any entity or object you want to protect should implement the [LockableEntity](https://github.com/markkolich/kolich-bolt/blob/master/src/main/java/com/kolich/bolt/LockableEntity.java) interface.

```java
import com.kolich.bolt.LockableEntity;
import java.util.concurrent.locks.ReadWriteLock;

public final class Foobar implements LockableEntity {

  private final ReadWriteLock lock_;

  public Foobar() {
    lock_ = new ReadWriteLock();
  }

  @Override
  public ReadWriteLock getLock() {
    return lock_;
  }

}
```

Now, let's create an instance of this example entity which we will use to protect a critical section of code within a **transaction**.

```java
public static final Foobar foo = new Foobar();
```

This instance, `foo`, is used below throughout my examples.

### Read Lock, Fail Immediately

First, let's grab a shared read lock on `foo`, but fail immediately with a `LockConflictException` if the **write lock** is already acquired by another thread.

```java
new ReentrantReadWriteEntityLock<T>(foo) {
  @Override
  public T transaction() throws Exception {
    // ... do read only work.
    return baz;
  }
}.read(false); // Fail immediately if read lock is not available
```

Note that `read` asks for a shared reader lock &mdash; the lock will be granted if and only if there are no threads holding a write lock on `foo`. There very well may be other reader threads.

### Read Lock, Block/Wait Forever

Next, let's grab a shared read lock on `foo`, but block/wait forever for the read lock to become available.  Execute the `success` callback if and only if the `transaction` method finished cleanly without exception.

Note the implementation of the `success` method is completely optional.

```java
new ReentrantReadWriteEntityLock<T>(foo) {
  @Override
  public T transaction() throws Exception {
    // ... do read only work.
    return baz;
  }
  @Override
  public T success(final T t) throws Exception {
    // Only called if transaction() finished cleanly without exception
    return t;
  }
}.read(); // Wait forever
```

It is very important to note that the underlying lock is held, while the `success` method is called.  That is, the acquired lock isn't released until the `transaction` and `success` method are finished.

### Write Lock, Fail Immediately

Grab an exclusive **write lock** on `foo`, or fail immediately with a `LockConflictException` if a write or read lock is already acquired by another thread.  Further, execute the `success` callback method if and only if the `transaction` method finished cleanly without exception.

```java
new ReentrantReadWriteEntityLock<T>(foo) {
  @Override
  public T transaction() throws Exception {
    // ... do read or write work, safely.
    return baz;
  }
  @Override
  public T success(final T t) throws Exception {
    // Only called if transaction() finished cleanly without exception
    return t;
  }
}.write(); // Fail immediately if write lock not available
```

### Write Lock, Block/Wait Forever

Grab an exclusive **write lock** on `foo`, or block/wait forever for all readers to finish.

```java
new ReentrantReadWriteEntityLock<T>(foo) {
  @Override
  public T transaction() throws Exception {
    // ... do read or write work, safely.
    return baz;
  }
}.write(true); // Wait forever
```

### An Example

The [Havalo project](https://github.com/markkolich/havalo) makes extensive real-world use of this locking mechanism, as a way to manage shared entities that may be concurrently accessed by any number of threads.  Havalo is a lightweight key-value store written in Java.  Internally, it maintains a collection of repositories and objects, and uses *Bolt* to conditionally gate access to these objects in local memory.

### GitHub

Bolt is free, and open source, on GitHub:

https://github.com/markkolich/kolich-bolt

Pull requests welcome.