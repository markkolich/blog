Last week, I lost a disk in a 4TB software RAID5 array mounted in my home Linux server.  This host has been online for almost 4-years without any major interruptions so the clock was ticking &mdash; it was really only a matter of time until I would be forced to replace a disk.  Fortunately, replacing the disk in the array was a complete breeze.  No data was lost, and rebuilding the array with a new disk only took a short 160-minutes while the host remained online.  Hats off to the folks maintaining [Linux RAID](https://raid.wiki.kernel.org/index.php/Linux_Raid) &mdash; the entire disk replacement process end-to-end, was flawless.

Before I could replace the failing disk, the array was limping along in a `degraded` state.  This got me thinking: I already regularly backup the data I cannot live without to an external USB pocket drive and store it "offsite" &mdash; what if I could sync the most important stuff to the "cloud" too?

So, I sat down and wrote a quick `bash` script that recursively crawls a root directory of my choosing and uses `curl` to upload each discovered file to AWS S3.  Note that the structure of the backup on S3 will exactly match the file/directory structure on disk:

```bash
#!/bin/bash -e

S3_KEY="[YOUR AWS KEY HERE]"
S3_SECRET="[YOUR AWS SECRET HERE]"

S3_BUCKET="[YOUR AWS S3 BUCKET NAME]"

CONTENT_TYPE="application/octet-stream"

for i in `find $@ -type f`;
do

  FILE=$i

  ## See http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html
  RESOURCE="/${S3_BUCKET}/${FILE}"
  DATE_VALUE=`date -R`
  STRING_TO_SIGN="PUT\n\n${CONTENT_TYPE}\n${DATE_VALUE}\n${RESOURCE}"
  SIGNATURE=`echo -en ${STRING_TO_SIGN} | openssl sha1 -hmac ${S3_SECRET} -binary | base64`

  echo $FILE

  curl -# -X PUT -T "${FILE}" \
    --limit-rate 250k \
    --connect-timeout 120 \
    -H "Host: ${S3_BUCKET}.s3.amazonaws.com" \
    -H "Date: ${DATE_VALUE}" \
    -H "Content-Type: ${CONTENT_TYPE}" \
    -H "Authorization: AWS ${S3_KEY}:${SIGNATURE}" \
    https://${S3_BUCKET}.s3.amazonaws.com/${FILE} > /dev/null

done
```

If you'd rather not copy+paste, download the script [here](static/entries/quick-bash-script-backup-to-aws-s3/s3-backup.sh).

A few notes:

* You should replace `S3_KEY`, `S3_SECRET`, and `S3_BUCKET` in the script with your AWS key, AWS secret, and backup bucket name respectively.
* I'm using the `--limit-rate 250k` argument to limit the upload speed to 250 KB/sec.  Otherwise, I'd completely saturate my upload bandwidth at home.  You should, of course, adjust this limit to suit your needs depending on where you're uploading from (starbucks Wifi vs. home internet connection vs. corporate fiber).
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