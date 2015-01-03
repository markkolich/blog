(function($, parent, window, document, undefined) {

	var

		// Namespace.
		self = parent.Twitter = parent.Twitter || {},

        username = 'markkolich',

        console = parent['console'],
        baseApiUrl = parent['baseApiUrl'],

        tweetsApi = baseApiUrl + 'tweets.json',

        tweetPanel = $('div.panel.twitter'),
        tweetBody = tweetPanel.find('div.panel-body.tweets'),

        linkify = (function() {
            var
                links = function(tweet) {
                    var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
                    return tweet.replace(exp,"<a href='$1' target='_blank'>$1</a>");
                },
                users = function(tweet) {
                    var exp = /\B@([\w\-]+)/gim;
                    return tweet.replace(exp, "<a href='https://twitter.com/$1' target='_blank'>@$1</a>");
                },
                backtickCode = function(tweet) {
                    var exp = /`(.*?)`/gim;
                    return tweet.replace(exp, '<code>$1</code>');
                },
                newLine = function(tweet) {
                    var exp = /[\n\r]/gim;
                    return tweet.replace(exp, '<br/>');
                };
            return function(tweet) {
                // Apply each of the transforms, in order.
                return [tweet]
                    .map(links)
                    .map(users)
                    .map(backtickCode)
                    .map(newLine)
                    .join('');
            };
        }()),

        linkifyTimestamp = function(id) {
            return "https://twitter.com/" + username + "/status/" + id;
        },

        /*
        // Logic borrowed from http://williamsportwebdeveloper.com/cgi/wp/?p=503
        // Refactored for my own needs.
        toISO8601String = (function() {
            var padzero = function(n) {
                    return n < 10 ? '0' + n : n;
                },
                pad2zeros = function(n) {
                    if (n < 100) {
                        n = '0' + n;
                    }
                    if (n < 10) {
                        n = '0' + n;
                    }
                    return n;
                };
            return function(d) {
                return d.getUTCFullYear() + '-'
                    + padzero(d.getUTCMonth() + 1)
                    + '-' + padzero(d.getUTCDate())
                    + 'T'
                    + padzero(d.getUTCHours()) + ':'
                    + padzero(d.getUTCMinutes()) + ':'
                    + padzero(d.getUTCSeconds()) + '.'
                    + pad2zeros(d.getUTCMilliseconds())
                    + 'Z';
            };
        }()),
        */

        load = function() {
            $.getJSON(tweetsApi, function(data) {
                var tweets = data.tweets;
                if(tweets && tweets.length > 0) {
                    var ul = $('<ul>').hide();
                    for(var i in tweets) {
                        var tweet = tweets[i],
                            li = $('<li>').addClass('tweet small'),
                            text = $('<p>').append(linkify(tweet.text)),
                            smaller = $('<p>').addClass('smaller'),
                            url = linkifyTimestamp(tweet.id_str),
                            //timestamp = $('<p>').append($.localtime.toLocalTime(tweet.created_at,'h:mm:ss a')).addClass('smaller');
                            timestamp = $('<a>').attr('href', url).append($.timeago(tweet.created_at));
                        smaller.append(timestamp);
                        li.append(text).append(smaller);
                        ul.append(li);
                    }
                    ul.find('li.tweet').first().addClass('first');
                    tweetBody.append(ul);
                    ul.slideDown(500);
                }
            });
        },

		init = function() {
            // Remove the tweet panel from the DOM if it's present but isn't
            // visible (hidden per our responsive design/CSS).
            if(tweetPanel.is(':visible')) {
                // Load tweets.
                load();
                // Only initialize+load the "Follow @markkolich" widget if the tweet
                // panel is visible per our responsive design.
                window.twttr = (function (d, s, id) {
                    var t, js, fjs = d.getElementsByTagName(s)[0];
                    if (d.getElementById(id)) return;
                    js = d.createElement(s); js.id = id;
                    js.src = 'https://platform.twitter.com/widgets.js';
                    fjs.parentNode.insertBefore(js, fjs);
                    return window.twttr || (t = { _e: [], ready: function (f) { t._e.push(f) } });
                }(document, 'script', 'twitter-wjs'));
            } else {
                // Remove panel from DOM.
                tweetPanel.remove();
            }
        };

    // Validate that the Twitter panel exists in the DOM, and it is visible
    // given our responsive CSS.  If the twitter panel is not visible (on
    // smaller devices) then don't even bother making a call to load the
    // Tweet feed since it can't be displayed anyways.
    (tweetPanel.length > 0) && init();

})(jQuery, Kolich.Blog || {}, this, this.document);
