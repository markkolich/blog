I fought through this issue today at work.  In PHP, you might have a need to parse some JSON that contains a field/member name with one or more hyphens.  Here's an example:

```json
{
  "field-one" : 1,
  "field-two" : 2,
  "field-three" : [ 1, 2, 3 ]
}
```

Most JSON libraries should convert this JSON string into a native object with ease.  However, in PHP when you try to access `$obj->field-one` directly, PHP will complain given the hyphen in the member name.  The solution to this simple problem, is to wrap the name in curly braces, like so:

```php
$obj->{'field-one'}
```

Here's a more complete example using [Services_JSON](http://pear.php.net/package/Services_JSON) in PHP:

```php
<?php

require_once("JSON.php");
$json = new Services_JSON();

$string = "{\"field-one\":1,\"field-two\":2,\"field-three\":[1,2,3]}";

$obj = $json->decode( $string );

// Will FAIL
//echo $obj->field-one;

// WORKS
echo $obj->{'field-one'};

?>
```

Cheers.