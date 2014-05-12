(function($, parent, window, document, undefined) {

	var

		// Namespace.
		self = parent.Translate = parent.Translate || {},

		translateUrl = "http://translate.google.com/translate?u=%u&sl=en&tl=%tl",

		init = function() {
            $("p.translateflag").click(function(e) {
                var tl = $(this).attr("data-tl");
                var href = encodeURIComponent(window.location.href);
                window.top.location.href = translateUrl.replace("%u",href).replace("%tl",tl);
            });
        };

    init();

})(jQuery, Kolich.Blog || {}, this, this.document);
