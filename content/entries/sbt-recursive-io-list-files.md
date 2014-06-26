Annoyingly, SBT's very own [sbt.IO](http://www.scala-sbt.org/0.13.5/api/index.html#sbt.IO$) util `Object` doesn't provide a mechanism to recursively list files in a directory.

As of SBT 0.13.5, the three `listFiles` functions it does implement are only somewhat useful for complex builds.

* `def listFiles(dir: File): Array[File]`
* `def listFiles(dir: File, filter: java.io.FileFilter): Array[File]`
* `def listFiles(filter: java.io.FileFilter)(dir: File): Array[File]`

Meh.

Perhaps more frustrating is that `sbt.IO` is an `Object` (a singleton) which by its very nature in Scala means it cannot be extended.  So, even if I wanted to `extend sbt.IO` and override to make it recursive, I can't. 

So, here's how one can recursively list files in a directory leveraging SBT's `sbt.IO.listFiles`:

```scala
trait IOHelpers {
  def listFilesRecursively(dir: File): Seq[File] = {
    val list = IO.listFiles(dir)
    list.filter(_.isFile) ++ list.filter(_.isDirectory).flatMap(listFilesRecursively)
  }
}
```

Functional programming for-the-win!

Cheers.