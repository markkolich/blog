(function($, parent, window, document) {

	var

		// Namespace.
		self = parent.Blog = parent.Blog || {},

		console = parent['console'],
		baseAppUrl = parent['baseAppUrl'],
        baseApiUrl = parent['baseApiUrl'],

		blogJsonApi = baseApiUrl + "blog.json",

        moreButton = $('button.more'),

		init = (function() {
		    var
                prettyprint = function() {
                    $('pre:not(.prettyprint)').addClass('prettyprint');
                    window.prettyPrint && prettyPrint();
                },
                reveal = function() {
                    $('div.entry').unbind().click(function(e) {
                        var node = e.target.nodeName;
                        if(node != 'A') {
                            $(this).find('div.fader').remove();
                            $(this).css('max-height','none').css('cursor','auto');
                            e.preventDefault();
                        }
                    });
                },
                more = function() {
                    moreButton.click(function(e) {
                        // Immediately disable the "Load More" button on click; it will
                        // be re-enabled later once the AJAX request completes.
                        moreButton.attr('disabled', 'disabled');
                        var moreBtnDiv = $(this).parent();
                        var lastCommit = $('p.hash:last').html();
                        $.getJSON(blogJsonApi, {before: lastCommit}, function(json) {
                            var entries = json.content, count = entries.length;
                            // Hide the "load more" button if no entries are left.
                            // The user is at the end of the entry list.
                            if(json.remaining <= 0) {
                                moreBtnDiv.slideUp('fast');
                            }
                            for(var i in entries) {
                                var entry = entries[i];
                                var entryDiv = $('<div>').addClass('entry').attr('id',entry.commit).hide(),
                                    h2 = $('<h2>').addClass('title'),
                                    link = $('<a>').attr('href',baseAppUrl+entry.name).html(entry.title),
                                    hash = $('<p>').addClass('hash').html(entry.commit),
                                    date = $('<p>').addClass('date').html(entry.date),
                                    content = $('<p>').html(entry.html),
                                    fader = $('<div>').addClass('fader');
                                entryDiv.append(h2.append(link)).append(hash).append(date).append(content);
                                entryDiv.append(fader);
                                moreBtnDiv.before(entryDiv);
                                entryDiv.slideDown('fast', function() {
                                    // Only adjust the scroll position if we're in the callback for
                                    // the "last" fetched entry in the list.  This is so we don't
                                    // execute the same callback multiple times, just once at the end.
                                    if(--count <= 0) {
                                        var winHeight = $(window).height(),
                                            newTop = 0;
                                        if(moreButton.is(':visible')) {
                                            var btnHeight = moreButton.height() * 4,
                                                btnOffset = moreButton.offset().top;
                                            newTop = (btnOffset+btnHeight) - winHeight;
                                        } else {
                                            var newDivHeight = entryDiv.height(),
                                                newDivOffset = entryDiv.offset().top;
                                            newTop = (newDivOffset+newDivHeight) - winHeight;
                                        }
                                        $('html, body').animate({scrollTop:newTop}, 'fast');
                                        // Re-enable the "Load More" button.
                                        moreButton.removeAttr('disabled');
                                    }
                                });
                            } /* for */
                            prettyprint();
                            reveal();
                        });
                    });
                };
            return function() {
                prettyprint();
                reveal();
                (moreButton.length > 0) && more();
            };
        }());

    self['path'] = parent['path'];
    self['baseAppUrl'] = parent['baseAppUrl'];
    self['baseApiUrl'] = parent['baseApiUrl'];

    self['debug'] = parent['debug'];
    self['console'] = parent['console'];

    init();

})(jQuery, Kolich || {}, this, this.document);
