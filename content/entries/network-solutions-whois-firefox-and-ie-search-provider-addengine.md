I prefer [Network Solutions' WHOIS service](http://www.networksolutions.com/whois-search/koli.ch) over others because it appears to be one of the more complete and accurate WHOIS services available.  Because I use this WHOIS search service a lot, I decided to put together a quick search plugin/provider for Chrome, Firefox and Internet Explorer.

**<a href="javascript:void(0)" enginexml="static/entries/network-solutions-whois-firefox-and-ie-search-provider-addengine/network-solutions-whois.xml" icon="static/entries/network-solutions-whois-firefox-and-ie-search-provider-addengine/netsol-whois-search-icon.png" provider="networksolutions.com" cat="Web">Click here to add my Network Solutions WHOIS search provider to your browser.</a>**

<img src="https://raw.githubusercontent.com/markkolich/blog/release/content/static/entries/network-solutions-whois-firefox-and-ie-search-provider-addengine/network-solutions-whois-search-provider.png">

In fact, building your own search plugin/provider for IE and other browsers is relatively simple.  All you need is a small XML file that defines a few details about the provider, and a little JavaScript to trigger the install process.  My Network Solutions WHOIS [search plugin XML can be found here](static/entries/network-solutions-whois-firefox-and-ie-search-provider-addengine/network-solutions-whois.xml).  If you need to Base64 encode an `image/x-icon` for the provider XML, I suggest using [this converter](http://www.greywyvern.com/code/php/binary2base64).

Now, with regards to the JavaScript that triggers the search plugin installation in the browser (`addEngine`), take a look at this JavaScript example:

```javascript
function addEngine(xml,icon,provider,cat) {
  if((typeof window.sidebar=="object") && (typeof window.sidebar.addSearchEngine=="function")) {
    window.sidebar.addSearchEngine(xml,icon,provider,cat);
  } else {
   try {
    window.external.AddSearchProvider(xml);
   } catch (x) {
    if(70==(x.number&0xFFFF)){
      alert("For security reasons, you must use the mouse" +
             "\n(or the Enter key) to click the Install button.");
    } else {
      alert("Unable to add search provider. [" + (x.number & 0xFFFF) + "]");
    }
    return false;
  }
 }
}
```

The call to `window.external.AddSearchProvider()` is for Internet Explorer.  The call to `window.sidebar.addSearchEngine()` is for all other browsers.

Special thanks to Network Solutions for featuring my WHOIS browser plugin on their [Network Solutions Labs homepage](http://www.networksolutions.com/labs/index.jsp)!

<!--- tags: whois, network solutions -->