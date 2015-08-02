You may have noticed that a few sites out there trigger some type of event when you use your mouse to select a word or a block of text on the page.  After selecting some text, a little pop-up might appear allowing you to look up the definition of the selected word, or search Google for the selected phrase.  The New York Times online is a perfect example; while reading any of their articles, select a block of text with your mouse and you'll notice a [little balloon like icon](http://graphics8.nytimes.com/images/global/word_reference/ref_bubble.png) appears.  If you click the balloon icon, a pop-up window opens that back searches all New York Times articles for the selected text.  Like any reasonable software engineer, I was curious how the New York Times online implemented this select, click, and search feature.

As it turns out, implementing your own is quite easy with jQuery [as shown in my example](static/entries/use-javascript-and-jquery-to-get-user-selected-text/get-selected-text-javascript.html).

First, I borrowed this little [code snippet from CodeToad](http://www.codetoad.com/javascript_get_selected_text.asp), that offered a nice cross-browser compatible function for getting the user selected text in the browser.  I was hoping that a single call to get the selected text would work across all platforms, but of course, each browser has its own getSelection implementation:

```javascript
Kolich.Selector.getSelected = function(){
  var t = '';
  if(window.getSelection){
    t = window.getSelection();
  }else if(document.getSelection){
    t = document.getSelection();
  }else if(document.selection){
    t = document.selection.createRange().text;
  }
  return t;
}
```

Second, use jQuery to bind a mouseup event handler to the document:

```javascript
$(document).ready(function(){
  $(document).bind("mouseup", Kolich.Selector.mouseup);
});
```

From there, your event handler can simply call your `getSelected()` function to get the selected text and do something with it:

```javascript
Kolich.Selector.mouseup = function(){
  var st = Kolich.Selector.getSelected();
  if(st!=''){
    alert("You selected:\n"+st);
  }
}
```

Putting it all together, your code might look something like this:

```javascript
if(!window.Kolich){
  Kolich = {};
}

Kolich.Selector = {};
Kolich.Selector.getSelected = function(){
  var t = '';
  if(window.getSelection){
    t = window.getSelection();
  }else if(document.getSelection){
    t = document.getSelection();
  }else if(document.selection){
    t = document.selection.createRange().text;
  }
  return t;
}

Kolich.Selector.mouseup = function(){
  var st = Kolich.Selector.getSelected();
  if(st!=''){
    alert("You selected:\n"+st);
  }
}

$(document).ready(function(){
  $(document).bind("mouseup", Kolich.Selector.mouseup);
});
```

### Demo

Complete [demo here](static/entries/use-javascript-and-jquery-to-get-user-selected-text/get-selected-text-javascript.html).

<!--- tags: javascript -->