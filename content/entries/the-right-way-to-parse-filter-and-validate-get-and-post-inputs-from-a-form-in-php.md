There are an endless number of ways to parse and validate `GET` or `POST` inputs to a PHP script.  I've seen examples (and have written code) that use everything from [array_key_exists](http://us.php.net/array_key_exists) to [empty](http://us.php.net/empty) to [preg_match](http://us.php.net/preg_match).  In any event, there doesn't appear to be a standard way of parsing and validating form inputs from a `GET` or a `POST`; each developer and application seems to use its own validation mechanism.  So, what is the PHP community preferred way to safely and securely validate submitted formvars?  I don't know.  But, I think I personally use a pretty robust and secure method for validating `GET` and `POST` form inputs in PHP.  So, consider this post an attempt to document and explain what I use in PHP to securely parse, filter, and validate formvars from a `GET` or `POST`.

For form validation, assuming you're running PHP 5.2.0 or greater, you really can't do much better than PHP's very own [filter_input_array](http://us.php.net/manual/en/function.filter-input-array.php) function.  If you're not familiar with `filter_input_array`, you should look into it.  From php.net, `filter_input_array`, "...gets external variables and optionally filters them."  In other words, filter_input_array gives you a lot parsing and validation logic for free.  It avoids the need to manually walk through each element of `$_`POST`[]`, check if it exists, etc.  And for an AJAX controller (a handler) written in PHP, `filter_input_array` can be very helpful.

In most cases, I find that a nice combo of `filter_input_array` and `empty` works quite nicely for most of my form validation needs.

### The &lt;form&gt;

Here's a reasonably complex `<form>` that should exercise `filter_input_array`.  In this form, I have a bunch of text inputs, some check boxes, and a hidden field.  One of the text inputs must be a valid email address, another must be a seven digit number (all digits, no non-digit characters), and the last one must be a number between 0 and 20.  The hidden field must be a valid URL (maybe useful for a script redirect or something like that).  The form will be submitted via a `POST`.

```html
<form method="post" action="<?= $_SERVER['PHP_SELF']; ?>">

  <input type="text" name="email" />
  <input type="text" name="sevendigits" />
  <input type="text" name="zerototwenty" />

  <input type="checkbox" name="checkboxes[]" value="cb1" />
  <input type="checkbox" name="checkboxes[]" value="cb2" />
  <input type="checkbox" name="checkboxes[]" value="cb3" />

  <input type="hidden" name="validurl" value="http://kolich.com" />

  <input type="submit" value="Submit" />

</form>
```

### The Ole' Fashioned (Common) Way to Parse Formvars

The common way to parse a form like this on the backend is to load each formvar from the `$_`POST`[]` array into separate variables and then check their value:

```php
$email = $_`POST`['email'];
$sevendigits = $_`POST`['sevendigits'];
$zerototwenty = $_`POST`['zerototwenty'];
// ... and so on.

if(empty($email)){
  // Empty email, show error or do something else.
  return;
}

if(!empty($sevendigits)){
  if(preg_match("/^(\d{7})$/",$sevendigits)){
    $sevendigits = intval($sevendigits);
  } else {
    // Not seven digits
  }
} else {
  // Error, sevendigits wasn't submitted.
}

// ... and so on, you get the idea.
```

A lot of code, and time, is wasted upfront parsing, checking, and validating each of the formvars from the `POST`.  Also, notice all of the nested and cumbersome if/else's all over the place.  We can definitely do better.

### Using filter_input_array() Instead

Instead of checking each field one-by-one, in **PHP 5.2.0** or later we can define a filter upfront that does most of this tedious work for us.  The filter will return "...an array containing the values of the requested variables on success (if the input matches the required format), or FALSE on failure. An array value will be FALSE if the filter fails, or NULL if the variable is not set."

Here's some sample code that defines a filter and uses `filter_input_array` to validate the incoming variables from a `POST`:

