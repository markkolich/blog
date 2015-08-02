Traditionally, I've relied on online JavaScript password generators to create my passwords.  I prefer extremely complicated ones, like **sPtBf4CDuul9Wlol** and **H0SD9BHz4xHIah0h** (at least 16-characters are preferred).  However, when I'm setting up a new system for myself, or configuring user accounts on a shared box at work, JavaScript password generators are slightly inconvenient if I don't have a web-browser handy.  Plus, if I need to generate multiple passwords for many users, it's unrealistic to use to a JavaScript password generator embedded in a web-browser.

Luckily, a colleague at work pointed out an easy way of generating good passwords using `/dev/urandom` on Linux:

```
head -c 500 /dev/urandom | tr -dc a-z0-9A-Z | head -c 16; echo
```

On OSX, run:

```
env LC_CTYPE=C tr -dc "a-zA-Z0-9-_\$\?" < /dev/urandom | head -c 10
```

On HP-UX, run:

```
head -n 500 -c /dev/urandom | tr -dc a-z0-9A-Z | head -n 16 -c
```

Note you can change the "16" in the final call to `head` to get a different password length of your choice.

Wikipedia has a lot of good information on selecting a good password.  [Password strength](http://en.wikipedia.org/wiki/Password_strength) is an interesting problem, and different folks have different opinions with regards to what constitutes a "good" password.  Regardless, I suggest following a few key rules when creating a password:

* Include numbers, symbols, upper and lowercase letters in passwords.
* Password length should be around 12 to 14 characters.
* Avoid any password based on repetition, dictionary words, letter or number sequences, usernames, relative or pet names, or biographical information (eg, dates, ID numbers, ancestors names or dates, ...).

Cheers.

<!--- tags: security, bash -->