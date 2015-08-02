Last week, I lost a disk in a 4TB software RAID5 array mounted in my home Linux server.  This host has been online for almost 4-years without any major interruptions so the clock was ticking &mdash; it was really only a matter of time until I would be forced to replace a disk.  Fortunately, replacing the disk in the array was a complete breeze.  No data was lost, and rebuilding the array with a new disk only took a short 160-minutes while the host remained online.  Hats off to the folks maintaining [Linux RAID](https://raid.wiki.kernel.org/index.php/Linux_Raid) &mdash; the entire disk replacement process end-to-end, was flawless.

Before I could replace the failing disk, the array was limping along in a `degraded` state.  This got me thinking: I already regularly backup the data I cannot live without to an external USB pocket drive and store it "offsite" &mdash; what if I could sync the most important stuff to the "cloud" too?

So, I sat down and wrote a quick `bash` script that recursively crawls a root directory of my choosing and uses `curl` to upload each discovered file to AWS S3.  Note that the structure of the backup on S3 will exactly match the file/directory structure on disk:

```bash
#!/bin/bash

S3_KEY="[YOUR AWS KEY HERE]"
S3_SECRET="[YOUR AWS SECRET HERE]"

BUCKET="[YOUR BUCKET NAME HERE]"

CONTENT_TYPE="application/octet-stream"

find $@ -type f -print0 | while IFS= read -r -d '' i; do
  FILE="$(perl -MURI::Escape -e 'print uri_escape($ARGV[0],"^A-Za-z0-9\-\._~\/");' "$i")"
  RESOURCE="/${BUCKET}/${FILE}"
  DATE_VALUE=`date -R`
  STRING_TO_SIGN="HEAD\n\n\n${DATE_VALUE}\n${RESOURCE}"
  SIGNATURE=`echo -en ${STRING_TO_SIGN} | openssl sha1 -hmac ${S3_SECRET} -binary | base64`
  EXISTS=`curl -s -I -w "%{http_code}" \
    -o /dev/null \
    -H "Host: ${BUCKET}.s3.amazonaws.com" \
    -H "Date: ${DATE_VALUE}" \
    -H "Authorization: AWS ${S3_KEY}:${SIGNATURE}" \
    https://${BUCKET}.s3.amazonaws.com/${FILE}`
  if [ $EXISTS -eq "200" ];
  then
    echo "File \"$i\" exists."
  else
    echo $i
    MD5=`openssl dgst -md5 -binary "$i" | base64`
    STRING_TO_SIGN="PUT\n${MD5}\n${CONTENT_TYPE}\n${DATE_VALUE}\n${RESOURCE}"
    SIGNATURE=`echo -en ${STRING_TO_SIGN} | openssl sha1 -hmac ${S3_SECRET} -binary | base64`
    curl -# -X PUT -T "${i}" \
      --limit-rate 300k \
      --connect-timeout 120 \
      -H "Host: ${BUCKET}.s3.amazonaws.com" \
      -H "Date: ${DATE_VALUE}" \
      -H "Content-Type: ${CONTENT_TYPE}" \
      -H "Content-MD5: ${MD5}" \
      -H "Authorization: AWS ${S3_KEY}:${SIGNATURE}" \
      https://${BUCKET}.s3.amazonaws.com/${FILE} > /dev/null
  fi
done
```

If you'd rather not copy+paste, download the script [here](https://raw.githubusercontent.com/markkolich/blog/release/content/static/entries/quick-bash-script-backup-to-aws-s3/s3-backup.sh).

A few notes:

* You should replace `S3_KEY`, `S3_SECRET`, and `BUCKET` in the script with your AWS key, AWS secret, and backup bucket name respectively.
* I'm using the `--limit-rate 300k` argument to limit the upload speed to 300 KB/sec.  Otherwise, I'd completely saturate my upload bandwidth at home.  You should, of course, adjust this limit to suit your needs depending on where you're uploading from.
* I'm using the `--connect-timeout 120` argument to work around spurious connection failures that might occur during a handshake with S3 while starting an upload.
* Documentation on the request signing mechanism used in the script can be found at http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html.

### Usage

Assuming you have a directory named `foobar` which contains a nested structure of the content you want to upload:

```
chmod +x s3-backup.sh

./s3-backup.sh foobar
```

Or maybe you only want to upload `foobar/baz/*`:

```
./s3-backup.sh foobar/baz
```

Happy uploading.

<!--- tags: aws, s3, curl, bash, backup -->