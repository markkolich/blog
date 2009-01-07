Ever have a need to convert a PostScript file to a JPG image?

Probably not, but just in case here's how &mdash; it's easy using [GhostScript](http://www.ghostscript.com/):

```
gs -dBATCH -dNOPAUSE -sDEVICE=jpeg -sOutputFile=output.jpg -r100 input.ps
```

Use the `-r` argument to tweak the resolution/quality of the JPG.