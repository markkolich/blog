(function($, parent, window, document, undefined) {

	var

		// Namespace.
		self = parent.Twitter = parent.Twitter || {},

        console = parent['console'],

        tweetPanel = $('div.panel.twitter'),
        tweetBody = tweetPanel.find('div.panel-body'),

        sampleJson = {"tweets":[{"id_str":"417006335452913664","created_at":"2013-12-28T10:57:12.000Z","text":"Oh hey, last Saturday of the year."},{"id_str":"416823787485536256","created_at":"2013-12-27T22:51:49.000Z","text":"@killerswan ever."},{"id_str":"416823348748767232","created_at":"2013-12-27T22:50:05.000Z","text":"In Scala, I strongly dislike having to write:\n  val x \u003d if(c) \"foo\" else \"bar\"\n\nOh, idk, how about:\n  val x \u003d c ? \"foo\" : \"bar\"\n\n#ternary"},{"id_str":"416818571738509313","created_at":"2013-12-27T22:31:06.000Z","text":"@royclarkson the worst"},{"id_str":"416818190228783104","created_at":"2013-12-27T22:29:35.000Z","text":"RT @embee: what happens when non-tech people discover web developer tools http://t.co/ApyRlC4niw"},{"id_str":"416759582044585985","created_at":"2013-12-27T18:36:42.000Z","text":"RT @zugzugglug: For those who haven’t yet experienced the joy of programming, now’s a great week to try an Hour of Code: http://t.co/hpoDKJ…"},{"id_str":"416759174249213952","created_at":"2013-12-27T18:35:04.000Z","text":"Fine-Grained Concurrency with Guava \"Striped\" http://t.co/xKwxxTJ0ob #java"},{"id_str":"416676341400875008","created_at":"2013-12-27T13:05:55.000Z","text":"It\u0027ll be quick they say.... two hours later."},{"id_str":"416675952169451520","created_at":"2013-12-27T13:04:23.000Z","text":"@benmilligan +1"},{"id_str":"416674100866596864","created_at":"2013-12-27T12:57:01.000Z","text":"Man sit, waiting, hungry. Woman shop, all day long, there is nothing else. #poetry"}]},

        linkify = (function() {
            var links = function(tweet) {
                var exp = /(\b(https?|ftp|file):\/\/[-A-Z0-9+&@#\/%?=~_|!:,.;]*[-A-Z0-9+&@#\/%=~_|])/ig;
                return tweet.replace(exp,"<a href='$1' target='_blank'>$1</a>");
            },
            users = function(tweet) {
                var exp = /\B\@([\w\-]+)/gim;
                return tweet.replace(exp, "<a href='https://twitter.com/$1' target='_blank'>@$1</a>");
            };
            return function(tweet) {
                return users(links(tweet));
            };
        }()),

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

		init = function() {
            var tweets = sampleJson.tweets;
            if(tweets && tweets.length > 0) {
                var ul = $('<ul>');
                for(var i in tweets) {
                    var tweet = tweets[i], li = $('<li>').addClass('tweet small');
                    var text = $('<p>').append(linkify(tweet.text));
                    var timestamp = $('<p>').append(tweet.created_at).addClass('smaller');
                    li.append(text).append(timestamp);
                    ul.append(li);
                }
                ul.find('li.tweet').first().addClass('first');
                tweetBody.append(ul);
            }
        };

    (tweetPanel.length > 0) && init();

})(jQuery, Kolich.Blog || {}, this, this.document);
