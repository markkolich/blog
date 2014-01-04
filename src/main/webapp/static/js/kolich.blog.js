(function($, parent, window, document, undefined) {

	var

		// Namespace.
		self = parent.Blog = parent.Blog || {},

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
                }
                more = function() {
                    $('button.more').click(function(e) {
                        var more = $(this).parent();
                        var lastCommit = $('p.hash:last').html();
                        $.getJSON("blog.json", {before: lastCommit}, function(data) {
                            var entries = data.entries;
                            for(i in entries) {
                                var entry = entries[i];
                                var div = $('<div>').addClass('entry').hide(),
                                    h2 = $('<h2>').addClass('title'),
                                    link = $('<a>').attr('href',entry.name).html(entry.title),
                                    hash = $('<p>').addClass('hash').html(entry.commit),
                                    date = $('<p>').addClass('date').html(entry.date),
                                    content = $('<p>').html(entry.html),
                                    fader = $('<div>').addClass('fader');
                                div.append(h2.append(link)).append(hash).append(date).append(content);
                                div.append(fader);
                                more.before(div);
                                div.slideDown('fast');
                            }
                            $('html, body').animate({scrollTop:$(document).height()}, 'fast');
                            prettyprint();
                            reveal();
                        });
                    });
                };
            return function() {
                prettyprint();
                reveal();
                more();
            };
        }());

    self['path'] = parent['path'];
    self['baseAppUrl'] = parent['baseAppUrl'];

    self['debug'] = parent['debug'];
    self['console'] = parent['console'];

    init();

})(jQuery, Kolich || {}, this, this.document);
