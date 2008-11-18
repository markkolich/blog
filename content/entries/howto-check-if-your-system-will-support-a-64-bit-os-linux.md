So you've got a really sweet system, but you want to know if it will run a 64-bit OS.  Like 64-bit Linux, of course.

Easiest way I've found to tell if your system supports a 64-bit OS is to check the output of `/proc/cpuinfo`.  Specifically, check `flags` for `lm` (Long Mode):

```
#/> cat /proc/cpuinfo | grep -i -e processor -e flags
processor       : 0
flags           : fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 \
   clflush dts acpi mmx fxsr sse sse2 ss ht tm syscall nx lm pni monitor ds_cpl cid cx16 xtpr
processor       : 1
flags           : fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 \
   clflush dts acpi mmx fxsr sse sse2 ss ht tm syscall nx lm pni monitor ds_cpl cid cx16 xtpr
```

Notice the `lm` in the flags on each of the CPU's.  If you see `lm` then your system will support an `x86_64` kernel.  If you don't see `lm`, then you're obviously stuck in 32-bit land.

In the Linux kernel, this is defined in `include/asm-x86_64/cpufeature.h`:

```
#/> cat linux-2.6.22/include/asm-x86_64/cpufeature.h | grep "Long Mode"
#define X86_FEATURE_LM          (1*32+29) /* Long Mode (x86-64) */
```

Cheers.