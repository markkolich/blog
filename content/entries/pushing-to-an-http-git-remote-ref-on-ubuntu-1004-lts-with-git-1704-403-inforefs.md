I'm running Ubuntu 10.04 LTS on several development machines.  By default, Git 1.7.0.4 ships with this version of Ubuntu, as provided by Ubuntu Canonical.  This is hardly the latest "stable" version of Git, but it works fine and is made available without any hacky Ubuntu PPA's.

```plain
#/> git --version
git version 1.7.0.4
```

Recently, I've been kicking the tires on moving many of my "internal" (eh, personal) SVN repositories to GitHub in an effort, perhaps perilously, to make some of my work a bit more publicly visible.

So, I created a new repository on GitHub, and cloned it to my local machine using the Read+Write HTTPS endpoint.  From what I understand, I should be able to use this HTTPS endpoint to push changes and pull updates.

```
#/> git clone https://github.com/markkolich/kolich-common.git
Initialized empty Git repository in /kolich-common/.git/
remote: Counting objects: 52, done.
remote: Compressing objects: 100% (38/38), done.
remote: Total 52 (delta 9), reused 48 (delta 8)
Unpacking objects: 100% (52/52), done.
```

Looks good, and I confirmed that Git pull's work fine.

```
#/kolich-common> git pull
Already up-to-date.
```

Now, if I make some changes, commit those changes to my local repository, then attempt to push to the remote, I'll see a silly 403 Forbidden error in response to my push.

```
#/kolich-common> git push origin master
error: The requested URL returned error: 403 while accessing
https://github.com/markkolich/kolich-common.git/info/refs

fatal: HTTP request failed
```

Huh?  Forbidden?  I can haz prompt for username and password?

### GIT_CURL_VERBOSE

I set the handy GIT_CURL_VERBOSE environment variable, and tried my push again (so Git would show me what it's up to under the hood).

```
#/kolich-common> GIT_CURL_VERBOSE=1 git push origin master
...
> GET /markkolich/kolich-common.git/info/refs HTTP/1.1
User-Agent: git/1.7.0.4
Host: github.com
Accept: */*
Pragma: no-cache

* The requested URL returned error: 403
* Closing connection #0
error: The requested URL returned error: 403 while accessing
https://github.com/markkolich/kolich-common.git/info/refs

fatal: HTTP request failed
```

There's a bit of output there, but the most useful part of that transaction was the final request and failure.  I was expecting Git to prompt me for my GitHub credentials, and therefore, would see an HTTP Authorization request header sent with the final GET request to .../info/refs.

### Solution: Add Your GitHub Username to Your "remote.origin.url"

With a little debugging, I realized that if you set the "remote.origin.url" in your Git config to start with a username, that will trigger Git 1.7.0.4 to prompt you for a password on any operation that requires authentication.

Here's what a **remote.origin.url** looks like following a clone.

```
#/kolich-common> git config -l | grep remote.origin.url
remote.origin.url=https://github.com/markkolich/kolich-common.git
```

So, change this property to start with [your GitHub username]@github.com and you'll be all set.

```
#/kolich-common> git config remote.origin.url \
"https://markkolich@github.com/markkolich/kolich-common.git"
```

Note the "https://markkolich@github.com" on the new URL. Of course, your GitHub username will be different.

### You Can Haz Git Prompt for Password

Now, try the same Git push again, and this time you'll be prompted for a password given that the push operation "requires authentication".

```command-line
#/kolich-common> git push origin master
Password:
Counting objects: 5, done.
Delta compression using up to 4 threads.
Compressing objects: 100% (3/3), done.
Writing objects: 100% (3/3), 290 bytes, done.
Total 3 (delta 2), reused 0 (delta 0)
To https://markkolich@github.com/markkolich/kolich-common.git
   46f194c..cc81442  master -> master
```

Look at that nice "Password:" prompt.

My entered password is correct, the push succeeded, and away I go!

### Wait, Shouldn't You Just Use SSH Keys Instead?

Yeah, probably not a bad idea.

It's just that I have 4 separate development environments and I didn't really feel comfortable copying my [GitHub SSH key](https://help.github.com/articles/generating-ssh-keys#platform-all) to multiple computers, some of which are property of my employer.  That said, I'd rather just be prompted for my password if pushing to a "personal" GitHub repository -- which frankly, really isn't all that often.

But yes, most folks would create a new SSH key (one per computer) then add the public side of those keys to GitHub.  I just found it easier to use a password.
