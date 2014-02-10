(function($, parent, window, document, undefined) {

	var

		// Namespace.
		self = parent.Provider = parent.Provider || {},

        baseAppUrl = parent['baseAppUrl'],
        engines = $("a[enginexml]"),

        addEngine = function(xml, icon, provider, cat) {
            if((typeof window.sidebar=="object")&&(typeof window.sidebar.addSearchEngine=="function")){
                window.sidebar.addSearchEngine(xml,icon,provider,cat);
            }else{
                try{
                    window.external.AddSearchProvider(xml);
                }catch(x){
                    if(70==(x.number&0xFFFF)){
                        alert("For security reasons, you must use the mouse\n(or the Enter key) to click the Install button.");
                    }else{
                        alert("Unable to add search provider. [" + (x.number & 0xFFFF) + "]");
                    }
                    return false;
                }
            }
        },

		init = function() {
            engines.click(function(e) {
                var xml = baseAppUrl + $(this).attr("enginexml"),
                    icon = baseAppUrl + $(this).attr("icon"),
                    provider = $(this).attr("provider"),
                    cat = $(this).attr("cat");
                addEngine(xml, icon, provider, cat);
                e.preventDefault();
            });
        };

    (engines.length > 0) && init();

})(jQuery, Kolich.Blog || {}, this, this.document);
