You're probably familiar with UUID's &mdash; those ubiquitous universally unique identifiers used in just about every modern web-application or web-service.  And, if you're a developer living on the JVM, you're probably close friends with java.util.UUID whether you like it or not.

Generally speaking, UUID's are a convenient way to represent some unique object or entity inside of an application.  After all, they're supposed to be "universally unique" and random enough such that an application can, in theory, generate "random" UUID's forever without any collisions.  In other words, UUID's are represented by a 128-bit number under-the-hood, so the total number of possible UUID's is immense -- 340,282,366,920,938,463,463,374,607,431,768,211,456 unique UUID's to be exact.

No application today could possibly have more than 340,282,366,920,938,463,463,374,607,431,768,211,456 users, or need to store more than 340,282,366,920,938,463,463,374,607,431,768,211,456 objects, right?

So what's the big deal?

In their canonical form, UUID's are represented by 32 hexadecimal digits, displayed in five groups separated by hyphens, in the form 8-4-4-4-12 for a total of 36 characters.  For example:

```scala
scala> import java.util.UUID

scala> UUID.randomUUID
res0: 09bf989f-5b24-47bc-871e-1e824d4f4c60
```

Again, note that UUID's are typically represented by 32-hexadecimal digits, with a canoncial form string length of 36 (including the hyphens).

```scala
scala> UUID.randomUUID.toString.length
res1: Int = 36
```

And therein lies the rub.

Given that UUID's are represented by a series of hexadecimal digits, it occurs to me that appending a long string of leading zeros, or even omitting a leading zero (if present), still results in a **valid UUID**.  For example, 0x0000000A is equivalent to 0x0A, or even 0xA.

That said, these UUID's are logically identical:

```
9bf989f-5b24-47bc-871e-1e824d4f4c60
09bf989f-5b24-47bc-871e-1e824d4f4c60
00000000000000000000000000000009bf989f-5b24-47bc-871e-1e824d4f4c60
```

At least, according to java.util.UUID they are!

OK, so, what's the problem?

Well, consider this: if you use UUID's in the paths to resources in your web-service or web-application, you need to make sure your application (or the framework you're using) does the right thing with egregiously long, or slightly short, UUID's represented in a URI as a String.
