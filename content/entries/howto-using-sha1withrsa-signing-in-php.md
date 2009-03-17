I've been banging my head against my cube for a while on this one, and finally gave into the fact that I couldn't get **SHA1withDSA signing in PHP** to work.  In Java, SHA1withDSA signing appears to be a no-brainer.  However, in PHP, it's a disaster and I don't fully understand why.  Bottom line, I finally decided to give up on DSA and started using SHA1withRSA to digitally sign a request in PHP.  I need to do this because I have a PHP web-app that is communicating with a REST interface which requires each request to be digitally signed.  The whole point of the digital signature is so that the REST API can validate that the request really is coming from the sender; in other words, the sender really is who they say they are.

Here's how I got SHA1withRSA digital signing working in PHP.

First, you need to create a public/private key pair.  So, use the `openssl` command to generate a new RSA key:

```
openssl genrsa -out key.pem 1024
```

Second, now that you have an RSA key you'll need to generate the public piece of the key which will be given to your partner (the person/interface you're sending the signed request to so they can validate your signature):

```
openssl rsa -in key.pem -pubout -outform DER -out pubkey.der
```

Note that my public key is DER encoded (`-outform DER`).  Normally, you might generate a PEM encoded key, but a DER encoded key is slightly easier to handle in Java.  A PEM encoded key is actually a DER encoded key in base64 format with a header.  In Java, if you give someone a PEM key, they have to parse out the header and then base64 un-encode the key to get the bytes which can be a pain.

Finally, use PHP to generate a signature that will be used to digitally sign the request.  In this example below, the data I need to sign is stored in the variable `$toSign`.  Typically, `$toSign` might contain a URL of the request, and some type of API key:

```php
<?php

$signature = null;
$toSign = "http://example.com/resources/bogus";

// Read the private key from the file.
$fp = fopen("key.pem", "r");
$priv_key = fread($fp, 8192);
fclose($fp);
$pkeyid = openssl_get_privatekey($priv_key);

// Compute the signature using OPENSSL_ALGO_SHA1
// by default.
openssl_sign($toSign, $signature, $pkeyid);

// Free the key.
openssl_free_key($pkeyid);

// At this point, you've got $signature which
// contains the digital signature as a series of bytes.
// If you need to include the signature on a URL
// for a request to be sent to a REST API, use
// PHP's bin2hex() function.

$hex = bin2hex( $signature );
$toSign .= "/" . $hex;

echo $toSign;

?>
```

This was tested on RHEL4 U7, with PHP version 4.3.9.  I'm sure it will work on PHP 5+, but I haven't tried it.

Good luck.