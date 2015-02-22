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