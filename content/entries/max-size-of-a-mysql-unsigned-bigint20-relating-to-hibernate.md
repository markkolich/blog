Just for grins, I decided to run a few tests to determine the max size of an `UNSIGNED BIGINT(20)`.  I use several databases where Hibernate's `OBJ_VERSION` field is defined as an `UNSIGNED BIGINT(20)`, and I was curious to see how MySQL behaves with very large numbers.  As it turns out, the max size of an `UNSIGNED BIGINT(20)` is:

```
18,446,744,073,709,551,615
```

To prove it:

```sql
mysql> UPDATE colors SET c_name = 'Blue',
        OBJ_VERSION = 18446744073709551615
         WHERE c_code = 'BLU';
Query OK, 0 rows affected (0.00 sec)
Rows matched: 1  Changed: 0  Warnings: 0

mysql> UPDATE colors SET c_name = 'Blue',
        OBJ_VERSION = 18446744073709551616
         WHERE c_code = 'BLU';
ERROR 1264 (22003): Out of range value for column 'OBJ_VERSION' at row 1
```

I thought that MySQL might "wrap the value" around back to zero if you try to UPDATE with a value greater than the column's max.  But, it definitely does not.  In my case, this is probably not too important because in order to reach `18,446,744,073,709,551,615` Hibernate would have to pound on one specific database record over a [quintillion (10^18) times](http://en.wikipedia.org/wiki/Names_of_large_numbers).

It seems a little presumptuous, but I guess Hibernate operates on the assumption that a real database would never reach this limit.

Food for thought...