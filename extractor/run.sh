#!/bin/bash

## GIT_AUTHOR_DATE='Sun Dec 11 12:55:00 2010 -0800' GIT_COMMITTER_DATE='Sun Dec 11 12:55:00 2010 -0800' git commit

for i in `cat posts.txt`; do
  DATE=`curl -s -X GET $i | grep -i abbr | grep -i published | sed -n 's/.*title\=\"\(.*\)\".*/\1/p'`
  SECONDS=$(perl -MHTTP::Date -wle 'print str2time('\"$DATE\"')')
  GIT_COMMIT_DATE=$(date -r $SECONDS "+%a %b %d %T %Y %z")
  echo $i
  echo "GIT_AUTHOR_DATE='$GIT_COMMIT_DATE' GIT_COMMITTER_DATE='$GIT_COMMIT_DATE' git commit"
  echo "-----------------------------------------------------------------"
  ##echo "$GIT_COMMIT_DATE,$i"
done

