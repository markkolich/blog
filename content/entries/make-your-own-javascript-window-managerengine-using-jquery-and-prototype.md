Recently, I've been experimenting with [jQuery](http://jquery.com/) and the [jQuery UI](http://jqueryui.com/) interface for a few side-projects.  As of right now, I'm working on creating my own JavaScript windowing engine complete with a window manager, task bar, etc.  Not surprisingly, I found that [jQuery UI's Draggable](http://jqueryui.com/draggable/) interface provides a large majority of the desired drag-and-drop functionality for free.

<img src="https://raw.githubusercontent.com/markkolich/blog/release/content/static/entries/make-your-own-javascript-window-managerengine-using-jquery-and-prototype/jquery-window-engine.png" width="500">

Using UI Draggable, I created a functional drag-and-drop windowing interface in about 20 minutes.

### Lame Demo

[Here](static/entries/make-your-own-javascript-window-managerengine-using-jquery-and-prototype/jquery-ui-draggable-example.html).

Only took six lines of real JavaScript to make this demo &mdash; eight if you count jQuery's `$(document).ready` boilerplate:

```javascript
$(document).ready(function($) {
 $("[id^=win]").draggable({
   containment: 'document',
   stack: {group: $("[id^=win]"), min: 1},
   handle: $(".title"),
   opacity: 0.8
 });
});
```

Cheers.

<!--- tags: javascript, jquery -->