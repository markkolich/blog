Several days ago I spent more than **5 hours** setting up a fresh install of Windows Vista Enterprise on a desktop computer in my home office.  This setup process involved tweaking the system configuration, installing and configuring all of my required software for development, checking out a massive SVN code repository, etc.  Sadly, less than a week after I finished setting up this box, the SATA hard disk died.  As it turns out, the disk flat out overheated due to poor airflow around the disk chassis in my PC (that's a story/post/opinion piece for another day).  In short, I lost everything on the drive.  All of the blood, sweat and tears; for nothing.

I decided to never let this happen again (since it's a huge time suck sitting around waiting for software to install) and began investigating free, yet solid and reliable backup solutions suitable for a home office.  This post is an attempt to document what whole disk backup and recovery solution worked for me, using several freely available open-source tools.

It's a known fact that more than half of all backups fail on recovery.  As a result, I want something non-proprietary, works with any file system, is simple to use, and is pretty much guaranteed to work on all hardware.  I also want to be sure that once backed up, my data is compressed using a very common data format (e.g., gzip).  It would be a shame to use a proprietary tool that locks my data into some commercial proprietary format.

### Disclaimer

For the sake of this HOWTO, I'm going to assume you are familiar with "Linux on a CD" distributions like [SystemRescueCD](http://www.sysresccd.org/) or [Knoppix](http://www.knoppix.net/).  Unfortunately, if you aren't familiar with Linux, the command line, or how to use a Linux on a CD distro then this HOWTO is probably going to feel a bit over your head.  BTW, this HOWTO assumes the drive you want to backup is at `/dev/sda`.  Your block device DSF (device special file) might be different.

This HOWTO is provided to you "as is", without warranty of any kind, express or implied. I am not responsible for data loss or hardware damage that occurs as a result of using these instructions. Use at your own risk.

### Boot into Linux on a CD

Pop in your favorite Linux on a CD distro and boot your PC accordingly.  For the sake of this HOWTO I'm going to assume you're using SystemRescueCD.  However, any decent Linux on a CD distribution should have all of the tools you'll need.

### Figure Out Where to Place the Backup

Before you do anything further, you should figure out where you are going to place your backups.  Backups are usually quite big, so expect them to chew up a good 80-100 GB of storage in most cases.  The better compression you use when making the backup, the less storage space you'll need.

In my case, I decided to put the backup on a large RAID-1 (mirror) volume I have in my home datacenter.  The mirrored volume is on another host, so I need to mount it locally using `sshfs`:

```
rescuecd#/> mkdir /mirror
rescuecd#/> sshfs mark@backup-host:/raid/backups /mirror
```

Once my mirror is mounted, I can read and write data to `/mirror` which will directly pipe it to the box connected to my RAID-1 volume via `sshfs`.

### Determine the Appropriate Block Size

For a quicker backup, it can help to nail down the optimal block size of the disk device you are going to backup.  Assuming you are going to backup `/dev/sda`, here's how you can use the `fdisk` command to determine the best block size:

```
rescuecd#/> /sbin/fdisk -l /dev/sda | grep Units

Units = cylinders of 16065 * 512 = 8225280 bytes
```

Note the `fdisk` output says "cylinders of 16065 * 512".  This means that there are 512 bytes per block on the disk.  You can significantly improve the speed of the backup by increasing the block size by a multiple of 2 to 4.  In this case, an optimal block size might be 1k (512*2) or 2k (512*4).  BTW, getting greedy and using a block size of 5k (512*10) or something excessive won't help; eventually the system will bottleneck at the device itself and you won't be able to squeeze out any additional performance from the backup process.

### Backup the Partition Layout

Before you do anything, it's always a good idea to backup the partition layout.  When you create a whole disk backup, you don't have to worry about partitions.  However, it can be handy to have this partition information (in case you need to mount a specific partition in the backup as a file using the exact offset).  Use the `sfdisk` command to backup the partition layout:

```
rescuecd#/> sfdisk -d /dev/sda > /mirror/backup-sda.sf
```

Once you've backed up the partition layout, you can `cat /mirror/backup-sda.sf` to verify that you've correctly saved the partition mapping.

### Backup the Master Boot Record (MBR)

Again, you don't need to explicitly do this since a whole disk backup includes the MBR, but it's a good idea to snag the master boot record just in case.  To backup the MBR, you can use the `dd` command:

```
rescuecd#/> dd if=/dev/sda of=/mirror/backup-sda.mbr count=1 bs=512
```

If you want to prove to yourself that you've successfully saved the MBR, you can run `file /mirror/backup-sda.mbr` to confirm you got what you needed:

```
rescuecd#/> file /mirror/backup-sda.mbr
backup-sda.mbr: x86 boot sector; partition 2: ID=0x83, active, starthead 1, \
  startsector 63, 2104452 sectors; partition 3: ID=0x82, starthead 0, \
  startsector 2104515, 4192965 sectors, code offset 0x48
```

Yep, the file command confirmed that we've successfully snagged the MBR of the disk.  The MBR always sits on the first 512-bytes of any bootable disk.

### Run the Backup

Now that you've saved everything you need from the disk, it's time to make the backup.  To create the backup, we'll use the `dd` command in conjunction with `gzip -9` for max compression.  For `dd`, we'll use an optimal block size of 1024 (as determined above).

Warning: this will take a long time so it's probably best to let this run overnight.  On my system at home, it took me 7+ hours to backup an entire 250 GB disk:

```
rescuecd#/> dd if=/dev/sda bs=1024 conv=noerror,sync | pv | gzip -c -9 > /mirror/backup-sda.gz
```

The `conv=noerror,sync` flag asks `dd` to keep going even if there are any read errors with the disk and to pad every input block with NULs to match your input block size.  Note that I'm using the pv command to monitor the speed and progress of data flowing between `dd` and `gzip`.  The `pv` command will tell me how much data I've processed, how long the backup has been running, and the approx speed of my backup; essentially it displays a progress bar on the console.

### Re-compress with P7ZIP (if desired)

Gzip offers pretty decent compression, but if you want insanely awesome compression, you can use [P7ZIP](http://p7zip.sourceforge.net/) to compress your backups.  After the `dd` to `gzip` backup is complete, you can re-compress `backup-sda.gz` using P7ZIP if you'd like to save a little storage space.  If so, here's how:

```
rescuecd#/> gunzip -c /mirror/backup-sda.gz | 7za a /mirror/backup-sda.7z -si
```

Again, be warned, this process will seem like it takes forever.  However, using P7ZIP over Gzip, saved me about 5 GB on the compressed backup.  Using `gzip -9` alone, I compressed a 250 GB backup image down to about 31 GB.  With P7ZIP, the same backup was only 26 GB. P7ZIP is interesting because it sacrifices CPU cycles for compression, using a more exhaustive and complete compression algorithm. If you want more information on P7ZIP, check out [Wikipedia's article on the 7z compression format](http://en.wikipedia.org/wiki/7z).

### Restore from Backup

Backups are useless unless you can actually restore your data.  If you need to restore a P7ZIP compressed backup to `/dev/sda`, here's how:

```
rescuecd#/> 7za x /mirror/backup-sda.7z -so | dd of=/dev/sda bs=1024
```

If you decided to skip P7ZIP compression, and need to restore a Gzip compressed backup to `/dev/sda`, here's how:

```
rescuecd#/> gunzip -c /mirror/backup-sda.gz | pv | dd of=/dev/sda bs=1024
```

### Compression Tip

For best performance, I strongly recommend zeroing out your disk before installing any OS'es on the drive you plan to backup.  For example, I recently upgraded my Vista Enterprise box to Windows 7 Professional.  I backed up Vista using the instructions in this post, then used the `dd` command to zero out the disk before installing Windows 7:

```
rescuecd#/> dd if=/dev/zero of=/dev/sda bs=1024 conv=noerror,sync
```

This writes zeroes to the entire disk, essentially eliminating any random or stray data that's lingering at the end of the drive from a previous OS install.  Then, once I install Windows 7 and back it up, the backup process will compress Windows 7, my data, and all installed applications.  Eventually, it will hit the "rest of the disk", which is all zeroes.  As a result, the backup run time is reduced not to mention that the backup itself could be a fraction of the normal size (most compression algorithms LOVE large streams of similar patterns; they're built for that, so if you give `gzip`, `p7zip`, or `bzip2` some data then a huge stream of all zeros, expect some insanely good compression).

Using this technique, on one of my desktops, I compressed a **250GB disk with Windows 7 Professional installed down to only 12GB**.

So, if you plan on re-installing your OS, then making a backup, you should always use `/dev/zero` to zero out your disk before doing anything.

Good luck!

<!--- tags: linux, security, backup -->