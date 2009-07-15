If you've ever looked at the code for some of the more popular mobile web-portals, you might have noticed that many of them include [base-64 encoded](http://en.wikipedia.org/wiki/Base64) binary image data in their cascading style sheets.  For example, Google's iPhone portal uses this content delivery mechanism to deliver image content to mobile browsers.  Here's an example using an inline style sheet to define the background-image of a `<div>`.  Note that the GIF image data itself is embedded inside of the CSS:

```html
<style type="text/css">
 div.wrapper {
   background-image: url(data:image/gif;base64,R0lGODdhAQAgAMIIABstehsv
      fRwwgR4zhB81iiA3jSE6kvf6/ywAAAAAAQAgAAADDji21U1wSClqDRjow08CADs=);
 }
</style>
```

I'm line-wrapping the base-64 encoding binary data above.  However, normally you don't want to line-wrap binary data when including it in an inline CSS declaration.  I'm simply wrapping above for display purposes.

As defined by the [RFC 2397](http://tools.ietf.org/html/rfc2397), the accepted syntax/format of data URI's are as follows:

```
data:[<mediatype>][;base64],<data>
```

### Why Is This Important/Useful?

You might ask, why would I ever care about this?  Simple: if done correctly, embedding binary image data in your CSS can dramatically improve page loading time on mobile devices.  For example, when a browser downloads and renders a page, it first downloads the HTML.  After parsing the HTML and accompanying CSS, it then begins to load all of the resources that make up the page itself.  These resources may include scripts, other CSS files, and of course, images.  For each additional resource the browser needs to load, it opens another HTTP connection to the web-server, which may or may not be an expensive operation.  If a wireless network is extremely busy, the mobile device may have to wait a few cycles before it can transmit its request.  Further, the device may also need to wait a bit for a response.  All the while, the user is staring at their mobile device waiting for your page to load.

To avoid this HTTP connection overhead on slower wireless networks, many mobile web-portals embed static binary image data directly in their CSS.  This can be very beneficial, because when the device downloads and renders the HTML and inline CSS, it doesn't need to open any additional connections over a slow wireless network.  In other words, everything the device needs to render the page in its browser is downloaded through a single HTTP request/response.  TCP transfers tend to start slowly, so this method helps alleviate the connection cycle bottleneck.

This type of optimization is one of the many reasons why popular mobile portals like Google's iPhone homepage appears to load so quickly on a cell-network.

### Base-64 Encoding Images

Before you run out and redo your entire site with images embedded in your CSS, you should know that on average base64-encoded data takes about 33% more space than the original data.  So if your site is ridiculously image heavy, base-64 encoding your image data isn't going to help; in fact, it might even make things worse.  Ideally, you should only base-64 encode and embed image content that's used repeatedly across your site.  Static icons or hover images for an on-mouse-over effect are often ideal candidates.

If your mobile site is [using HTTP compression](howto-use-apache-mod-deflate-to-compress-web-content-obsessed-with-speed-of-kolichcommobi), then you may not even see much of a jump in bandwidth usage even if base-64 encoded images use up to 33% more bandwidth.

In PHP, you can base-64 encode an image using the `base64_encode` function.  Here's a quick PHP app I wrote up in a few minutes that should work nicely:

```php
<?php

// NOTE: This script is probably not fool proof, so there might be some
// holes in it. I would not recommend you place this app in any publicly
// accessible directory on your web-server. Use at your own risk.

$uploadedFile = (isset($_FILES['uploadedfile']))
                        ? $_FILES['uploadedfile'] : null;

if(empty($uploadedFile)){
 $self = $_SERVER['PHP_SELF'];
 ?>
 <html>
  <body>
  <h2>Base-64 Encode An Uploaded File</h2>
  <form enctype="multipart/form-data" action="<?= $self; ?>" method="POST">
    <input type="hidden" name="MAX_FILE_SIZE" value="100000" />
    Choose a file to upload: <input name="uploadedfile" type="file" /><br />
    <input type="submit" value="Encode File" />
  </form>
  </body>
 </html>
 <?
}
else {

 $fileToEncode = $_FILES['uploadedfile']['tmp_name'];
 $fp = @fopen($fileToEncode,"r");
 $buffer = "";
 if(!$fp){
    echo "ERROR opening file on server.";
 } else {
    while(!feof($fp)) {
      $buffer .= fgets($fp,4096);
    }
 }
 @fclose($fp);

 // Use the base64_encode() function to base-64
 // encode the file contents.
 echo base64_encode($buffer);

}

?>
```

If you have access to a Linux box with a decent distribution on it, you can use the `base64` command to encode an image file.  The `base64` command is included with the GNU coreutils package:

```
#/> base64 -w0 img.jpg > img.b64
```

The `-w0` argument instructs the `base64` command to disable line-wrapping.  When including binary image data in your CSS, you cannot include line breaks.

### Browser Support

IE 8, IE9, IE10, Firefox 3+, Safari, Mobile Safari (iPhone browsers), and Google Chrome support embedded binary image data in CSS files.  Not surprisingly, IE 6 and 7 does NOT.  This means that you probably shouldn't use inline binary image data in your CSS for all versions of your mobile and non-mobile site.  If you run a mobile site that's just for the iPhone, or another browser that supports inline binary image data, then you should be fine.

### Additional Notes

Note that the [RFC 2397](http://tools.ietf.org/html/rfc2397) data URI scheme can be used on `<img>`'s too:

```html
<img src="data:image/png;base64,
iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAABGdBTUEAALGP
C/xhBQAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB9YGARc5KB0XV+IA
AAAddEVYdENvbW1lbnQAQ3JlYXRlZCB3aXRoIFRoZSBHSU1Q72QlbgAAAF1J
REFUGNO9zL0NglAAxPEfdLTs4BZM4DIO4C7OwQg2JoQ9LE1exdlYvBBeZ7jq
ch9//q1uH4TLzw4d6+ErXMMcXuHWxId3KOETnnXXV6MJpcq2MLaI97CER3N0
vr4MkhoXe0rZigAAAABJRU5ErkJggg==" alt="Red dot" />
```

Remember, you can pretty much use the data URI scheme anywhere in a browser that supports it.  If you have a mobile web-portal, and want to improve the loading time of your pages, this might be a useful enhancement to consider.

### Conclusion

Even though base-64 encoded images could use up to 33% more bandwidth, many mobile portal operators have decided that the extra bandwidth usage is a small price to pay to save a few costly connection cycles on a slow wireless network.  Given the poor latency of wireless cell networks, sending several extra bytes in a single transfer is often faster than sending small chunks of bytes across multiple transfers.

Enjoy.