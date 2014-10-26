[Curacao is a beautifully simple toolkit](introducing-curacao) for the JVM that lets you write highly concurrent services on the common and trusted [J2EE stack](https://jcp.org/aboutJava/communityprocess/final/jsr315/).  While you can use it to build a full web-application, Curacao is fundamentally designed to support highly asynchronous REST/HTTP-based integration layers on top of asynchronous Servlets that are easy to understand, maintain, and debug.  At its core, Curacao completely avoids the mental overhead of passing of messages between actors or within awful event loops &mdash; yet, given its simplicity, performs very well in even the most demanding applications.

Quite often, one of the most difficult problems to solve when designing an API is resource versioning.

As I see it, there's several aspects to the API versioning problem:

1. how do clients specify the version of the resource they're asking for?
2. how does the API route version specific requests?
3. how does the API manage and respond with versioned responses?
4. how does the API gracefully sunset deprecated resource versions?

Solutions to these questions [have been debated](http://stackoverflow.com/questions/389169/best-practices-for-api-versioning), [ad nauseam](http://stackoverflow.com/questions/972226/how-to-version-rest-uris), [into infinity](http://www.lexicalscope.com/blog/2012/03/12/how-are-rest-apis-versioned/) to which everyone has a conflicting opinion.

Opinions aside, **Part 1** of this series highlights a few examples that illustrate how you might implement solutions to these problems with [Curacao](https://github.com/markkolich/curacao).

### Part 1: Versioned Requests

How do clients specify the version of the resource they’re asking for?

#### Query Parameter

One approach, is specifying the desired version of a resource using an *optional* query parameter.  For example, consider the following requests:

```
GET:/user/89171245.json?version=1
GET:/user/89171245.json?version=2
GET:/user/89171245.json
```

While technically asking for the same resource `/user/89171245.json`, the client is using the `version` query parameter to specify the version of the API it intends to use.  The server side can interpret the value of the `version` query parameter, and respond with an entirely unique response object depending on the requested version.  In this case, `version=1` may result in an entirely different response JSON object compared to that of `version=2`.  In the event that the `version` query parameter is omitted, the API will default to the most recent version.

Implementing this versioning mechanism with Curacao is trivial.

The trick is to implement a custom [ControllerMethodArgumentMapper](https://github.com/markkolich/curacao/blob/master/curacao/src/main/java/com/kolich/curacao/handlers/requests/mappers/ControllerMethodArgumentMapper.java) that looks for the `version` query parameter, sanitizes it, and passes the requested version to your controller methods as a typed argument.

First, lets define an enumeration that cleanly represents all possible supported API versions.

```java
public static enum MyApiVersion {

  /* API version 1 */
  VERSION_1("1"),
  
  /* API version 2 */
  VERSION_2("2");
  
  private String version_;
  private MyApiVersion(final String version) {
    version_ = version;
  }
  
  /**
   * Given a string, from a query parameter, convert it into one of the
   * supported API versions.  If the param is null, or doesn't match any
   * known version, this method returns the latest version.
   */
  public static final MyApiVersion versionFromParam(final String param) {
    MyApiVersion result = MyApiVersion.VERSION_2; // Default
    if (param != null) {
      // Iterate over each possible version in the enumeration,
      // looking for a match.
      for (final MyApiVersion version : MyApiVersion.values()) {
        if (version.version_.equals(param)) {
          result = version;
          break;
        }
      }
    }
    return result;
  }
  
}
```

Now, let's implement a custom `ControllerMethodArgumentMapper` that converts the `version` query parameter on the request, if any, into a `MyApiVersion`.

```java
import com.kolich.curacao.handlers.requests.mappers.ControllerMethodArgumentMapper;

@ControllerArgumentTypeMapper(MyApiVersion.class)
public final class MyApiVersionArgumentMapper extends ControllerMethodArgumentMapper<MyApiVersion> {

  private static final String VERSION_QUERY_PARAM = "version";

  @Nullable @Override
  public final MyApiVersion resolve(@Nullable final Annotation annotation,
                                    final CuracaoRequestContext context) throws Exception {
    final HttpServletRequest request = context.request_;
    final String versionParam = request.getParameter(VERSION_QUERY_PARAM);
    return MyApiVersion.versionFromParam(versionParam);
  }

}
```

And finally, we can now write our controller methods to take an argument of type `MyApiVersion`.  At runtime, Curacao will see this `MyApiVersion` argument on your controller methods, and invoke our custom `MyApiVersionArgumentMapper` to extract the desired version from the request.

```java
import static com.kolich.curacao.annotations.methods.RequestMapping.RequestMethod.*;

@Controller
public final class VersionedController {

  private final DataSource ds_;

  @Injectable
  public VersionedController(final DataSource ds) {
    ds_ = ds;
  }

  @RequestMapping(value="^\\/user\\/(?<userId>\\d+)\\.json$", methods={GET})
  public final String getUser(@Path("userId") final String userId,
                              final MyApiVersion version) {
    final User user = ds_.getUserById(userId);
    if (MyApiVersion.VERSION_1.equals(version)) {
      // Construct and return a "version 1" User object.
    } else {
      // Construct and return a "version 2" User object.
    }
  }
  
}
```

Note that the `MyApiVersion` argument in the controller method above is automatically discovered and injected when invoked by Curacao.

#### Path

A more common approach to request versioning is through the usage of a version identifier in the path itself.    For example, consider the following requests:

```
GET:/v1/user/98143016.json
GET:/v2/user/98143016.json
```

Note the `v1` and `v2` version identifier in the path.

Again, unsurprisingly, implementing this versioning mechanism with Curacao is trivial.  Current best practices dictate the usage of multiple controllers &mdash; one that handles `v1` requests and another that handles `v2`.

And so, one controller for `v1`:

```java
package com.foo.api.controllers.v1;

@Controller
public final class ControllerV1 {

  @RequestMapping(value="^\\/v1\\/user\\/(?<userId>\\d+)\\.json$", methods={GET})
  public final String fooV1(@Path("userId") final String userId) {
    return "v1: " + userId;
  }

}
```

And another for `v2`:

```java
package com.foo.api.controllers.v2;

@Controller
public final class ControllerV2 {

  @RequestMapping(value="^\\/v2\\/user\\/(?<userId>\\d+)\\.json$", methods={GET})
  public final String fooV2(@Path("userId") final String userId) {
    return "v2: " + userId;
  }

}
```

Note clean separation using a unique `package` declaration.

#### `Accept` Header

Another, slightly more RESTful approach, is using the `Accept` HTTP request header to identify the desired version of a resource.  This is somewhat analogous to client/server "content negotiation".

In the interest of brevity, I won't write a complete implementation here.  However, a key takeaway is that you can use Curacao's `@Header` annotation to extract the value of any request header.  From there, your business logic in the controller can examine the header value to make a decision about what API version is invoked.
  
```java
@Controller
public final class HeaderController {

  @RequestMapping(value="^\\/foo", methods={GET})
  public final String headerDemo(@Header("Accept") final String accept) {
    if (accept.contains("v2")) {
      // V2
    } else {
      // V1
    }
  }  

}
```

In addition to `@Header`, there are a number of "convenience" request header annotations you can use to decorate your controller method arguments:

* `@Accept` &mdash; convenience for the `Accept` request header
* `@UserAgent` &mdash; convenience for the `User-Agent` request header
* `@ContentType` &mdash; convenience for the `Content-Type` request header
* `@Authorization` &mdash; convenience for the `Authorization` request header
* ... and of course, many more in the [com.kolich.curacao.annotations.parameters.convenience](https://github.com/markkolich/curacao/tree/master/curacao/src/main/java/com/kolich/curacao/annotations/parameters/convenience) package.

### Part 2

Next in this series, [Writing Versioned Service APIs With Curacao: Part 2](writing-versioned-service-apis-with-curacao-part2) discusses routing strategies with Curacao.

Enjoy!