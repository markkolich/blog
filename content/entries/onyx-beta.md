If you [follow me on Twitter](https://twitter.com/markkolich), you may have noticed I silently launched [Onyx](https://onyx.koli.ch) a few weeks ago.  In a nutshell, as described on the Onyx homepage ...

"Onyx is a social file management tool I built to help me keep track of, organize, and share my digital archive. While browsing the web, I tend to accumulate a lot of junk; if I like something, I save it. If I see a cool application of some sort, I'll take a screen shot. If I find a cool song, I'll snag it for later. Or, if I have an important document I need to archive, I'll store it. All of this digital content was sitting around in a relatively unorganized and unsearchable set of files and directories on a local file system. Onyx is my solution to this digital content clutter problem. Files and bookmarks uploaded into Onyx can be protected, searched, organized and shared much easier than a set of files and directories on my local disk."

Yep.

Onyx was a chance for me to "cross off" an important task on my digital "TODO" list that's been hanging over my head for a while: organize and archive all of my digital crap.  It also gave me a chance to play with some new technologies I've been wanting to integrate into a real project for quite a while, like jQuery UI's [draggable](http://jqueryui.com/demos/draggable/) and [droppable](http://jqueryui.com/demos/droppable/).  I also learned how to [base-36 encode numbers](base36-encoding-for-tiny-urls-with-php) for a tiny URL.

In the last 24-hours, I finished uploading all of my personal, and public, digital content into Onyx which [you can browse here](https://onyx.koli.ch/get/1/mark) from my Onyx home directory.  Of course, like any good file management solution, my personal/private files are protected.  What you'll see in my home directory are files and folders I've allowed the public to view.

For the curious software engineer, Onyx is written entirely in PHP running on Apache 2.2.3.  I'm also using a clever little `mod_rewrite` hack in Apache to drop the .php on each Onyx URL.  Dropping the .php makes my URL's look a little cleaner.  You may also ask why I named this project "Onyx".  As [described here on Wikipedia](http://en.wikipedia.org/wiki/Onyx), Onyx is a type of colorful layered quartz which contains bands of almost every color.  This colorful layering reminded me of the layered structure of a file system: files, folders, bookmarks, etc. all mashed together.  Hence, Onyx.

If you'd like to read a little more about my Onyx project, [you might find this post interesting](onyx-my-custom-solution-to-the-digital-clutter-problem).

Cheers.