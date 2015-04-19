It may be more than a month past April Fools' Day, but there's never a bad time for a little humor and laughter around the office.  After reading [this post](http://kovaya.com/miscellany/2007/10/insert-coin.html), I decided to see if this innocent little trick with HP LaserJets actually works.  Sure enough, it does!  Using this Perl script, you can make the LCD screen on your HP LaserJet say whatever you want.

And to top it all off, I successfully [ported the app to PHP](static/entries/a-little-office-fun-with-hp-laserjet-printers/hpsetdisp.php) and put a nice little front end on it for your added enjoyment.

<img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/a-little-office-fun-with-hp-laserjet-printers/insert-coin-printer.png">

Have fun, and remember, always understand what you're doing.  If your IT department frowns upon stuff like this (what do they not frown upon, right?) then you should probably think twice before trying this at the office.

```php
<?php

define("PORT","9100");

function showUI($ip="",$msg="INSERT COIN",$status=""){
?>
<html>
 <head><title>Set HP Printer Display Message</title></head>
 <body>
  <?
  if(!empty($status)){
    ?><h2><?= $status; ?></h2><?
  }
  ?>
  <form method="post" action="<?= $_SERVER['PHP_SELF']; ?>">
   <strong>Printer IP Address:</strong><br/>
   <input type="text" size="20" name="ip" value="<?= $ip; ?>">
   <br/><br/>
   <strong>Display Message:</strong><br/>
   <input type="text" size="50" name="msg" value="<?= $msg; ?>">
   <br/><br/>
   <input type="submit" value="Set Message!">
  </form>
 </body>
</html>
<?
}

if( !empty($_POST['ip']) && !empty($_POST['msg']) ) {

  $ip = $_POST['ip'];
  $msg = $_POST['msg'];
  $msg = addslashes($msg);

  if (($longip = ip2long($ip)) !== false) {
    if ($ip != long2ip($longip)) {
       die("Invalid IP Entered!");
    }
  } else {
    die("Invalid IP Entered!");
  }

  $socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
  if ($socket === false) {
     die("socket_create() failed!");
  }

  $connection = socket_connect($socket, $ip, PORT);
  if ($connection === false) {
     die("socket_connect() failed!");
  }

  $out = "@PJL RDYMSG DISPLAY=\"$msg\"\r\n";
  socket_write($socket, $out, strlen($out));
  socket_close($socket);

  showUI($_POST['ip'], $_POST['msg'], "Message set successfully!");

} else {
  showUI();
}

?>
```

You can [download the PHP here](static/entries/a-little-office-fun-with-hp-laserjet-printers/hpsetdisp.php) too.

Tested and works on the following printers:

* HP Color LaserJet 5550hdn
* HP Color LaserJet 4700
* HP LaserJet 4350
* HP LaserJet 9000mfp

Power cycle the printer to return the message to the "default".

Be safe you crazy kids.