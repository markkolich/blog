(function($, parent, window, document, undefined) {

	var

		// Namespace.
		self = parent.Blog = parent.Blog || {},

		baseAppUrl = parent['baseAppUrl'],
		api = parent['api'],

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
        };

    init();

})(jQuery, Kolich || {}, this, this.document);
