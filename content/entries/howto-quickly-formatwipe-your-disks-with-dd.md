Quick tip: If you're ever in a situation that requires a simple and dirty wipe/format/erase of a device (USB key, hard disk, whatever), you might find the following HOWTO somewhat useful.  This post assumes you are familiar with Linux.

**Note that these instructions tell you how to erase a disk for simple "keep prying eyes away from your data" purposes.  If the device you're erasing contains sensitive data of any kind, and you care about data security, then you should consider "shredding" your device using a tool like [DBan &ndash; Darik's Boot And Nuke](http://www.dban.org/).**

### Attach and Locate the Device You want to "Erase"

For a hard disk, you'll probably use `/dev/sda`.  You'll need to locate the correct device special file for your device; these vary from system to system.  Make sure you pick the right one.

### Erase with All Zeros, or a Random Bit Pattern

Once you've located the DSF for your device, you can use dd to erase it by writing out a series of continuous zeros, or a random bit pattern.  For the sake of this example, I'll assume the device you want to erase is `/dev/sda`.

Erase the device with all zeros:

```
#/> dd if=/dev/zero of=/dev/hda bs=1024k
```

Or, erase the device with a random bit pattern using `/dev/urandom`:

```
#/> dd if=/dev/urandom of=/dev/hda bs=1024k
```

Side note, you can also [generate decent passwords using /dev/random](howto-generating-good-random-passwords-with-devurandom).