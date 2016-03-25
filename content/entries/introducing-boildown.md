I've been poking at a fun side project lately, exploring how to compress/uncompress arbitrary streams flowing between two sockets.  I ended up with something that's a little hacky, but surprisingly works quite well.

Introducing [Boildown](https://github.com/markkolich/boildown).

From a remote location (usually from work or on the road), I SSH home quite regularly and port-forward to several services behind NAT on my home network: SSH, remote desktop, web-cams, etc.  I was curious to see if I could write something general that compresses traffic flowing between two sockets in an attempt to improve the overall "remote experience".  That is, compress the bidirectional traffic flowing over-the-wire to see if I could make things "faster".

Orthogonally, I kinda wanted an excuse to play with [LZF]((https://github.com/ning/compress/wiki/LZFFormat)) and [Snappy](http://google.github.io/snappy/).

### How it works

[Boildown](https://github.com/markkolich/boildown) listens on a local port, compresses (or decompresses) incoming traffic, and forwards the result to its destination.  It's like SSH port-forwarding, but the bidirectional network traffic flowing through Boildown is automatically compressed or decompressed, depending on how it's configured.  In essence, Boildown provides a compressed "pipe" connecting two nodes on a network.

Boildown is entirely protocol agnostic &mdash; it knows nothing about the protocol of the data flowing through it, and works transparently with any protocol that can be expressed over TCP/IP.  The most common being HTTP (port 80), HTTPS (port 443), and SSH (port 22).  This was key for me, because I wanted to build something general &mdash; a tool that isn't protocol or application specific, and an app I could just stick between a sender and a receiver on a network and (ideally) see some sort of performance benefit with compression.

And so, Boildown v1 supports the following framed or "block" codecs:

* [ZLIB](https://www.ietf.org/rfc/rfc1950.txt)
* [LZF](https://github.com/ning/compress/wiki/LZFFormat) &mdash; provided by [ning/compress](https://github.com/ning/compress)
* [Google Snappy](http://google.github.io/snappy/) &mdash; provided by [xerial/snappy-java](https://github.com/xerial/snappy-java)

### Usage

There's two sides (or "modes") to Boildown:

* **Compressor** &mdash; listens on a local port, compresses outgoing traffic, and forwards the compressed data to another host.
* **Decompressor** &mdash; listens on a local port, decompresses incoming traffic, and forwards the original (uncompressed) result to its destination.

Assuming you'd want to SSH to `remote:22`, here's how you'd create a compressed pipe using Boildown for an SSH session between `localhost:10022` and `remote:22`:

```
+--------- [localhost] ---------+                               +----------- [remote] ------------+
| --compress 10022:remote:10022 | <---- (compressed pipe) ----> | --decompress 10022:localhost:22 |
+-------------------------------+                               +---------------------------------+
```

A Boildown compressor listens at `localhost:10022` and forwards compressed traffic to the decompressor listening at `remote:10022`.  Any bytes received by the decompressor at `remote:10022` are decompressed and forwarded to the SSH server daemon listening locally on `localhost:22`.  Of course, traffic flowing the other way, `remote:22` back to `localhost:10022`, is compressed and decompressed in the same way.

Hence, a bidirectional, compressed network pipe.

#### On `localhost`

Start a compressor on `localhost:10022`, forwarding compressed traffic to `remote:10022`:

```
java -jar boildown-0.1-SNAPSHOT-runnable.jar --compress 10022:remote:10022 --zlib
```

#### On `remote`

Start a decompressor on `remote:10022`, forwarding decompressed traffic to `localhost:22`:

```
java -jar boildown-0.1-SNAPSHOT-runnable.jar --decompress 10022:localhost:22 --zlib
```

#### Connect-the-dots

On `localhost`, start a new SSH session, funneling traffic through the Boildown managed compressed pipe:

```
ssh -p 10022 localhost
```

#### Compression codecs

Specify `--zlib`, `--snappy`, or `--lzf` on the command line to use any of the 3 supported compression codecs.

Note, both sides of the pipe need to be using the same codec (obviously).

#### Thread pool

The compressor and decompressor implementations run within threads.  The size of the internal thread pool used by Boildown can be controlled with the `--poolSize N` argument, where `N` is the maximum number of desired threads in the pool.

By default, if `--poolSize` is omitted, the internal thread pool is sized to match the number of available cores.

### On-the-wire

Seeing what's happening on-the-wire, over the Boildown compressed pipe, is quite easy with `nc` (netcat), `telnet` and `tcpdump`.

Spin up a compressor listening at `localhost:20000` that forwards compressed traffic to `localhost:30000`:

```
java -jar boildown-0.1-SNAPSHOT-runnable.jar --compress 20000:localhost:30000 --zlib &
```

Spin up a decompressor listening at `localhost:30000` that forwards uncompressed traffic back to `localhost:30001`:

```
java -jar boildown-0.1-SNAPSHOT-runnable.jar --decompress 30000:localhost:30001 --zlib &
```

In a separate terminal, spin up an instance of `tcpdump` that dumps traffic on port `30000`.  On Mac OS X:

```
sudo /usr/sbin/tcpdump -i lo0 -nnvvXXSs 1514 port 30000
```

In another terminal, launch `nc` to open up a socket and listen on port `30001` (where the decompressed/original bytes will be forwarded to):

```
nc -l 30001
```

And finally, in yet another terminal window, launch `telnet` and connect to `localhost:20000`:

```
telnet localhost 20000
```

#### Magic

Click to enlarge.

<a href="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/introducing-boildown/tcpdump-magic-boildown.png"><img src="https://raw.githubusercontent.com/markkolich/blog/master/content/static/entries/introducing-boildown/tcpdump-magic-boildown.png" width="100%"></a>

In the left panel, we're using `telnet` to connect to the Boildown compressor listening at `localhost:20000`.  Anything typed into this `telnet` session is routed through Boildown, compressed, and forwarded to `localhost:30000`.

The middle panel, we're running `nc` which is listening at `localhost:30001`.  This is the decompressed side.  Anything from the `telnet` session at `localhost:20000` is seen here, and consequently, anything we type into this session is forwarded (and compressed) back to `localhost:20000`.

In the right panel, notice the bidirectional compressed traffic captured by `tcpdump` flowing over `localhost:30000`.  The astute reader will notice the `Z?` header in the `tcpdump` output given we're running Boildown with `--zlib`.

### Next steps

* **Java NIO** &mdash; eventually I want to explore how to use Java's non-blocking I/O paradigm in lieu of threads to manage data flowing over-the-wire, similar to Jetty's NIO [org.eclipse.jetty.server.ServerConnector](https://github.com/eclipse/jetty.project/blob/master/jetty-server/src/main/java/org/eclipse/jetty/server/ServerConnector.java). 
* **Specify Multiple Compressors/Decompressors** &mdash; as of now you can only specify a single `--compress` or `--decompress` route on the command line, but I'd eventually like to rework the app to support an arbitrary number of routes similiar to SSH's `-L`.

### Open Source

Boildown is [free on GitHub](https://github.com/markkolich/boildown) and licensed under the popular [MIT License](https://github.com/markkolich/boildown/blob/master/LICENSE).

Issues and pull requests welcome.

<!--- tags: boildown, java, ssh -->