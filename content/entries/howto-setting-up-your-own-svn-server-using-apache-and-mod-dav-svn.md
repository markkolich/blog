Setting up your own SVN source control server is surprisingly easy.  At home, I recently setup an SVN server in a CentOS 5.4 virtual machine with Apache 2.2 and [mod_dav_svn](http://summersoft.fay.ar.us/pub/subversion/Book/re58.html).  With a little work, I had a secure and fully functional SVN server up and running in about 20 minutes.

Note that this HOWTO is specific to CentOS/RHEL/Fedora.  The location of configuration files, and other tools, might be different depending on your Linux distro.  For the most part though, everything should be pretty similar and you should be able to figure it out.

### Install Apache, Subversion, mod_dav_svn and mod_ssl

On CentOS, installing the Apache web-server, Subversion, and the Apache `mod_dav_svn` and `mod_ssl` modules are a snap with yum:

```
#(root)/> yum -y install httpd subversion mod_dav_svn mod_ssl openssl
```

If you're on Ubuntu you can probably install the required packages using `apt-get install`.  Note that you need to install `mod_ssl` if you plan on securing your SVN server with HTTPS.  If you don't care about HTTPS, then you can ignore `mod_ssl` and skip to "Configure `mod_ssl` and Setup HTTPS" below.

### Create your SVN Root Directory Structure

On my SVN server, I created a new SVN root at `/svn`. From here on out, all of my SVN repositories will live under `/svn/repos`:

```
#(root)/> mkdir -p /svn/repos
#(root)/> chown -R apache:apache /svn
```

Once done, you're ready to create your first SVN repository.

### Create your First Repository

Using the `svnadmin` command, create a repository under `/svn/repos`. For the sake of this example, the repository I'm creating is named `myproject`. Of course, you can name your own repository whatever you'd like.  Oh, and you can create as many repositories as you'd like under `/svn`.

```
#(root)/> cd /svn/repos
#(root)/svn/repos> svnadmin create --fs-type fsfs myproject
#(root)/svn/repos> chown -R apache:apache myproject
#(root)/svn/repos> chmod -R g+w myproject
#(root)/svn/repos> chmod g+s myproject/db
```

You'll notice that the `svnadmin` command created a new directory named `myproject/`.  If you look inside `myproject/` you'll see a bunch of SVN repository data and configuration files.

```
#(root)/svn/repos> ll myproject
total 28
drwxrwxr-x 2 apache apache 4096 Mar 13 11:49 conf
drwxrwxr-x 2 apache apache 4096 Mar 13 12:01 dav
drwxrwsr-x 5 apache apache 4096 Mar 13 12:23 db
-r--rw-r-- 1 apache apache 2    Mar 13 11:49 format
drwxrwxr-x 2 apache apache 4096 Mar 13 11:49 hooks
drwxrwxr-x 2 apache apache 4096 Mar 13 11:49 locks
-rw-rw-r-- 1 apache apache 229  Mar 13 11:49 README.txt
```

Great, looks like our new SVN repository was setup correctly!

### Configure mod_dav_svn

Now that you have an SVN repository setup and ready to go, let's configure Apache and the `mod_dav_svn` module.  Open `/etc/httpd/conf.d/subversion.conf` in your favorite text editor, and tweak the configuration to match your installation.  My `subversion.conf` file looks like this:

```
LoadModule dav_svn_module     modules/mod_dav_svn.so
LoadModule authz_svn_module   modules/mod_authz_svn.so

<Location /svn>

   DAV svn
   SVNParentPath /svn/repos

   # Require SSL connection for password protection.
   SSLRequireSSL

   AuthType Basic
   AuthName "Marks SVN Server"
   AuthUserFile /svn/repos/users
   Require valid-user

</Location>
```

First, note that when you install `mod_dav_svn` using yum, the installation process will create a standard cookie cutter template `/etc/httpd/conf.d/subversion.conf` for you.  This template has a `LimitExcept` directive in it, and a few other things.  For security, I think it's best to require a user to authenticate before they are able to issue any request.  Hence, why I removed the `LimitExcept` directive and did my own thing.  If you want your SVN server to be read-only for anonymous users, and read-write for authenticated users, then my `subversion.conf` file is **not** for you.  My `subversion.conf` file shown above allows no anonymous access; all users must authenticate (enter a valid username and password) before they can do anything with the SVN server.

Second, note that I have enabled the `SSLRequireSSL` directive.  This triggers `mod_dav_svn` to reject all non-HTTPS requests.  This ensures that any communication between the server and my SVN client will be sent via HTTPS; usernames, passwords, and source code will be reasonably secured.  I'll show you how to setup HTTPS here in a moment.  Note that if you don't want to enable HTTPS on your SVN server, then you can comment out or remove the `SSLRequireSSL` line in your `subversion.conf` configuration file.

Finally, note that my `AuthUserFile` is `/svn/repos/users`.  This is a standard Apache `htpasswd` file that we'll create in the next step.

### Create your SVN Users File

Create your SVN users file using the `htpasswd` command.  This is the file that stores a list of usernames and passwords declaring who is allowed to access your SVN server.

```
#(root)/> htpasswd -c /svn/repos/users mark
```

Replace "mark" above with your desired username.  Repeat this command for however many users you need to add access.

### Configure mod_ssl and Setup HTTPS

If you have decided to make your SVN sever an HTTPS only server, we'll need to setup Apache's HTTPS configuration.  This involves tweaking `/etc/httpd/conf.d/ssl.conf` and creating a new self-signed SSL certificate.  For your convenience, I've included the same set of instructions below.  Note that my SVN server is named `svn.kolich.local` &mdash; yours will obviously be different.  Whatever it is, make sure that you enter the correct server name when openssl prompts you for a "Common Name" in your certificate.  The "Common Name" in your SSL certificate should match the fully qualified name of your SVN server.

Note that if you have an SSL certificate signed by a legitimate Certificate Authority (Network Solutions, Verisign, Thawte) you shouldn't need to generate a new SSL key and self-signed certificate.  You can simply use the one issued to you by your CA.

First, create a new SSL key with the `openssl` command:

```
#(root)/> mkdir /etc/httpd/ssl
#(root)/> cd /etc/httpd/ssl
#(root)/etc/httpd/ssl> openssl genrsa 4096 > svn.kolich.local.key
```

Now that you have a private key, create a self-signed certificate:

```
#/etc/httpd/ssl> openssl req -new -key svn.kolich.local.key -x509 \
    -days 1095 -out svn.kolich.local.crt

You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [GB]:US
State or Province Name (full name) [Berkshire]:California
Locality Name (eg, city) [Newbury]:My Town
Organization Name (eg, company) [My Company Ltd]:Mark Kolich
Organizational Unit Name (eg, section) []:
Common Name (eg, your name or your server's hostname) []:svn.kolich.local
Email Address []:
```

Finally, edit `/etc/httpd/conf.d/ssl.conf` to point to your newly generated SSL key and SSL certificate.  This involves updating the `SSLCertificateFile` and `SSLCertificateKeyFile` directives accordingly:

```
##
## SSL Virtual Host Context
##

<VirtualHost _default_:443>
 ...
 SSLCertificateFile /etc/httpd/ssl/svn.kolich.local.crt
 SSLCertificateKeyFile /etc/httpd/ssl/svn.kolich.local.key
 ...
</VirtualHost>
```

Note that you should not place your SSL private key and certificate in a location accessible by the web-server.  Usually placing them under `/etc/httpd` is sufficient.  It would be less desirable and quite insecure to place them under `/var/www/html` for example.

### Configure HTTP to HTTPS Redirection

If you've bothered to setup HTTPS in the previous step, you probably want Apache to gracefully redirect clients from HTTP to HTTPS.  If you don't automatically redirect, and you have `SSLRequireSSL` enabled in your `subversion.conf` file, when clients try to communicate with your SVN server via HTTP they'll see a `403 Forbidden` error.  Instead, let's `301 Moved Permanently` redirect them to HTTPS.  Open `/etc/httpd/conf/httpd.conf` in your favorite text editor, jump to the bottom of the file, and edit your `VirtualHost` configuration.  Mine is as follows:

```
NameVirtualHost *:80

<VirtualHost *:80>

  ServerAdmin example@example.com
  DocumentRoot /var/www/html
  ServerName svn.kolich.local
  ServerAlias svn

  RewriteEngine On
  RewriteCond %{HTTPS} !=on
  RewriteRule ^/(.*)$ https://svn.kolich.local/$1 [R=301,L]

  ErrorLog logs/svn.kolich.local-error_log
  CustomLog logs/svn.kolich.local-access_log common

</VirtualHost>
```

Save it, and you're done.  Now when a client tries to communicate with my SVN server via HTTP, it'll see a `301 Moved Permanently` redirect to HTTPS.  If my SVN client is smart enough, it will gracefully follow this redirect to HTTPS, and all is well.  Of course, you'll need to change the HTTPS URL shown above in the [RewriteRule directive](http://httpd.apache.org/docs/2.2/mod/mod_rewrite.html#rewriterule) to match your server hostname (your SVN server is not `svn.kolich.local`).

### Start Apache, and Enjoy

That's it!  Start Apache and checkout your new repository.

```
#(root)/> /etc/init.d/httpd start
```

On another machine, try to checkout the repository:

```
#(mark)~> svn co http://svn.kolich.local/svn/myproject
svn: PROPFIND request failed on '/svn/myproject'
svn: PROPFIND of '/svn/myproject': 301 Moved Permanently (http://svn.kolich.local)
```

Yep, HTTP to HTTPS redirection is working as expected.  Unfortunately my SVN client isn't smart enough to follow the redirect on its own.  Oh well, change that repository URL to HTTPS, and try again:

```
#(mark)~> svn co https://svn.kolich.local/svn/myproject
Error validating server certificate for 'https://svn.kolich.local:443':
 - The certificate is not issued by a trusted authority. Use the
   fingerprint to validate the certificate manually!
Certificate information:
 - Hostname: svn.kolich.local
 - Valid: from Mar 15 20:17:38 2010 GMT until Mar 14 20:17:38 2013 GMT
 - Issuer: Mark Kolich, California, US
 - Fingerprint: ff:ee:b6:9c:d8:d7:78:3b:ce:9e:09:dd:4a:99:93:11:3e:12:07:85
(R)eject, accept (t)emporarily or accept (p)ermanently? p
Authentication realm: <https://svn.kolich.local:443> Marks SVN Server
Password for 'mark': ...
A    myproject
Checked out revision 0.
```

It worked!  Note that the "Error validating server certificate" warning is because I'm using a self-signed SSL certificate.  When SVN asks if you want to accept the certificate, if you permanently accept it you will not be prompted about this again.  If you use an SSL certificate issued by a real Certificate Authority like Network Solutions, Verisign, or Thawte, you shouldn't see this warning.

Time to start hacking &mdash; cheers!

<!--- tags: apache, svn, security -->