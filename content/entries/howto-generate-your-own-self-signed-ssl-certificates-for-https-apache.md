If you'd like to generate your own self-signed SSL certificates for use with Apache, the `openssl` command makes it easy.

At home, I run a few HTTPS dev Apache instances that use my own self-signed SSL certificates.  Granted these certificates are not signed by a legitimate Certificate Authority (like Verisign, Thawte, or Network Solutions), but they get the job done if you want quick and cheap SSL security.  Keep in mind that if you use a self-signed certificate, a web-browser will complain.  You shouldn't use these instructions to setup SSL in a real production environment, however, for development stuff at home, this is perfect.

Generate your own self-signed SSL certificates using the `openssl` command:

```
openssl genrsa 4096 > example.com.key
openssl req -new -key example.com.key -x509 -days 365 -out example.com.crt
```

The first command will generate a new private key with a specified size of 4096-bits.

The second command will produce a certificate worthy of inclusion into Apache.

Now that you've got a key (a `.key` file) and certificate (a `.crt` file), you can integrate them into Apache.  This involves using the [SSLCertificateFile](http://httpd.apache.org/docs/2.2/mod/mod_ssl.html#sslcertificatefile) and [SSLCertificateKeyFile](http://httpd.apache.org/docs/2.2/mod/mod_ssl.html#sslcertificatekeyfile) directives in your Apache configuration file that defines an HTTPS VirtualHost.  You need to configure these directives to point to your certificate and key files, respectively.  In my environment, this configuration goes into `/etc/httpd/conf.d/ssl.conf`:

```
##
## SSL Virtual Host Context
##

<VirtualHost _default_:443>
 ...
 SSLCertificateFile /path/to/crt/file/example.com.crt
 SSLCertificateKeyFile /path/to/key/file/example.com.key
 ...
</VirtualHost>
```

Remember, your private key (your `.key` file) is important.  You should keep it in a secure/private place on your server, and certainly not in a public readable directory.