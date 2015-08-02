Here's the situation, I'm often on a network that does not allow outbound traffic on port 22.  Meaning, I cannot directly "SSH out" from that network to my Linux box at home. Fair enough. However, this network does allow outbound traffic on ports 80, 443, and 8443 via a web-proxy.  That said, if I want to "SSH out" from this network to my Linux box at home, I can do so with a little tweaking of my remote Apache server and my local SSH client.

Here's how ...

### Overview

First, you'll need to configure your Apache web-server to accept traffic on a port that's acceptable to the web-proxy.  In my case, I don't have anything running on port 8443, and the web-proxy allows traffic through port 8443, so that's perfect.  Apache will be configured to listen on 8443, and act as a "proxy" between an SSH client and an SSH server (usually the SSH server running on the box you're trying to connect to).

Second, on the client side, you'll be using something like [Proxytunnel](http://proxytunnel.sourceforge.net/) to punch a hole through the web-proxy allowing your SSH client to connect to an SSH server of your choice.

Putting it all together, the basic flow is ...

1. Your local SSH client uses Proxytunnel to connect to web-proxy.corp.example.com:3128
2. Web-proxy.corp.example.com connects to Apache running at yourwebserver:8443
3. Your Apache server, acting as yet another proxy, connects to yoursshserver:22
4. It works!

### Install and Configure Proxytunnel

If you're on Ubuntu, you can install Proxytunnel with the following command:

```
#/> sudo apt-get install proxytunnel
```

Once installed, edit your **~/.ssh/config** file to instruct your SSH client to use Proxytunnel when connecting to the destination host:

```
## ~/.ssh/config

Host kolich.com
  Hostname kolich.com
  ProtocolKeepAlives 30
  ProxyCommand /usr/bin/proxytunnel \
    -p web-proxy.corp.example.com:3128 \
    -r kolich.com:8443 -d %h:%p \
    -H "User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Win32)"
```

In this example, when my SSH client makes an attempt to connect to kolich.com:22, it will spawn Proxytunnel which will then route my connection through web-proxy.corp.example.com:3128 and on to kolich.com:8443.  This seems really convoluted, but it actually works quite well.

Note that I'm spoofing a somewhat real User-Agent to prevent suspicion from the system administrators running web-proxy.corp.example.com.  If you're a system administrator that runs such a web-proxy, please accept my apologies for making your life even more difficult.

### Configure mod_proxy on Apache

Now that you've got the client part figured out, you'll need to configure Apache's mod_proxy module to proxy traffic between yourwebserver:8443 and yoursshserver:22.  In all likelihood, your web-server and SSH server are the same box.  At least, in my home, they are.

Oh, and I assume you already have Apache and mod_proxy installed, and working.  There are ton of other tutorials and nice blog posts online about how to install and setup Apache if you don't already have it installed and functional.

In my Apache virtual host configuration, I've added another V-host listening on port 8443 that will only accept CONNECT requests bound for kolich.com on port 22:

```
## Load the required modules.
LoadModule proxy_http_module modules/mod_proxy_http.so
LoadModule proxy_connect_module modules/mod_proxy_connect.so

## Listen on port 8443 (in addition to other ports like 80 or 443)
Listen 8443

<VirtualHost *:8443>

  ServerName youwebserver:8443
  DocumentRoot /some/path/maybe/not/required
  ServerAdmin admin@example.com

  ## Only ever allow incoming HTTP CONNECT requests.
  ## Explicitly deny other request types like GET, POST, etc.
  ## This tells Apache to return a 403 Forbidden if this virtual
  ## host receives anything other than an HTTP CONNECT.
  RewriteEngine On
  RewriteCond %{REQUEST_METHOD} !^CONNECT [NC]
  RewriteRule ^/(.*)$ - [F,L]

  ## Setup proxying between youwebserver:8443 and yoursshserver:22

  ProxyRequests On
  ProxyBadHeader Ignore
  ProxyVia Full

  ## IMPORTANT: The AllowCONNECT directive specifies a list
  ## of port numbers to which the proxy CONNECT method may
  ## connect.  For security, only allow CONNECT requests
  ## bound for port 22.
  AllowCONNECT 22

  ## IMPORTANT: By default, deny everyone.  If you don't do this
  ## others will be able to connect to port 22 on any host.
  <Proxy *>
    Order deny,allow
    Deny from all
  </Proxy>

  ## Now, only allow CONNECT requests bound for kolich.com
  ## Should be replaced with yoursshserver.com or the hostname
  ## of whatever SSH server you're trying to connect to.  Note
  ## that ProxyMatch takes a regular expression, so you can do
  ## things like (kolich\.com|anothersshserver\.com) if you want
  ## to allow connections to multiple destinations.
  <ProxyMatch (kolich\.com)>
    Order allow,deny
    Allow from all
  </ProxyMatch>

  ## Logging, always a good idea.
  LogLevel warn
  ErrorLog logs/yourwebserver-proxy_error_log
  CustomLog logs/yourwebserver-proxy_request_log combined

</VirtualHost>
```

Once you get everything integrated, restart Apache and you should be golden.

### Under the Hood

To prove that everything works, let's try a few things.

First I'm going to telnet to web-proxy.corp.example.com:3128.  Then, I'm going to tell it to connect to kolich.com:8443.  Finally, I'm going to tell Apache on kolich.com:8443 to connect to kolich.com:22.  This is exactly the same flow used by Proxytunnel under the hood.

```
(mark@ubuntu)~> telnet web-proxy.corp.example.com 3128
Trying 10.10.10.10...
Connected to web-proxy.corp.example.com (10.10.10.10).
Escape character is '^]'.
CONNECT kolich.com:8443 HTTP/1.1
Host: kolich.com

HTTP/1.0 200 Connection Established

CONNECT kolich.com:22 HTTP/1.1
Host: kolich.com

HTTP/1.0 200 Connection Established
Proxy-agent: Apache

SSH-2.0-OpenSSH_4.3
```

Sweet!  Notice the raw "SSH-2.0-OpenSSH_4.3" response from the SSH server, indicating a successful connection.  Now, If I was a real SSH client, I'd continue the handshake and away we go.

So, from a real SSH client with Proxytunnel enabled ...

```
(mark@ubuntu)~> ssh mark@kolich.com
Via web-proxy.corp.example.com:3128 -> kolich.com:8443 -> kolich.com:22
mark@kolich.com's password:

Last login: Sat Dec 31 12:53:22 2011 from gateway.kolich.local
(mark@server)~>
```

It works!  Notice the intermediate "Via web-proxy.corp.example.com:3128 -> kolich.com:8443 -> kolich.com:22" output from Proxytunnel telling me what it's doing to connect.  And of course, look at that beautiful shell prompt.

SSH through a web-proxy, I love it.

Enjoy.

<!--- tags: ssh, apache, security -->