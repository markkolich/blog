A blog reader recently contacted me with an interesting question: can you explicitly tell Java when and where to flush its permgen and heap to disk?  The answer, based on what I understand about Java and operating system fundamentals, is **no**.

I can say with much certainty that you can't control where Java saves its heap and permgen (either on disk or in memory).  Java itself doesn't know about paging stuff out to disk.  It simply asks the operating system for the memory it needs and if the OS can't give it, then either the OS has to fail the request or make room by paging out unused chunks of memory to swap.  In other words, Java relies on the host OS to handle this type of stuff.

But what if you're dealing with millions of Java objects on a standard computer and you don't have room to keep all of those objects in physical memory?  In this case, your only real option is to write code that manually swaps objects in/out of the disk.  Of course, this requires that you implement your own swapping mechanism, which isn't too bad.  When your Java application needs a set of objects, it loads what it needs into memory from disk, does some stuff with the objects, then writes them back out to disk.

Meet `java.io.Serializable` &mdash; http://java.sun.com/javase/6/docs/api/java/io/Serializable.html

Here's an example:

```java
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Dog implements Serializable {

  private static final long serialVersionUID = -4367737315167700936L;

  private String name_;
  private String breed_;

  public Dog (String name, String breed) {
    this.name_ = name;
    this.breed_ = breed;
  }

  @Override
  public String toString() {
    return String.format("%s:%s", this.name_, this.breed_);
  }

  public static void main (String [] args) {

    final List<Dog> dogs = new ArrayList<Dog>();
    dogs.add( new Dog("Fido", "mutt") );
    dogs.add( new Dog("Clifford", "big red dog") );

    ByteArrayOutputStream os = null;
    ObjectOutputStream out = null;
    for( Dog d : dogs ) {
      try {

        // To write the dogs out to a file, you'll of course
        // need to use a FileOutputStream instead of a
        // ByteArrayOutputStream
        os = new ByteArrayOutputStream();
        out = new ObjectOutputStream(os);
        out.writeObject(d);

        // Print the serialized version of Dog
        final String serialized = os.toString();
        System.out.println(d.toString() + " serialized is: " + serialized);

      } catch (Exception e) {
        e.printStackTrace(System.err);
      } finally {
        closeQuietly(os);
        closeQuietly(out);
      }
    }

  }

  private static final void closeQuietly(final OutputStream os) {
    try {
      os.close();
    } catch (Exception e) {  }
  }

}
```

Each of the objects you wish to save to disk will have to implement java.io.Serializable.  This will let you convert a Java object into something that can be written out to disk.  From there, you will have to write some type of queue or stack control mechanism that will know when, from where, and how to page these objects in and out of the disk.