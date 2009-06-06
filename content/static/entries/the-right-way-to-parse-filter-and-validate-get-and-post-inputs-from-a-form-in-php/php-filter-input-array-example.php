<?php

// Copyright (c) 2009 Mark S. Kolich
// http://mark.kolich.com
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.

define("POST", "POST");
define("GET", "GET");

// We switch on the method below.
$method = $_SERVER['REQUEST_METHOD'];

$filter = array(

	// This ensures that $_POST['email'] is actually a
	// valid email address.
    'email' => FILTER_VALIDATE_EMAIL,
	
	// This filter verifies that $_POST['sevendigits'] is
	// exactly a seven digit number using a regular expression.
	'sevendigits'    => array('filter'  => FILTER_VALIDATE_REGEXP,
                              'options' => array('regexp' => '/^\d{7}$/')
                             ),
	
	// Make sure that $_POST['zerototwenty'] is a number
	// 0 to 20 using the min_range and max_range specs.
	'zerototwenty'    => array('filter'   => FILTER_VALIDATE_INT,
                               'options' => array('min_range' => 0,
								                  'max_range' => 20)
                              ),
	
	// Encode the URL that's supposed to be encoded.
	'encodeurl' => array( 'filter' => FILTER_SANITIZE_ENCODED ),	
	
	// Verify that the incoming $_POST['checkboxes'] from
	// the checkbox list is actual an array like we expect.
	'checkboxes' => array( 'filter' => FILTER_VALIDATE_INT,
                           'flags'  => FILTER_REQUIRE_ARRAY,
                         ),
	
	// This field must be a boolean type.  If mustbeboolean is
	// "true", "1", "TRUE" or some other value that represents
	// true then this will be true.  Otherwise, it will be false.
	'mustbeboolean' => array( 'filter' => FILTER_VALIDATE_BOOLEAN ),
						   
	// Make sure that the hidden URL field is a valid
	// properly formatted URL.
	'validurl' => FILTER_VALIDATE_URL,
	
	// This dosen't exist in the form, I'm just using it to show
	// what the result will be when an input dosen't exist.
    'doesnotexist' => FILTER_VALIDATE_INT
	
);

?>
<html><head>
  <title>PHP filter_input_array() Example</title>
</head><body>
<?

if ( $method === POST ) {	
	
	// Filter and sanitize the incoming $_POST[] through the
	// filters we defined above.
	$inputs = filter_input_array( INPUT_POST, $filter );
	
	?> <pre> <? var_dump( $inputs ); ?> </pre> <?
	
	?> <ul> <?
	
	if(empty($inputs['email'])){
		echo "<li>Email was empty or invalid</li>";
	}
	
	if(empty($inputs['sevendigits'])){
		echo "<li>sevendigits was not seven digits.</li>";
	}
	
	if(empty($inputs['zerototwenty'])){
		echo "<li>zerototwenty was not a number between 0 and 20.</li>";
	}
	
	if(empty($inputs['encodeurl'])){
		echo "<li>encodeurl was empty or invalid.</li>";
	}
	
	if(empty($inputs['checkboxes'])){
		echo "<li>no checkboxes were checked.</li>";
	}
		
	?> </ul> <?
	
}
else {

	?>
	<form method="post" action="<?= $_SERVER['PHP_SELF']; ?>">

		Email: <input type="text" name="email" value="email@example.com" /><br /><br />
		Seven Digits: <input type="text" name="sevendigits" maxlength="7" /><br /><br />
		0 to 20: <input type="text" name="zerototwenty" value="15" /><br /><br />
		
		Encode URL: <input type="text" name="encodeurl" value="http://kolich.com?dog=124&whatever=cool" /><br /><br />
		
		CB1: <input type="checkbox" name="checkboxes[]" value="1" /><br />
		CB2: <input type="checkbox" name="checkboxes[]" value="2" /><br />
		CB3: <input type="checkbox" name="checkboxes[]" value="3" /><br /><br />
		
		Must be a Boolean: <select size="1" name="mustbeboolean" >
		  <option value="true">true</option>
		  <option value="1">1</option>
		  <option value="notboolean">notboolean</option>
		  <option value="false">false</option>
		  <option value="FALSE">FALSE</option>
		  <option value="0">0</option>
		</select><br /><br />

		<input type="hidden" name="validurl" value="http://mark.kolich.com" />
		<input type="submit" value="Submit" />

	</form>	
	<?
	
}

?>
</body></html>