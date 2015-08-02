This is a follow-up post to my [Generating Good Random Passwords With /dev/urandom](howto-generating-good-random-passwords-with-devurandom) entry.  A local system administrator professional contacted me with a few real-life scenarios for good password generation and provided some suggestions on how to handle them.

* Creating reasonably complicated passwords for a school (for an entire student body).
* Creating complex passwords for a company/business with strict password requirements.
* Creating passwords for a company/business with an anal-retentive network admin (super complex passwords with special characters like #%^*&!. and so on).

Forget expensive and ridiculous "password generation" software.  If you have access to a Linux box, or a UNIX box, then you've got a password generator.

Here's a few scenarios and solutions:

### Create reasonably complicated passwords for a school (for an entire student body)

"Printers are lame and people can't read.  What that means is we cannot use the number zero or the capital letter O.  We cannot use the number one, the letter l "el", or the capital I "i".  Long passwords are harder to learn so we will limit ours to six characters.  It is also nice to limit this to all lowercase letters because kids get confused whenever caps-lock has a chance of accidentally getting turned on."

```bash
#!/bin/bash
#
# Script to generate X passwords and 'tee'
# the results to a file named passwords.txt
#
X=1000
i=1
while [ $i -le $X ]
do
    head -c 500 /dev/urandom | tr -dc a-hj-km-npr-z2-9 \
        | head -c 6 | tee -a passwords.txt;
        echo | tee -a passwords.txt;
    let "i+=1"
done
```

Sample passwords from this solution (what they'll look like):

```
39uy9n
h52bx7
m6agtz
6cmbwj
```

### Create complex passwords for a company/business/school/home-use with strict password requirements.

"All letters.  Upper and lower case.  All digits.  Length must be at least ten characters. Guarantee: passwords will end up on sticky notes."

```bash
#!/bin/bash
#
# Script to generate X passwords and 'tee'
# the results to a file named passwords.txt
#
X=1000
LENGTH=10
i=1
while [ $i -le $X ]
do
    head -c 500 /dev/urandom | tr -dc a-zA-Z0-9 \
        | head -c $LENGTH | tee -a passwords.txt;
        echo | tee -a passwords.txt;
    let "i+=1"
done
```

Sample passwords from this solution (what they'll look like):

```
co3Jr0uEKg
SPIuKLMk7h
C69OsDVbyc
XFkdNK7Hfa
```

### Create passwords for a company/business/school/home-use with an anal-retentive network admin.

"Super long and complex passwords with special characters like #%^*&!. and so on.  Guarantee: passwords will end up on sticky notes."

```bash
#!/bin/bash
#
# Script to generate X passwords and 'tee'
# the results to a file named passwords.txt
#
X=1000
LENGTH=16
SEED=1000
i=1
while [ $i -le $X ]
do
    head -c $SEED /dev/urandom | tr -dc [:punct:]a-zA-Z0-9 \
        | head -c $LENGTH | tee -a passwords.txt;
        echo | tee -a passwords.txt;
    let "i+=1"
done
```

Sample passwords from this solution (what they'll look like):

```
Nb9|2Cb$LT;,=t-4
([[Y?#>VH]_c%fEU
qv-_)x#nU+OEyav&
e~fZ@<}2'2a|)TGV
```

The ultra paranoid should take a look at [The Diceware Passphrase Home Page](http://world.std.com/~reinhold/diceware.html) for more information on actually using one or more dice to generate a password (a.k.a., passphrase).  From the Diceware web-site:  "[Diceware is a method for picking passphrases](http://en.wikipedia.org/wiki/Diceware) that uses dice to select words at random from a special list called the [Diceware Word List](http://zzzen.com/dialdice.html). Each word in the list is preceded by a five digit number. All the digits are between one and six, allowing you to use the outcomes of five dice rolls to select one unique word from the list."

Cheers.

<!--- tags: security, bash -->