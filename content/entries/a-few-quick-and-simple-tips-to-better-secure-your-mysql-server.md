Most MySQL server installations floating around on the Internet are blindly using the default server configuration.  Even more shocking, you'd be surprised to find out how many of these MySQL server installations are left wide open, completely vulnerable to attacks.  In this post, I'll provide a few simple tips you can use to better secure your MySQL server.  Note that these tips should be used as a basic starting point; they are not an end-all-be-all MySQL security solution.  You still need to know what you're doing, and if you're unsure, find a consultant or friend who can help you lock down your server.

### Change the Default Admin Username and Password

Sadly, many MySQL server installations are using the default admin username and password configuration.  By default, the administrative username for MySQL is "root" and the password is empty (no-password).  Anyone with root access to your MySQL server can log into your database, create new users, change or grant privileges, add or drop tables, etc.  So, let's change the root username to something less obvious, and while were at it, let's change the root password too:

```
(root)/> mysql -u root mysql

mysql> UPDATE user SET user = "mydbadmin" WHERE user = "root";
mysql> FLUSH PRIVILEGES;
```

### Delete Default Users

On occasion, several default database user accounts are created when you install MySQL.  You really shouldn't need these for any reason, so let's remove them:

```
(root)/> mysql -u mydbadmin -p mysql

mysql> DELETE FROM user WHERE NOT (host = "localhost" AND user = "mydbadmin");
mysql> FLUSH PRIVILEGES;
```

### Create Separate User Accounts for Each Application

Now, let's create a single MySQL account per application that needs access to your database server.  Some database administrators provide a single account that has global access to all MySQL databases and tables.  This is bad for a number of reasons, but mainly because if one application using the database is compromised, a hacker can then access all other tables and databases running on the server.

For the sake of this example, say your MySQL database will be used by a blog and a guestbook.  The blog and guestbook are separate web-applications, and therefore, should have their own MySQL username and password.  The idea is that the blog MySQL account shouldn't have access to the guestbook database and vice versa.  If a hacker compromises the username and password used to access the blog database, they won't be able to access the guestbook database.  So, let's create a separate database and user account for the blog and guestbook:

```
(root)/> mysql -u mydbadmin -p

mysql> CREATE DATABASE blog;
mysql> CREATE DATABASE guestbook;

mysql> CREATE USER 'blgu'@'localhost' IDENTIFIED BY 'somepass';
mysql> CREATE USER 'gbu'@'localhost' IDENTIFIED BY 'anotherpass';

mysql> GRANT ALL PRIVILEGES ON blog.* TO 'blgu'@'localhost' WITH GRANT OPTION;
mysql> GRANT ALL PRIVILEGES ON guestbook.* TO 'gbu'@'localhost' WITH GRANT OPTION;

mysql> FLUSH PRIVILEGES;
```

Be sure to use good passwords when creating your new MySQL user accounts.

### Delete the Sample Databases

As a general rule of thumb, you should delete or disable anything you don't explicitly need.  This includes the sample databases created by MySQL at install time.  On most MySQL installations, the sample/test database is named "test".  Let's remove it since it's just another component we don't need:

```
(root)/> mysql -u mydbadmin -p

mysql> DROP DATABASE test;
mysql> quit;
```

Cheers.

<!--- tags: mysql, security -->