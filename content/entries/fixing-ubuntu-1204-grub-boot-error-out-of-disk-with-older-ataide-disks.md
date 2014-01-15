I've been running Ubuntu 12.04 successfully on a tiny travel notebook (an [HP tc4200 Tablet PC](http://h18000.www1.hp.com/products/quickspecs/12138_na/12138_na.HTML)) for several months now.  To my surprise, things work quite well out of the box on this notebook, no major hangups or other issues.  Even the stylus based touchscreen works!

One small, yet very annoying, "feature" of the Grub version shipped with this version of Ubuntu is its strong affinity to newer SATA disks.  My HP tc4200 uses a much older ATA/IDE storage controller, so I'm stuck with an old IDE disk ...

```
root@tc4200:/# lspci -v | grep IDE
00:1f.1 IDE interface: Intel Corporation 82801FB/FBM/FR/FW/FRW
  (ICH6 Family) IDE Controller (rev 03) (prog-if 8a [Master SecP PriP])
```

This is fine, except that newer versions of Grub &mdash; like the one shipped with Ubuntu 12.04 -- seem to struggle with older (non SATA) boot disks.  In fact, it seems that non-SATA disks are almost entirely ignored by default.  On my HP tc4200, Grub was failing to boot Ubuntu with the following error ...

```
error: out of disk
```

I discovered that this is because Grub was not configured to pre-load the "ata" module on boot, effectively making it impossible for Grub to boot anything from a vanilla ATA/IDE disk.

To work around this problem, you need to add this line to the top of your **/etc/default/grub** file:

```
GRUB_PRELOAD_MODULES="ata"
```

And, run **update-grub** to re-generate your boot configuration with the correct pre-load module setting:

```
#(root)/> update-grub
```

Reboot, and enjoy.

Oh, and by the way, updating **/etc/default/grub** will naturally persist your desired configuration across kernel updates -- when a new kernel is installed, your Grub will be updated with the right pre-load ATA module setting.  In other words, you don't have to continuously remember to update /etc/default/grub every time a new kernel is installed.