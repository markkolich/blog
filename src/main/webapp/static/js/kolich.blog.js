(function($, parent, window, document, undefined) {

	var

		// Namespace.
		self = parent.Blog = parent.Blog || {},

		init = function() {
            $('pre').addClass('prettyprint');
            window.prettyPrint && prettyPrint();
            $('div.entry').click(function(e) {
                var node = e.target.nodeName;
                if(node != 'A') {
                    $(this).find('div.fader').remove();
                    $(this).css('max-height','none').css('cursor','auto');
                    e.preventDefault();
                }
            });
            //alert($('p.hash').last().html());
        };

    self['path'] = parent['path'];
    self['baseAppUrl'] = parent['baseAppUrl'];

    self['debug'] = parent['debug'];
    self['console'] = parent['console'];

    init();

})(jQuery, Kolich || {}, this, this.document);
