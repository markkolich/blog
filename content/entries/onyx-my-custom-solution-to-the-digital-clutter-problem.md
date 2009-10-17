In my spare time over the last several weeks, I've been hammering out a new personal project I've had on my to-do list for quite a while.  I named it [Onyx](https://onyx.koli.ch).  Onyx is my solution to an ongoing and often frustrating "digital content clutter" problem.

### Why

While browsing the web, I tend to accumulate a lot of junk; if I like something, I save it. If I see a cool application of some sort, I'll take a screen shot. If I find a cool song, I'll snag it for later. Or, if I have an important document I need to archive, I'll store it. As it turns out, all of this digital content was sitting around in a relatively unorganized, unsearchable, and unsharable set of files and directories on a local file system.

Onyx is my solution to this problem. Files uploaded into Onyx can be protected, searched, organized, and shared much easier than a set of files and directories sitting on my local disk.  Plus, the file storage itself is hosted on my web-server "in the cloud", which means that I can access it using any decent web-enabled device.  And, when necessary, I can easily share files and directories in Onyx with friends, family, or co-workers via Twitter, email, or instant messenger.

### How

Onyx is a web-app written in PHP 5 with a MySQL back end.  Data uploaded into Onyx is physically stored in the "cloud" on the web-server file system.  This proved to be an interesting and [challenging atomic transaction problem](http://en.wikipedia.org/wiki/Atomicity_%28database_systems%29).  Because I'm using a MySQL database AND a file system to organize and store files in Onyx, I have to make sure that my database and file system stay in sync.  I could have used a BLOB to store the file data itself inside of my Onyx MySQL database, but I avoided that because there may be cases where I want to access the uploaded files without a database (e.g., if my database crashes, or if I do something stupid, I don't want to lose all of my files in a database table).

### Where

Onyx is available at http://onyx.koli.ch.

### The Home Screen

I'm not a formal user-interface expert, but I built the Onyx UI to be what I think is clean, simple, and elegant.  I settled on a dark gray minimalist style color theme after experimenting with several color options.  The home screen, as shown below, displays a convenient list of recently added files, newest on top.  The "top five" users, determined by the number of files uploaded, are shown under the recently added files list. Note that only "public" files are shown in the recent files list &mdash; files and folders marked private by the user are not visible or accessible to the public.

<img src="static/entries/onyx-my-custom-solution-to-the-digital-clutter-problem/onyx-homescreen-thumb-400x376.jpg" width="400">

### Adding a Directory and Uploading Files

Once logged in, users are allowed to create folders and upload files. Again, note that users can label folders as public, or private. Public folders, and their public contents are visible to the world. On the other hand, private folders hide their contents.  Onyx understands hierarchical permissions; if the parent of a file or folder in the file structure is labeled private, the child automatically inherits the parents' visibility unless explicitly overridden by the user.

<img src="static/entries/onyx-my-custom-solution-to-the-digital-clutter-problem/onyx-new-folder-thumb-400x188.jpg" width="400">

<img src="static/entries/onyx-my-custom-solution-to-the-digital-clutter-problem/onyx-upload-thumb-400x323.jpg" width="400">

### Sharing Content

Files uploaded to the "Onyx cloud" can be easily shared. From the file list browser, users can simply mouse over any file, and click the "Share on Twitter" or "Get Permalink" icons.  Of course, users can also delete, or download their files by clicking the respective delete or download icons in the file list.  If the user deletes a folder, all files and folders under that folder are also deleted automatically.  Registered users can only delete files they own: files they've uploaded, or folders they've created.

<img src="static/entries/onyx-my-custom-solution-to-the-digital-clutter-problem/onyx-share-shot-thumb-400x223.jpg" width="400">

### Permalinks and Twitter Integration

Users can instantly snag permalinks for any file or folder in Onyx.  Onyx permalinks can be shared via email, instant messenger, Facebook, Twitter, Digg, or any other instant communication or link aggregation service.  As shown below, I loaded a permalink for a picture of my wife and I.  I then clicked the "Tweet It!" button which redirected me to Twitter.  Notice my Twitter "What are you doing?" status box is automatically populated with the file name, file description, and permalink from Onyx.  One click later, my picture stored in Onyx is instantly shared with my followers on Twitter.

<img src="static/entries/onyx-my-custom-solution-to-the-digital-clutter-problem/onyx-permalink-share-thumb-400x228.jpg" width="400">

Enjoy.