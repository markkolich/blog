With a little spare time on my hands, I dug into [Google's Closure Compiler](http://code.google.com/closure/compiler/), an open-source tool that minifies and optimizes JavaScript for better performance.  Unlike other JavaScript compression tools like [JSMin](http://www.crockford.com/javascript/jsmin.html), [Packer](http://dean.edwards.name/packer/) or the [YUI compressor](http://developer.yahoo.com/yui/compressor/), Google's Closure Compiler converts your decent JavaScript into "better" JavaScript.  It does so by searching for and pointing out obvious JavaScript pitfalls, deleting dead code, and optimizing what's left to make your web-applications more efficient.  This all sounds great, so I gave it a whirl and eventually integrated it into my Ant build for a new project.  Heck, I even [found a bug](http://code.google.com/p/closure-compiler/issues/detail?id=221) and [suggested an enhancement](http://code.google.com/p/closure-compiler/issues/detail?id=220).

### $jQuery Users be Warned

Before you run off and have a field day with the Closure Compiler, you should know that optimizing jQuery with the [compiler's ADVANCED_OPTIMIZATIONS](http://code.google.com/closure/compiler/docs/api-tutorial3.html) mode failed miserably.  Even with a proper [JS externs](http://code.google.com/closure/compiler/docs/api-tutorial3.html#externs) file, I could not get jQuery, or my JavaScript that uses jQuery, to compile under ADVANCED_OPTIMIZATIONS mode.  Ok, actually it compiled fine, but when I tried to run this JavaScript in a browser: welcome to JavaScript Error City, population one.

Eventually, I gave up and reverted back to SIMPLE_OPTIMIZATIONS mode instead.  This works much better.  If you know something I don't about compiling jQuery in advanced mode, please let me know.

### Get 'er done

First things first, you'll need to [download the Closure Compiler JAR](http://code.google.com/p/closure-compiler/downloads/list) and integrate it into your Ant build file by defining a new Ant task:

```xml
<property name="jscompjar"
  location="${lib.dir}/google-closure-compiler.jar" />

<taskdef name="jscomp"
  classname="com.google.javascript.jscomp.ant.CompileTask"
  classpath="${jscompjar}"/>
```

Yay, now you have a `<jscomp>` Ant task.

### Concatenate

Next, you'll need Ant to concatenate each of your JavaScript files into a single file.  Technically, you don't have to do this, but I found it much easier and I like having all of my JavaScript compressed into a single resource:

```xml
<concat destfile="${build.dir}/js/project.js">
  <filelist dir="${source.js.dir}">
    <file name="underscore-1.0.4.min.js" />
    <file name="jquery-1.4.2.min.js" />
    <file name="jquery-ui-1.8.4.min.js" />
    <file name="project.util.js" />
    <file name="project.js" />
  </filelist>
</concat>
```

Note that I'm using a [FileList](http://ant.apache.org/manual/Types/filelist.html) instead of a [FileSet](http://ant.apache.org/manual/Types/fileset.html).  This is because the order in which the resources are concatenated is important; they should be concatenated in the order in which they are required, and using a FileList ensures proper order.

Also, note that if you eventually plan on compiling your JavaScript with ADVANCED_OPTIMIZATIONS enabled, you'll need to concatenate your JavaScript files together anyways.  Further, using a single JavaScript resource across your entire site has obvious performance advantages.  HTTP operations are usually expensive (especially on mobile devices), so having a web-browser download one large JavaScript file with everything in it is usually better than having it download multiple smaller files.  Of course, you could even take it a step further and use a tool like [LABjs to load your JavaScript dynamically](http://blog.getify.com/labjs-why-not-just-concat), as needed.

### Compile

Ok, now that your JavaScript files have been concatenated into a single resource, let's use the `<jscomp>` Ant task we defined earlier to compile it:

```xml
<jscomp compilationLevel="simple" warning="quiet"
  debug="false" output="${release.js.dir}/project.min.js">
  <sources dir="${build.dir}/js">
    <file name="project.js" />
  </sources>
</jscomp>
```

Assuming your code compiled cleanly, you'll have a Closure Compiler optimized JavaScript file sitting around ready for deployment!