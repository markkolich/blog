In [Part 1](writing-versioned-service-apis-with-curacao-part1) of this series, I covered how to use Curacao to handle versioned resource requests.  That is, "how do clients specify the version of the resource they're asking for" given a number of implementation possibilities.  In this Part 2, let's talk routing and a few respective implementation strategies using [Curacao](https://github.com/markkolich/curacao).   

### Routing

How does the API route version specific requests?

#### Path

Without question, the most common mechanism used to "route" requests to the right controller is through the URI path.

By default, Curacao accomplishes this using regular expressions in conjunction with Java's named capture groups (in Java 7+) to pull values out of the path as needed.  For instance, consider the following RESTful like API requests that manipulate users in a data store.

```
GET:/users.json?lastName=jones
GET:/user/76234849.json
POST:/user
DELETE:/user/76219057
```

With a relatively simple set of regular expressions can write a controller that support each of these requests.

```java
import static com.kolich.curacao.annotations.methods.RequestMapping.RequestMethod.*;

@Controller
public final class SampleController {

  private final DataSource ds_;

  @Injectable
  public SampleController(final DataSource ds) {
    ds_ = ds;
  }

  // GET:/users.json?lastName=jones
  // Note the @Query annotation on the 'lastName' argument
  @RequestMapping(value="^\\/users\\.json$", methods={GET})
  public final List<User> getUsers(@Query("lastName") final String lastName) {
    return ds_.getUsersWithLastName(lastName);
  }
  
  // GET:/user/76234849.json
  // Note the @Path annotation on the 'userId' argument
  @RequestMapping(value="^\\/user\\/(?<userId>\\d+)\\.json$", methods={GET})
  public final User getUser(@Path("userId") final String userId) {
    return ds_.getUserById(userId);
  }
  
  // POST:/user
  @RequestMapping(value="^\\/user$", methods={POST})
  public final User createUser(@RequestBody final String createUserRequest) {
    return ds_.createUser(createUserRequest);
  }

  // DELETE:/user/76219057
  @RequestMapping(value="^\\/user\\/(?<userId>\\d+)$", methods={DELETE})
  public final User deleteUser(@Path("userId") final String userId) {
    return ds_.getAndDeleteUser(userId);
  }

}
```

Nothing too surprising here, but lets walk through it anyways.

This `@Controller` declares a dependency on `DataSource` through its constructor &mdash; the immutable singleton `DataSource` will be injected automatically into the constructor when Curacao instantiates an instance of this controller on application startup.

Subsequent methods like `getUsers` and `getUser` are only invoked on incoming `GET` requests whose path matches the regular expression provided in the `value` attribute of the `@RequestMapping` annotation.
 
The `@Query` controller argument annotation is used to extract the values of query parameters, if any.  If `@Query` references a query parameter that is not present on the request, the argument value will be `null`.  Likewise, the `@Path` controller argument annotation is used to extract values from the path, if any.  If `@Path` references a named capture group that is not present in the path, or the provided regular expression was unable to extract a value for the given capture group, the argument value will be `null`.
   
Beautifully simple &mdash; no awful DSL's to learn, and completely interoperable with other languages for the JVM like Scala and Clojure.

#### Custom Header

In the odd event that you'd like to route requests based on something other than the path, Curacao supports the implementation of a custom [CuracaoPathMatcher](https://github.com/markkolich/curacao/blob/master/curacao/src/main/java/com/kolich/curacao/handlers/requests/matchers/CuracaoPathMatcher.java) to be used within your `@RequestMapping` annotations.  For instance, consider a controller that routes requests based on a custom value within an HTTP request header &mdash; it's easy to implement a custom `CuracaoPathMatcher` to achieve this behavior.

```java
import com.google.common.collect.ImmutableMap;

public final class MyCustomHeaderMatcher implements CuracaoPathMatcher {

  private static final String MY_CUSTOM_HEADER = "X-Custom-Header";

  @Override @Nullable
  public Map<String,String> match(final HttpServletRequest request,
                                  final String value, // From your @RequestMapping
                                  final String path) throws Exception {
    final String header = request.getHeader(MY_CUSTOM_HEADER);
    if (header != null && header.contains(value)) {
      // If the custom contains the provided value from the annotation,
      // then we have a match!  Note the value argument here is the
      // "value" from the controller method @RequestMapping annotation.
      // For example:
      // @RequestMapping("foo") the value is "foo"
      // @RequestMapping(value="bar", methods=POST) the value is "bar"
      return ImmutableMap.of(MY_CUSTOM_HEADER, value);
    } else {
      return null; // No match!
    }
  }

}
```

And simply reference your custom `CuracaoPathMatcher` in your controllers using the `matcher` attribute on Curacao's `@RequestMapping` annoation.

```java
@Controller
public final class SampleController {

  @RequestMapping(value="foo", matcher=MyCustomHeaderMatcher.class)
  public final String foo() {
    // Will only be invoked when an 'X-Custom-Header' request header is
    // present that contains "foo".
    return "foo";
  }
  
  @RequestMapping(value="bar", matcher=MyCustomHeaderMatcher.class)
  public final String bar() {
    // Will only be invoked when an 'X-Custom-Header' request header is
    // present that contains "bar".
    return "bar";
  }

}
```

In the example above, the `foo` method will only be invoked when the `X-Custom-Header` HTTP request header contains the string "foo".  Likewise, the `bar` method will only be invoked when the `X-Custom-Header` contains the string "bar".

You can, of course, implement your own logic to "pull apart" a custom header value and route requests as desired using any custom `CuracaoPathMatcher` implementation.  But, always remember that the first "matcher" to return a non-null map indicating a match, wins.  In other words, if you have two custom `CuracaoPathMatcher` implementations that could potentially match the same "value", the first matcher that matches will win &mdash; the ordering in which matchers are interrogated to find a controller method to invoke is nondeterministic.  This is by design.

### Part 3

In the upcoming Part 3 of this series, I'll cover the creation and serving of versioned response objects using Curacao.
  
Stay thirsty, my friends.

<!--- tags: curacao, java, servlet -->