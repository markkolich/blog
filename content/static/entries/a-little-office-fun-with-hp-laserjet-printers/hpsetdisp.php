<?php

// Copyright (c) 2009 Mark S. Kolich
// http://mark.kolich.com
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

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

if( !empty($_POST['ip']) &&
        !empty($_POST['msg']) ) {

  $ip = $_POST['ip'];
  $msg = $_POST['msg'];
  $msg = addslashes($msg);

  if (($longip = ip2long($ip)) !== false) {
    if ($ip != long2ip($longip)) {
       die("Invalid IP Entered!");
    }
  }
  else {
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

  showUI($_POST['ip'], $_POST['msg'],
          "Message set successfully!");

}
else {
  showUI();
}

?>
