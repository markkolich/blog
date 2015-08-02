I just discovered that dealing with MySQL triggers, in many instances, is quite painful.  For example, here's a trigger that deletes a bunch of rows in a table on every `INSERT`:

```sql
delimiter |
CREATE TRIGGER delete_expired_tweets AFTER INSERT ON tweets
  FOR EACH ROW BEGIN
    DELETE FROM tweets WHERE DATEDIFF(NOW(), created_at) > 365;
  END;
|

delimiter ;
```

Ok, let's load this trigger into MySQL:

```
#/> mysql -h myhost -u normaluser -p mydatabase
Enter password: **********
Welcome to the MySQL monitor.  Commands end with ; or \g.
Your MySQL connection id is 10
Server version: 5.0.41-community-nt MySQL Community Edition (GPL)

Type 'help;' or '\h' for help. Type '\c' to clear the buffer.

mysql> source trigger.sql
ERROR 1227 (42000): Access denied; you need the SUPER privilege
    for this operation
```

Access denied?  I dug into it, and confirmed that you can only add triggers if your user account has the `SUPER` privilege enabled.  You're probably thinking, "No kidding Sherlock, that's what the error message says."  Yes, I know that's what the error message says.  But here's the problem.  Normal database users created using `GRANT ALL PRIVILEGES ON database.* TO...` will not have the `SUPER` privilege assigned to them by default.  As described here, the SUPER privilege in MySQL let's the account do some things that normal database users, in most environments, should not be able to do (like kill database threads, modify global system variables, etc.).  As a result, it's a very bad idea to grant the `SUPER` privilege to normal database users, even if they just need the `SUPER` privilege to load a trigger.  You know better than that!

Even worse, suppose you `GRANT SUPER PRIVILEGES` to a single user, on a single database.  Well, that still won't be enough to load a trigger.  Unfortunately, loading triggers requires SUPER PRIVILEGES at the global level (e.g., `GRANT SUPER PRIVILEGES ON *.*`).  Again, it's a very bad ideal to grant normal database users the `SUPER` privilege.

So how exactly am I supposed to load this trigger?  Well as far as I can tell, assuming I refuse to give myself `SUPER PRIVILEGES` for the reasons I just explained, I have two options:

1. Don't use triggers, and find another way to cleanup rows in my table.
2. Log into the database as root/admin and load the trigger on behalf of the normal user.  If I wasn't the owner of this database server, this would probably involve asking my database administrator to load the trigger for me.

Just one of many common annoyances with MySQL.

<!--- tags: mysql, security -->