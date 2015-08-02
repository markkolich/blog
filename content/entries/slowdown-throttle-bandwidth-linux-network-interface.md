In complex service oriented application stacks, some bugs only manifest themselves on congested or slow networking interfaces.  Consider a web-service running on a generic Linux box with a single networking interface, `eth0`.  If `eth0` is busy enough to completely saturate its networking link, a web-service running on the host behind that link may experience odd behavior when things "slowdown".

For instance, established client connections timeout but the service fails to gracefully cleanup after itself leaving these connections open &mdash; this is a classic source of connection leaks, which on the JVM usually results in the dreaded `IOException: Too many open files` problem.

So, in development, if one wants to see how a service behaves behind a slow networking interface with extra latency:

* Download large files in a loop to artificially saturate your networking link
* Or, more appropriately, figure out how to shape networking traffic on an interface of your choice 

A quick search for "how to artificially slow down a Linux networking interface" produced a number of interesting results.  Folks mostly discussed 3rd party tools like [Wondershaper](http://lartc.org/wondershaper/) and [Dummynet](http://info.iet.unipi.it/~luigi/dummynet/).  Other suggestions involved proxying all HTTP/HTTPS traffic through Apache's `mod_bw` &mdash; yuck! 

Fortunately, most Linux distros ship with the `tc` command which is used to configure Traffic Control in the Linux kernel.

On my Ubuntu 12.04 box I've got a single gigabit networking interface, `eth0`.

Let's slow 'er down!

### Add latency, slowing ping times

Without throttling, ping times to another local node on my home network are less than `0.2ms` on average.

```
[mark@ubuntu]~$ ping regatta
PING regatta.kolich.local (1.0.0.2) 56(84) bytes of data.
64 bytes from regatta.kolich.local (1.0.0.2): icmp_req=1 ttl=64 time=0.118 ms
64 bytes from regatta.kolich.local (1.0.0.2): icmp_req=2 ttl=64 time=0.193 ms
64 bytes from regatta.kolich.local (1.0.0.2): icmp_req=3 ttl=64 time=0.181 ms
```

So, lets use `tc` to add `500ms` of latency to all network traffic.

```
[mark@ubuntu]~$ sudo tc qdisc add dev eth0 root netem delay 500ms
```

Now, trying `ping` again note `time=500 ms` as desired.

```
[mark@ubuntu]~$ ping regatta
PING regatta.kolich.local (1.0.0.2) 56(84) bytes of data.
64 bytes from regatta.kolich.local (1.0.0.2): icmp_req=1 ttl=64 time=500 ms
64 bytes from regatta.kolich.local (1.0.0.2): icmp_req=2 ttl=64 time=500 ms
64 bytes from regatta.kolich.local (1.0.0.2): icmp_req=3 ttl=64 time=500 ms
```

Using `tc` we've added a delay of `500ms` to all traffic.  This will slow short connections, but once a connection gets past the TCP [Slow-start window](http://en.wikipedia.org/wiki/Slow-start) we're back to full speed.  That is, the connection may start slow &mdash; as shaped by our `tc` delay tweak &mdash; but once things are started TCP will ramp up and eventually hit full speed again.  

### Throttling a sustained maximum rate

So, let's configure a sustained maximum rate using `tc`.  In other words, lets configure Linux to never allow `eth0` to use more than `1kbps` regardless of port or application.

```
[mark@ubuntu]~$ sudo tc qdisc add dev eth0 handle 1: root htb default 11
[mark@ubuntu]~$ sudo tc class add dev eth0 parent 1: classid 1:1 htb rate 1kbps
[mark@ubuntu]~$ sudo tc class add dev eth0 parent 1:1 classid 1:11 htb rate 1kbps
```

Looks good, now lets download a large `.iso` file using `wget` to prove to ourselves that our sustained maximum rate throttling is actually working. 

```
[mark@ubuntu]~$ wget http://mirrors.kernel.org/.../CentOS-6.5-x86_64-bin-DVD1.iso -O /dev/null
HTTP request sent, awaiting response... 200 OK
Length: 4467982336 (4.2G) [application/octet-stream]
Saving to: `/dev/null'
 13% [==>                                   ] 580,837,703     10.5K/s
```

Note the download isn't going to hover exactly at `1.0K/sec` &mdash; the actual download speed as reported by `wget` is an **average** over time.  In short, you'll see numbers closer to an even `1.0K/sec` the longer the transfer.  In this example, I didn't wait to download an entire `4.2GB` file, so the `10.5K/s` you see above is just `wget` averaging the transfer speed over the short time I left `wget` running.

### Clearing all `tc` rules

Now that we're done, simply delete all traffic control throttling rules to return to normal.

```
[mark@ubuntu]~$ sudo tc qdisc del dev eth0 root
```

Cheers!

<!--- tags: linux -->