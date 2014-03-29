This weekend I setup my own [SVN source control server](howto-setting-up-your-own-svn-server-using-apache-and-mod-dav-svn) running inside of a CentOS 5.4 virtual machine.  Once my new SVN server was setup and ready to roll, I tried checking out one of the repositories.  From the nearest command line, I started typing `svn co http://192.168.1.` ... damn, what was the IP-address of my local SVN server again?  I just picked an IP-address about 15 minutes ago during my CentOS install, but already forgot it!  Ok, time to setup a decent local DNS server for my home network.  Since 2006, I've been limping along manually editing my `/etc/hosts` files on multiple machines and memorizing the IP-addresses of critical devices on my network.  Looking back, this was just plain silly.

### My Network Topology

My network topology, shown in the diagram below, isn't too complicated. In fact, it's probably quite standard for an average developer &mdash; I've got a typical wireless router and firewall appliance connected to an HP Procurve 1800-8G Gigabit switch which fans out the bandwidth from there. Generally speaking, most traffic on my home network is local so everything sits behind the HP Procurve Gigabit switch. I'm more concerned about the internal network speed between devices, instead of device to the outside world. And of course, all of my devices sit behind NAT, which the router handles for me.

<img src="static/entries/howto-setting-up-your-own-local-dns-server/kolich.com-network-topology.png" width="400">

### The Problem

The DNS for my publicly visible domain names are hosted by my registrar, Network Solutions.  I'm not planning on hosting my own DNS for these services anytime soon, so I just needed something to help me out around the house.  In other words, I just need a local DNS server so I can resolve addresses internally on my network.  I was getting really tired of typing `ping 192.168.1.102` when I could have been typing `ping somebox` this whole time.  In addition to resolving locally, if the DNS server receives a request to resolve a name it doesn't know about, I want it to forward the request to my favorite DNS provider, OpenDNS.  So if my DNS server receives a request for `somebox`, and `somebox` is a local device on my network, it should resolve to `192.168.1.whatever`.  However, if it receives a request to resolve `google.com`, or some other domain it knows nothing about, it should forward the request to OpenDNS and cache the response if necessary.

I should also add that every device with a name on my local network, has a hostname that ends with `kolich.local` (this is my new DNS zone).  This is so I can distinguish internal devices from servers, and services, running externally under `kolich.com`, `koli.ch`, or another domain.

### Installing the DNS Server

