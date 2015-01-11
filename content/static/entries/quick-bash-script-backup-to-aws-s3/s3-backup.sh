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
