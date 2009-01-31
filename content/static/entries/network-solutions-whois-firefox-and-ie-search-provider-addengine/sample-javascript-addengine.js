/*
 * xml is the full URL to your XML file (e.g., http://mark.kolich.com/network-solutions-whois/network-solutions-whois.xml)
 * icon is the full URL to the provider icon file (e.g., http://mark.kolich.com/network-solutions-whois/netsol-whois-search-icon.png)
 * provider is the name of the provider (e.g., networksolutions.com)
 * cat is the search provider category (e.g., Web)
 *
 */
function addEngine(xml,icon,provider,cat){
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
}

// Here's an example of how you might call addEngine() using an <a href="#" onClick="...">
addEngine(
	"http://mark.kolich.com/network-solutions-whois/network-solutions-whois.xml",
	"http://mark.kolich.com/network-solutions-whois/netsol-whois-search-icon.png",
	"networksolutions.com", "Web" );

// Here's an example of how you might call addEngine() using
// a jQuery event handler
$("a[engineXML]").click(function(ev){
  var xml = $(this).attr("engineXML");
  var icon = $(this).attr("icon");
  var provider = $(this).attr("provider");
  var cat = $(this).attr("cat");
  addEngine(xml,icon,provider,cat);
  return false;
});

