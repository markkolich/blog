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

### Followup 2015

I submitted my idea for a recursive `listFiles` function as an enhancement request to the folks that maintain SBT:

https://github.com/sbt/sbt/issues/1789

It was quickly closed as "won't fix" but they did point out that you can use `sbt.Path` (a.k.a., `Paths`) to achieve the same behavior without any hacks:

```scala
scala> import sbt._, Path._
import sbt._
import Path._

scala> val base = new java.io.File(".")
base: java.io.File = .

scala> (base ** (-DirectoryFilter)).get
res1: Seq[java.io.File] = ArrayBuffer(./.gitattributes,...
```

Cheers.