```php
<?php

// Works only in PHP 5.2.0 or later.

$filter = array(

   // This ensures that $_`POST`['email'] is actually a
   // valid email address.
   'email' => FILTER_VALIDATE_EMAIL,

   // This filter verifies that $_`POST`['sevendigits'] is
   // exactly a seven digit number using a regular expression.
   'sevendigits' => array('filter'  => FILTER_VALIDATE_REGEXP,
                          'options' => array('regexp' => '/^\d{7}$/')
                          ),

   // Make sure that $_`POST`['zerototwenty'] is a number
   // 0 to 20 using the min_range and max_range specs.
   'zerototwenty' => array('filter'  => FILTER_VALIDATE_INT,
                           'options' => array('min_range' => 0,
                                              'max_range' => 20)
                           ),

   // Verify that the incoming $_`POST`['checkboxes'] from
   // the checkbox list is actual an array like we expect.
   'checkboxes' => array('filter' => FILTER_VALIDATE_INT,
                         'flags'  => FILTER_REQUIRE_ARRAY,
                         ),

   // Make sure that the hidden URL field is a valid
   // properly formatted URL.
   'validurl' => FILTER_VALIDATE_URL,

   // ----- A few other filter examples not included
   // ----- in the <form> sample above.  I just felt like
   // ----- experimenting with a few other filters.

   // This field must be a boolean type.  If mustbeboolean is
   // "true", "1", "TRUE" or some other value that represents
   // true then this will be true.  Otherwise, it will be false.
   'mustbeboolean' => array('filter' => FILTER_VALIDATE_BOOLEAN),

   // Encode a URL that we need encoded from $_`POST`['encodeurl']
   'encodeurl' => array('filter' => FILTER_SANITIZE_ENCODED),

   // This dosen't exist in the form, I'm just using it to show
   // what the result will be when an input doesn't exist.
   'doesnotexist' => FILTER_VALIDATE_INT

);

// Filter and sanitize the incoming $_`POST`[] with the filter above.
$inputs = filter_input_array( INPUT_`POST`, $filter );

// Here's an example of checking if $_`POST`['email'] made it
// past our FILTER_VALIDATE_EMAIL filter.
if( empty($inputs['email']) ) {
   echo "Empty or invalid email entered.";
}

?>
```

As you can see, we define a set of filters upfront that validates the input from `$_`POST`[]`.  The result is stored in `$inputs`, which we can then access like any other associative PHP array.  BTW, additional filter constants like `FILTER_SANITIZE_ENCODED`, `FILTER_VALIDATE_IP`, etc. can be found on PHP's [Predefined Filter Constants page](http://us.php.net/manual/en/filter.constants.php).

### A More Complete Example

To test the example code above, I threw together some PHP which you can [find here](static/entries/the-right-way-to-parse-filter-and-validate-get-and-post-inputs-from-a-form-in-php/php-filter-input-array-example.php).

### Using filter_input_array() with AJAX

In terms of usefulness, `filter_input_array` is perfect for an AJAX controller written in PHP.  Imagine a controller expecting three or so formvars from a `$_GET`.  Instead of checking for each one manually, one-by-one, you can define a filter upfront that checks and validates the incoming formvars.  From there, a simple `empty()` check is sufficient to see if the variable matched the desired type, or format.

Here's a sample AJAX controller illustrating the use of `filter_input_array` with a `GET`.  Note that this script might be called with a request that looks like `../ajax/controller.php?idnumber=123&field=op&status=true`:

```php
<?php

// Only works in PHP 5.2.0 or later.

// A sample AJAX controller written in PHP that shows
// how to use filter_input_array().

$filter = array(

   // An ID number must be exactly 7-digits.
   'idnumber' => array('filter'  => FILTER_VALIDATE_REGEXP,
                       'options' => array('regexp' => '/^\d{7}$/')
                       ),

   // A field name to update.
   'field' => array('filter'=> FILTER_SANITIZE_STRING),

   // Set the field to either true or false.
   'status' => array('filter' => FILTER_VALIDATE_BOOLEAN)

);

try {

   // Filter $_GET[]
   $inputs = filter_input_array( INPUT_GET, $filter );

   $id = null;
   if( empty($inputs['idnumber']) ) {
      throw new Exception("Invalid ID number!");
   } else {
      $id = $inputs['idnumber'];
   }

   $field = null;
   if( empty($inputs['field']) ) {
      throw new Exception("Field to update cannot be empty!");
   } else {
      $field = $inputs['field'];
   }

   $status = false;
   if( !empty($inputs['status']) ) {
      $status = $inputs['status'];
   }

   // Do something here, probably with a database, to save/update
   // to set $field = $status for ID number $id

   echo "SUCCESS";

} catch ( Exception $e ) {
   echo "ERROR: " . $e->getMessage();
}

?>
```

Enjoy.