I cloned another CentOS Linux virtual machine, booted it up, and installed [Bind](http://www.isc.org/software/bind) (an open source DNS server usually called `named`):

```
#(dns)/> yum -y install bind bind-chroot caching-nameserver
```

As of 3/15/10 CentOS 5.4 ships with Bind 9.3.6, which should be perfectly stable for what I need.  This new virtual machine is now my own private internal DNS server, not visible to the outside world.  Once installed, I used `chkconfig` to start named on system startup:

```
#(dns)/> /sbin/chkconfig --level 35 named on
```

You should note that named on CentOS, and probably other newer Linux distros, runs inside of a `chroot`'ed environment which I have no interest in explaining here.  If you want to read more about `chroot`, check out [this decent overview of it on Wikipedia](http://en.wikipedia.org/wiki/Chroot).  For the sake of this blog post, you probably don't have to care about how named runs under `chroot`.

### Configuring the DNS Server

Once installed, I tweaked my `/etc/named.conf` file so it looks something like this:

```
options {
  directory           "/var/named"; // the default
  dump-file           "data/cache_dump.db";
  statistics-file     "data/named_stats.txt";
  memstatistics-file  "data/named_mem_stats.txt";
  version             "currently unavailable";
  ;; Forward anything this DNS server can't resolve to OpenDNS
  forwarders { 208.67.222.222; 208.67.220.220; };
};

;; All devices under my local network sit behind
;; the kolich.local zone
zone "kolich.local" in {
  type master;
  file "kolich.local.ns";
  allow-update { none; };
};

;; For reverse lookups, going IP addy back to a hostname
zone "1.168.192.in-addr.arpa" in {
  type master;
  file "1.168.192.in-addr.arpa.ns";
  allow-update { none; };
};
```

I have two zone files: `kolich.local.ns` and `1.168.192.in-addr.arpa.ns`.

The first zone file maps a list of hostnames to IP-addresses, and the second defines a reverse lookup zone that maps a list IP-addresses back to hostnames.  Note that because named is running under a chroot'ed environment, it's usually safest to place these zone files under `/var/named/chroot/var/named` (yes, that is the correct path) on a typical CentOS/RHEL/Fedora installation.

In my `/etc/named.conf` file, notice I've given two forwarders in the options section.  This declaration lets me tell named to forward any requests it cannot resolve to OpenDNS at `208.67.222.222` or `208.67.220.220`.

Now let's take a quick look at my zone files.  First, here is my `/var/named/chroot/var/named/kolich.local.ns` zone file for my new `kolich.local` zone:

```
$TTL    1d
kolich.local.  IN    SOA   ns.kolich.local. support.kolich.com. (
    2010031500 ; se = serial number
    3h         ; ref = refresh
    15m        ; ret = update retry
    3w         ; ex = expiry
    3h         ; min = minimum
    )

    IN    NS    ns.kolich.local.

; private hosts
ns         IN    A    192.168.1.2

cat        IN    A    192.168.1.3
fish       IN    A    192.168.1.4
whale      IN    A    192.168.1.5
monkey     IN    A    192.168.1.6
horse      IN    A    192.168.1.7
cow        IN    A    192.168.1.8
```

And here's my `/var/named/chroot/var/named/1.168.192.in-addr.arpa.ns` reverse lookup zone file that is used to map IP-addresses back to hostnames:

```
$TTL    1d
@   IN    SOA   ns.kolich.local. support.kolich.com. (
    2010031500 ; se = serial number
    3h         ; ref = refresh
    15m        ; ret = update retry
    3w         ; ex = expiry
    3h         ; min = minimum
    )

    IN    NS    ns.kolich.local.

; private hosts, reverse lookup
2     IN    PTR    ns.kolich.local.

3     IN    PTR    cat.kolich.local.
4     IN    PTR    fish.kolich.local.
5     IN    PTR    whale.kolich.local.
6     IN    PTR    monkey.kolich.local.
7     IN    PTR    horse.kolich.local.
8     IN    PTR    cow.kolich.local.
```

Note that my `1.168.192.in-addr.arpa.ns` zone file is basically a reverse map of my `kolich.local.ns` file.  Of course, depending on your local network settings, your IP addresses might be different.  And no, the systems and devices on my network are not named after animals; the names shown here are just for the sake of this example.

### Starting the DNS Server

Ok, once all of your configuration files are in place, it's time to start your new local DNS server:

```
#(dns)/> /etc/init.d/named start
```

That's it!  Assuming named started correctly, I can configure the clients on my local network to point to my new in-house DNS server.  On Linux, this involves editing the domain and nameserver configuration inside of `/etc/resolv.conf`:

```
domain kolich.local
nameserver 192.168.1.2
```

Once `/etc/resolv.conf` is configured properly, I can use the `nslookup` or `dig` commands to verify that all is working as expected:

```
#(dns)/> nslookup monkey
Server:         192.168.1.2
Address:        192.168.1.2#53

Name:   monkey.kolich.local
Address: 192.168.1.6
```

Great!  Hostname resolution works well.  Now, let's verify that I can go the other way (reverse lookups):

```
#(dns)/> nslookup 192.168.1.3
Server:         192.168.1.2
Address:        192.168.1.2#53

2.1.168.192.in-addr.arpa    name = cat.kolich.local.
```

Yup, works fine.  So what about that forwarding stuff?  Good call, we should also check that the server is forwarding requests for hostnames it can't resolve to OpenDNS:

```
#(dns)/> nslookup twitter.com
Server:         192.168.1.2
Address:        192.168.1.2#53

Non-authoritative answer:
Name:   twitter.com
Address: 128.242.240.20
```

Perfect.  Internal hosts resolve correctly, and my DNS server is forwarding all requests it can't resolve to OpenDNS as desired!

Finally, with a proper DNS server up and running, I can get back to my project.

Enjoy.