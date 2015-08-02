Luckily, the answer to this one is an emphatic NO.

I was recently working on some code that dealt with MySQL table locking.  My app `WRITE LOCK`'ed a table while processing an `UPDATE`, `INSERT`, or `DELETE` because of an interesting multi-user concurrency problem.  BTW, if you are unfamiliar with a `WRITE LOCK`, this means all other MySQL connections to the same DB table will block until your transaction has finished.  In other words, all other transactions will not be able to access the locked table until your transaction sends an `UNLOCK TABLES`.  Or, as I described in this post, until your PHP script dies unexpectedly and the transaction is implicitly closed.

While experimenting, I decided to write a sample PHP script that locked a table, then died with an uncaught exception.  My concern was simple: if the script grabbed a lock on a table, then died, is the lock going to remain in place or will MySQL take care of this on its own?  I was concerned about this, because if a script grabbed a lock on a core table, then died unexpectedly, my entire application would lock up for all other users.

```php
<?php

// Connecting, selecting database
$link = mysql_connect('127.0.0.1', 'mark', 'mysql') or
  die('Could not connect: ' . mysql_error());
mysql_select_db('sample_db') or die('Could not select database');

// Lock 'er down
$query = 'LOCK TABLES sample WRITE';
$result = mysql_query($query) or die('Query failed: ' . mysql_error());

// At this point, the "sample" table is locked.
// Do somethings here, wait a bit
sleep(30);

// Throw a new exception, uncaught.
throw new Exception("Something randomly bad happened!");

// Will not get here, but we should unlock.
$query = 'UNLOCK TABLES';
$result = mysql_query($query) or die('Query failed: ' . mysql_error());

echo "Done!";

?>
```

While the script was sleeping, I switched over to my nearest MySQL command prompt, and tried to lock the sample table from another transaction:

```sql
mysql>  LOCK TABLES sample WRITE;
... waiting
```

Because the PHP script had a `WRITE LOCK` on the table, this transaction had to wait.  Once the 30-second sleep was up and the script failed, I checked out my Apache error_log file.  Sure enough, an uncaught exception caused the script to die:

```
[Sat Aug 01 09:34:27 2009] [error] [client 1.0.0.102] PHP Fatal error:
  Uncaught exception 'Exception' with message 'Something randomly bad happened!'
  in /www/lock-tester.php:11\nStack trace:\n#0 {main}\n
  thrown in /www/lock-tester.php on line 11
```

Immediately after the script intentionally died, I went back to my trusty MySQL command prompt and sure enough, the `LOCK TABLES` finally succeeded:

```sql
mysql> LOCK TABLES sample WRITE;
... waiting
... eventually finished
Query OK, 0 rows affected (0.00 sec)
```

So, this leads me to conclude with much certainty, that if a PHP script grabs a lock on a table and then dies unexpectedly, MySQL automatically cleans up the transaction and releases all locks grabbed by the script.  Hence, the rest of my application will continue to run unaffected.

And much joy was had by all.

<!--- tags: mysql, php -->