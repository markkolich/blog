While working on [MySQL triggers](http://dev.mysql.com/doc/refman/5.0/en/create-trigger.html) this afternoon, I encountered a strange problem that dealt with using a trigger to tweak records in a table after an `UPDATE`.  Specifically, I wanted to write a trigger that would automatically loop over all records in a table, and then mark any "expired" if they were added more than X days ago.  I added a trigger using the `UPDATE` mechanism I thought would work, but nope, I kept getting this error:

```
ERROR 1442 (HY000): Can't update table 'pass' in stored
   function/trigger because it is already used by statement
   which invoked this stored function/trigger.
```

Looks intimidating.  According to the MySQL forums, a fairly [large number of folks have reported this same problem](http://forums.mysql.com/read.php?99,122354,122354).  Sadly, I wasn't able to find a good solution to the problem, and according to MySQL's documentation, what I'm trying to do is damn near impossible with triggers.

### What I'm Trying To Accomplish

I've got a table named "pass" that contains three fields: an ID field, a status, and a `TIMESTAMP`.  The ID field is simply a unique ID number of some sort.  The status field is an `ENUM`, either "active" or "expired".  And, the `TIMESTAMP` indicates when the record was added to the table.  The goal here is to use a trigger that automatically sets the status to "expired" on each row that was added more than 7 days ago.  I want to let the MySQL trigger engine take care of managing the expired status of these records for me, so that my web-app doesn't explicitly have to.

### The Schema

Here's the schema for my "pass" table:

```sql
CREATE TABLE pass (
  id BIGINT NOT NULL,
  status ENUM('active','expired') NOT NULL DEFAULT 'active',
  addedon TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ( id )
) TYPE=InnoDB;
```

Note that the default value on the status field is "active" and the default on the `TIMESTAMP` field is the `CURRENT_TIMESTAMP` (the current time on the MySQL server when the record was added to the table).  Let's add some records to this "pass" table:

```sql
mysql> insert into pass ( id ) values ( '1' );
Query OK, 1 row affected (0.00 sec)

mysql> insert into pass ( id ) values ( '2' );
Query OK, 1 row affected (0.00 sec)

mysql> insert into pass ( id ) values ( '3' );
Query OK, 1 row affected (0.00 sec)

mysql> insert into pass ( id ) values ( '4' );
Query OK, 1 row affected (0.00 sec)

mysql> update pass set addedon = '2009-06-30 22:06:03' where id = 3;
Query OK, 1 row affected (0.01 sec)
Rows matched: 1  Changed: 1  Warnings: 0

mysql> select * from pass;
+----+--------+---------------------+
| id | status | addedon             |
+----+--------+---------------------+
|  1 | active | 2009-07-08 22:20:18 |
|  2 | active | 2009-07-08 22:20:19 |
|  3 | active | 2009-06-30 22:06:03 |
|  4 | active | 2009-07-08 22:20:22 |
+----+--------+---------------------+
4 rows in set (0.00 sec)
```

OK, so I populated the pass table and tweaked the addedon `TIMESTAMP` of ID 3 so that it's more than 7 days old.  Using a trigger, I want to set the status of this record to "expired" on any `UPDATE` to the pass table.

### The Trigger Problem

The original trigger I wrote used `UPDATE pass SET status = 'expired'` like so:

```sql
DELIMITER |
  CREATE TRIGGER expire_trigger
    BEFORE UPDATE ON pass
    FOR EACH ROW BEGIN
      UPDATE pass SET pass.status = 'expired' WHERE
        (DATEDIFF(NOW(),pass.addedon) > 7);
    END;
|
DELIMITER ;
```

This looks like it should work, but unfortunately I kept hitting the following error when trying to `UPDATE` any record in the pass table within the trigger:

```
ERROR 1442 (HY000): Can't update table 'pass' in stored
   function/trigger because it is already used by statement
   which invoked this stored function/trigger.
```

In a nutshell the problem here is that I'm trying to use a trigger to `UPDATE` a table via the operation that invoked the trigger in the first place.  According to what I read in the MySQL documentation, this error is generated to prevent infinite recursion: an `UPDATE` occurs, the trigger is run and updates the table, this trigger `UPDATE` causes the trigger to run again, and again, and again.  Basically without this error, the trigger would trigger itself in an infinite loop.

### Trigger Limitations in MySQL

In the end, I concluded that I cannot easily tweak my trigger to accomplish what I'm trying to do.  For example, instead of using the `UPDATE` above, I also tried with procedural like statements:

```sql
DELIMITER |
  CREATE TRIGGER expire_trigger
    BEFORE UPDATE ON pass
    FOR EACH ROW BEGIN
      IF (DATEDIFF(NOW(),NEW.addedon) > 7) THEN
        SET NEW.status = 'expired';
      END IF;
    END;
|
DELIMITER ;
```

This works quite nicely, BUT only on the record that I'm modifying.  For example, if I'm updating a record that's more than 7-days old, then yes, the trigger will change the status to expired for me.  However, if I update another record in the table that's not more than 7-days old, the trigger will not run over ALL records.  Basically, it seems that the trigger can only be used to modify/change the record being updated, inserted, or deleted.  There does not appear to be a way to write a MySQL trigger that loops over all records when activated.

### What About Procedures?

Knowing that triggers won't let me explicitly loop over and adjust all records in a table on an `UPDATE`, I gave up and started looking into [procedures](http://dev.mysql.com/doc/refman/5.0/en/create-procedure.html).  A procedure is somewhat similar to a trigger, except that it doesn't run automatically during an `UPDATE`, `INSERT`, `DELETE`, etc.  Instead, the user has to explicitly call a procedure to run it.  So, I created a procedure to loop over and expire all records more than 7-days old:

```sql
mysql> DELIMITER |
  CREATE PROCEDURE expire_procedure()
    BEGIN
      UPDATE pass SET pass.status = 'expired'
        WHERE (DATEDIFF(NOW(),pass.addedon) > 7);
    END;
|
DELIMITER ;
Query OK, 0 rows affected (0.00 sec)

mysql> CALL expire_procedure();
Query OK, 1 row affected (0.01 sec)

mysql> select * from pass;
+----+---------+---------------------+
| id | status  | addedon             |
+----+---------+---------------------+
|  1 | active  | 2009-07-08 22:20:18 |
|  2 | active  | 2009-07-08 22:20:19 |
|  3 | expired | 2009-06-30 22:06:03 |
|  4 | active  | 2009-07-08 22:20:22 |
+----+---------+---------------------+
4 rows in set (0.00 sec)
```

Sure enough, the correct record was set to "expired".  So, the procedure works fine (I knew it would) but I then tried to call it from a trigger:

```sql
mysql> DELIMITER |
  CREATE TRIGGER expire_trigger
    BEFORE UPDATE ON pass
    FOR EACH ROW BEGIN
      CALL expire_procedure();
    END;
|
DELIMITER ;

mysql> update pass set id = 10 where id = 3;
ERROR 1442 (HY000): Can't update table 'pass' in stored
  function/trigger because it is already used by statement
  which invoked this stored function/trigger.
```

OK, so even using a procedure with a trigger isn't going to solve my problem.  I still see the same error as before.

### Conclusion

Bottom line, there doesn't appear to be a way to use a trigger in MySQL that loops over all rows in a table when activated.  And, calling a procedure from a trigger isn't going to work either.  Solutions to this problem might include building your web-app to call your procedure at a given interval.  Or, call the procedure when a user logs in (depending on the load).  You might even consider using a cron job.

Good luck.

<!--- tags: mysql -->