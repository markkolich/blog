Tired of [Spring](http://spring.io), [raw Servlets](https://jcp.org/aboutJava/communityprocess/final/jsr315/), and other [REST toolkits](http://spray.io) I cautiously approached the thought of building my own JVM web-layer from scratch.  In retrospect, I probably didn't need to spend time on yet another toolkit to help shield engineers from the boilerplate and complexity of web-applications on the JVM.  However, I found most existing libraries (and frameworks) to be overly bloated, complex and just generally awful.

I wanted something "better" &mdash; of course, better purely by my own personal definition.

I've written enough Java and Scala to recognize what's most relevant when choosing a highly asynchronous and flexible web-layer upon which to build a scalable web-service or application.  With a foot in multiple camps, and having previously used most widely available frameworks and tools, I'd like to think I have a unique perspective on this problem.  From what I can tell, there's generally two sides:

1. The **asynchronous overkill** approach &mdash; [Akka](http://akka.io/), [Spray](http://spray.io) and [Play](http://www.playframework.com/)
2. The **boil-the-ocean, thread-based** approach &mdash; [Spring](http://spring.io) and [Apache Struts](http://struts.apache.org/)

Each of these tools have their own merits, but I conjecture that a large majority of the time, they are either misused or chosen for the wrong reasons.  Quite often, especially in software engineering, developers get lost in a haze of early over optimization or analysis paralysis.  I wish I had a dollar for every time I heard a Product or Engineering Manager say something like, "We need to support 1,000,000 concurrent users! Web-scale!"  Hold on &mdash; let's make a pragmatic upfront technical decision, and build a beautiful product first.  If and when the opportunity to go "web-scale" presents itself, we can address those tough scalability questions later.

However, in the meantime, there must exist some web-layer that:

* doesn't attempt to boil-the-ocean
* can be used with a Java or Scala stack
* is easy to understand and debug
* avoids confusing and generally awful [DSLs](http://en.wikipedia.org/wiki/Domain-specific_language)
* is fully asynchronous
* doesn't require tens-of-megabytes of dependency hell
* doesn't "baby" engineers with fancy shells and command line tools
* has a reasonable set of *complete* documentation and examples
* is "fast"

And so, I sat down one evening many moons ago, and began to write my own web-layer from scratch &mdash; one that attempts to address many, if not all, of the shortcomings I perceive in existing toolkits.

I named the project [Curacao](https://github.com/markkolich/curacao), because I like fancy blue drinks with tiny umbrellas.

### 10,000 foot view

At a high level, here are some things you should know about Curacao:

* it's written in Java 7, but plays nicely with Scala
* it's thread based, built on top of asynchronous Servlets as part of the [J2EE Servlet 3.0 spec](https://jcp.org/aboutJava/communityprocess/final/jsr315/)
* takes a "return or throw anything, from anywhere" approach to response handling
* implements a clean, and very fast, dependency injection model
* controllers, components, and routes are defined using simple annotations 
* BYO (bring-your-own) ORM library
* no XML, anywhere &mdash; is configurable using [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md) and the [Typesafe Config](https://github.com/typesafehub/config) configuration library
* for JSON, supports GSON and Jackson out-of-the-box
* compiled, Curacao ships in a single JAR that's only 150KB in size
* deployable with any Servlet 3.0 compatible web-application
* it's free and open source on [GitHub](https://github.com/markkolich/curacao)

### Bootstrap

Still here?

Let's bootstrap a Curacao application in 3-steps.

<ol>
  <li>
<p>First, configure your project to pull in the necessary dependencies.  As of this writing, the latest stable version is 2.6.2, however you should check the <a href="https://github.com/markkolich/curacao/releases">Curacao Releases page</a> for the latest version.</p>
<p>If using Maven:</p>
<p><pre class="prettyprint">
<code class="xml">&lt;repository&gt;
  &lt;id&gt;Kolichrepo&lt;/id&gt;
  &lt;name&gt;Kolich repo&lt;/name&gt;
  &lt;url&gt;http://markkolich.github.io/repo/&lt;/url&gt;
  &lt;layout&gt;default&lt;/layout&gt;
&lt;/repository&gt;

&lt;dependency&gt;
  &lt;groupId&gt;com.kolich.curacao&lt;/groupId&gt;
  &lt;artifactId&gt;curacao&lt;/artifactId&gt;
  &lt;version&gt;2.6.2&lt;/version&gt;
  &lt;scope&gt;compile&lt;/scope&gt;
&lt;/dependency&gt;</code>
</pre></p>
<p>If using SBT:</p>
<p><pre class="prettyprint">
<code class="scala">resolvers += "Kolich repo" at "http://markkolich.github.io/repo"
val curacao = "com.kolich.curacao" % "curacao" % "2.6.2" % "compile"</code>
</pre></p>
</li>

<li>
<p>Second, inject the required listener and dispatcher into your application's <code>web.xml</code>.</p>
<p><pre class="prettyprint">
<code class="xml">&lt;web-app&gt;
                 
  &lt;listener&gt;
    &lt;listener-class&gt;com.kolich.curacao.CuracaoContextListener&lt;/listener-class&gt;
  &lt;/listener&gt;

  &lt;servlet&gt;
    &lt;servlet-name&gt;CuracaoDispatcherServlet&lt;/servlet-name&gt;
    &lt;servlet-class&gt;com.kolich.curacao.CuracaoDispatcherServlet&lt;/servlet-class&gt;
    &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
    &lt;async-supported&gt;true&lt;/async-supported&gt;
  &lt;/servlet&gt;
  &lt;servlet-mapping&gt;
    &lt;servlet-name&gt;CuracaoDispatcherServlet&lt;/servlet-name&gt;
    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
  &lt;/servlet-mapping&gt;

&lt;/web-app&gt;</code>
</pre></p>
<p>The <code>CuracaoContextListener</code> listens for <code>ServletContext</code> lifecycle events, and initializes and destroys application components accordingly.  And, like you might expect, the <code>CuracaoDispatcherServlet</code> is responsible for receiving and dispatching incoming requests from the Servlet container.</p>
</li>
<li>
<p>Lastly, create a <a href="https://github.com/typesafehub/config">HOCON</a> configuration file named <code>application.conf</code> and put it in a place that's accessible on your classpath &mdash; typically somewhere like <code>src/main/resources</code>.  This file defines your Curacao application configuration, and is loaded from the classpath at runtime.
<p><pre class="prettyprint">
<code class="plain">curacao {
                    
  ## Your boot package is the package in which all of your components and
  ## controllers reside.  At boot time, Curacao uses reflection and scans
  ## this package, and all of its children, looking for annotated classes
  ## to dynamically instantiate.
  boot-package = "com.foobar"
  
  ## The asynchronous timeout for any response.  If your application fails to
  ## respond to any request within this timeout, Curacao will kick in and
  ## throw an exception, which allows you to abort+handle the response
  ## gracefully.  Set to 0 (zero) for an infinite timeout.
  async-context-timeout = 30s
  
  ## The maximum number of threads that will be used to handle incoming
  ## requests.  The number of concurrent request worker threads will never
  ## exceed this size.  Set to 0 (zero) for an unbounded thread pool.
  pools.request {
    size = 4
  }
  
  ## The maximum number of threads that will be used to process outgoing
  ## responses.  The number of concurrent response worker threads will never
  ## exceed this size.  Set to 0 (zero) for an unbounded thread pool. 
  pools.response {
    size = 4
  }
  
}</code>
</pre></p>
<p>Take a look at Curacao's global <a href="https://github.com/markkolich/curacao/blob/master/curacao/src/main/resources/reference.conf">reference.conf</a> for the complete list of application configuration options.  This <code>reference.conf</code> file defines the Curacao default set of configuration options, which are completely overridable in your own <code>application.conf</code>.</p>
</li>
</ol>

That's it!  You've bootstrapped your first Curacao enabled application.

### Controllers

At their core, Curacao controllers are immutable singletons that are automatically instantiated at application startup &mdash; they're classes that contain methods which Curacao will invoke via reflection when dispatching a request.  On launch, Curacao recursively scans your defined `boot-package` looking for any classes annotated with the `@Controller` annotation.  As requests are received and dispatched from the Servlet container, Curacao very efficiently interrogates each known controller instance looking for a method worthy of handling the request.

For maximum efficiency at runtime, regular expressions and request routing tables are compiled and cached **once** at startup.

Here's a sample controller implementation that demonstrates several key features:

```java
@Controller
public final class UserController {

  @RequestMapping("^\\/users\\/(?<userId>[a-zA-Z_0-9\-]+)$")
  public String getUser(@Path("userId") final String userId) {
    return "Load user: " + userId;
  }
  
  @RequestMapping(value="^\\/users$", methods=POST)
  public String createUser(@RequestBody final String body) {
    // Lazily convert 'body' to a user object
    // Insert user into data store
    return "Successfully created user.";
  }
  
  @RequestMapping(value="^\\/users\\/(?<userId>[a-zA-Z_0-9\-]+)$", methods=PUT)
  public void updateUser(@Path("userId") final String userId,
                         final HttpServletResponse response,
                         final AsyncContext context) {
    try {
      // Do work, update user with id 'userId'
      response.setStatus(201); // 201 Created
    } finally {
      // Complete context manually due to 'void' return type
      context.complete();
    }    
  }
  
  @RequestMapping(value="^\\/users\\/(?<userId>[a-zA-Z_0-9\-]+)$", methods=DELETE)
  public void deleteUser(@Path("userId") final String userId,
                         final HttpServletResponse response,
                         final AsyncContext context) {
    try {
      // Delete user specified by 'userId'
      response.setStatus(204); // 204 No Content
    } finally {
      // Complete context manually due to 'void' return type
      context.complete();
    }    
  }
  
  @RequestMapping("^\\/users$")
  public Future<List<String>> queryUsers(@Query("name") final String name) {
    // Query data store for a list of users matching the provided 'name'.
    // Return a Future<List<String>> which represents an async operation that
    // fetches a list of user ID's.
    return someFuture;
  }

}
```

Like other popular toolkits, request routing is handled using a familiar `@RequestMapping` method annotation.  The `@RequestMapping` annotation allows you to specify, among other things, the HTTP request method and URI path to match.  The default behavior of `@RequestMapping` uses Java regular expressions and [Java 7's named capture groups](https://blogs.oracle.com/xuemingshen/entry/named_capturing_group_in_jdk7) to extract path components from the incoming request URI.

When you need the entire request body as a `UTF-8` encoded `String`, simply add a `String` method argument and annotate it with the `@RequestBody` annotation.  Further, query parameters can be easily extracted using the `@Query` method argument annotation.  For more complex scenarios, when you need direct access to the underlying `HttpServletResponse` or Servlet 3.0 `AsyncContext` object, just add them as arguments and Curacao will pass them to your method when invoked.  Last but not least, your controller methods may return a `Future<?>` anytime you need to render the result of an asynchronous operation, that may or may not complete successfully at some point in the future.
 
In the unlikely event you need to route requests by something other than the URI/path, you can implement your own `CuracaoPathMatcher` and pass it to Curacao using the `matcher` attribute of the `@RequestMapping` annotation.

### Components

Like Curacao controllers, components are immutable singletons instantiated at application startup &mdash; they're classes that represent pieces of shared logic or configuration, much like Java "beans".  Unsurprisingly, component classes are annotated with the `@Component` annotation.

Component singletons can be passed to other components, controllers, request filters, request mappers and response handlers &mdash; we'll cover the latter three later in this post.  In the spirit of immutability, Curacao components can only be passed to other Curacao instantiated classes via their constructors &mdash; there are no "getters" and "setters".  Current best practices dictate the usage of `final` like instance variables in your Curacao instantiated classes, ensuring immutability.

Consider the two components below, `Foo` and `Bar` &mdash; based on their constructor declarations, `Bar` depends on `Foo`.  In other words, `Bar` cannot be instantiated unless it is passed an instance of `Foo` via its constructor.

```java
@Component
public final class Foo {

  public Foo() {
    // Stuph.
  }

}
```

```java
@Component
public final class Bar {

  private final Foo foo_;

  @Injectable
  public Bar(@Nonnull final Foo foo) {
    foo_ = foo;
  }

}
```

You may have noticed the `@Injectable` constructor annotation.  The `@Injectable` annotation is used to declare dependencies on other components.  In the example above, because class `Bar` has an `@Injectable` annotated constructor with an argument of type `Foo`, Curacao interprets this relationship as "class `Bar` depends on `Foo`".  Therefore, `Foo` will be instantiated first, and then passed to `Bar`'s constructor.

Curacao automagically identifies such dependencies, and instantiates component singletons in dependency order.  Like other dependency-injection (DI) models, Curacao scans your declared `boot-package` and intelligently builds an object graph by analyzing dependencies derived from your implementation.  However, note there are no "component factories" in Curacao.

Your object graphs can be as simple, or as complex as you'd like.

Injecting components into your controllers is easy too.  In your controller, simply add an `@Injectable` annotated constructor.  Component singletons, once instantiated, will be passed to your controller as constructor arguments.

```java
@Controller
public final class SampleController {

  private final Bar bar_;

  @Injectable
  public SampleController(final Bar bar) {
    bar_ = bar;
  }
  
  @RequestMapping("^\\/bar$")
  public String bar() {
    return bar_.toString();
  }

}
```

Lastly, components that need to be aware of [application container lifecycle events such as startup and shutdown](http://docs.oracle.com/javaee/7/tutorial/doc/servlets002.htm), can implement the `ComponentInitializable` and/or `ComponentDestroyable` interfaces.

```java
@Component
public final class WebServiceClient implements ComponentDestroyable {

  private final AsyncHttpClient httpClient_;

  public WebServiceClient() {
    httpClient_ = new AsyncHttpClient();
  }
  
  /**
   * Called once during application shutdown to stop this component.
   * Is useful to cleanup or close open sockets and other resources.
   */
  @Override
  public void destroy() throws Exception {
    httpClient_.close();
  }

}
```

```java
@Component
public final class DataStore implements ComponentInitializable {

  private final MongoDbClient mongo_;

  public DataStore() {
    mongo_ = new MongoDbClient();
  }
  
  /**
   * Called once during application startup to initialize this component.
   * Is useful to further initialize a component beyond its constructor.
   */
  @Override
  public void initialize() throws Exception {
    mongo_.setCredentials("foo", "bar");
    mongo_.setMaxConnections(100);
  }

}
```

### Filters

Request filters are singletons that implement the `CuracaoRequestFilter` interface and are invoked as "pre-processing" tasks before an underlying controller method is invoked.  Filters can accept the request, and attach context attributes for consumption by a controller.  Or, they can reject the request by throwing an `Exception`.

This makes request filters a suitable place for handling request authentication or authorization.

Unlike vanilla Servlet filters, Curacao request filters are handled asynchronously outside of a blocking Servlet container thread.  In other words, Curacao calls `request.startAsync()` on the incoming `ServletRequest` before it invokes your request filter.  This means that Curacao request filters are asynchronously handled in the context of a normal request. 

Just like other components or controllers, filters are injectable &mdash; decorate a filter's constructor with `@Injectable` to inject component singletons into the filter. 

```java
public final class SessionFilter implements CuracaoRequestFilter {

  private final DataStore ds_;

  @Injectable
  public SessionFilter(final DataStore ds) {
    ds_ = ds;
  }
  
  @Override
  public void filter(final CuracaoRequestContext context) throws Exception {
    final HttpServletRequest request = context.request_;
    final String auth = request.getHeader("Authentication");
    // Authenticate the request against the data store, throw Exception if needed 
    final String userId = ds_.authorizeUser(auth);
    // If we got here, we must have successfully authenticated the user.
    // Attach the user's ID to the context to be picked up by a controller later.
    context.setProperty("user-id", userId);
  }

}
```

The `CuracaoRequestContext` is an object that represents a mutable "request context" which spans across the life of the request.  A filter can use the internal mutable property map in this class to pass data objects from itself to another filter, controller, or argument mapper (covered later).

Attach one or more filters to your controller methods using the `filters` attribute of the `@RequestMapping` annotation.

```java
@Controller
public final class SecureController {

  @RequestMapping(value="^\\/secure$", filters={SessionFilter.class})
  public String secureArea() {
    // Secure.
  }

}
```

### Request Mappers

Request mappers are immutable singletons that translate the request body, or some other piece of the request, into something directly usable by a controller method.  For example, reading and translating an incoming form `POST` body into a `Multimap<String,String>`.  Or, reading and translating an incoming `PUT` request body into a custom object &mdash; e.g., unmarshalling a JSON string into an application entity.

For convenience, Curacao ships with several default request mappers.  For instance, in your controller, if you'd like to convert the incoming request body to a `Multimap<String,String>`, simply add the right argument and annotate it with the `@RequestBody` annotation.  Curacao uses [Google's Guava Multimap implementation](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/Multimap.html) exclusively.
 
```java
@Controller
public final class RequestBodyDemoController {

  /**
   * Buffer the request body, and decode the URL encoded key-value parameters
   * therein into a Multimap<String,String>.
   */
  @RequestMapping(value="^\\/post", methods=POST)
  public String post(@RequestBody final Multimap<String,String> body) {
    // Assume POST body was 'foo=bar&dog=cat', body.get("foo") returns ["bar"]
    List<String> foo = body.get("foo");
    return foo.toString();
  }
  
  /**
   * Get a single parameter from the POST body, 'foo'.
   */
  @RequestMapping(value="^\\/post\\/foo", methods=POST)
  public String postFoo(@RequestBody("foo") final String foo) {
    return foo;
  }
  
  /**
   * Buffer the entire request body into an NIO ByteBuffer.
   */
  @RequestMapping(value="^\\/put\\/buffer", methods=PUT)
  public String postBuffer(@RequestBody final ByteBuffer body) {
    return "Byte buffer capacity: " + body.capacity();
  }
  
}
```

Implementing your own request mapper is easy too.  For instance, if you need to unmarshall a JSON `POST` body into an object, simply write a class to `extend InputStreamReaderRequestMapper<T>` and annotate it with the `@ControllerArgumentTypeMapper` annotation.

```java
@ControllerArgumentTypeMapper(MyObject.class)
public final class MyObjectMapper extends InputStreamReaderRequestMapper<MyObject> {

  private final DataStore ds_;

  /**
   * Yes, argument mappers are component injectable too!
   */
  @Injectable
  public MyObjectMapper(final DataStore ds) {
    ds_ = ds;
  }

  @Override
  public MyObject resolveWithReader(final InputStreamReader reader) throws Exception {
    // Use provided 'InputStreamReader' and unmarshall string to a MyObject instance
    return myObject;
  }

}
```

Now that you've registered a request mapper for type `MyObject`, you can simply add a `MyObject` argument to any controller method.  Curacao will automagically invoke your request mapper to convert the body to a `MyObject`, before calling your controller method.
 
```java
@Controller
public final class MyObjectController {

  @RequestMapping(value="^\\/myobject", methods=POST)
  public String myObject(final MyObject mine) {
    // Do something with MyObject
    return "Worked!";
  }

}
```

You can find the [default set of Curacao request mappers here](https://github.com/markkolich/curacao/tree/master/curacao/src/main/java/com/kolich/curacao/handlers/requests/mappers/types).

### Response Handlers

Curacao takes a "return or throw anything, from anywhere" approach to response handling.

Like you might expect, response handlers are designed to convert controller returned objects into a response, or convert thrown exceptions into a response.  Fortunately, Curacao handles `AsyncContext` completion for you, so in most cases there's no need to write verbose code that forcibly calls `context.complete()` in your controllers.

For convenience, Curacao ships with several default response handlers.  For instance, when your controller method returns a `String`, Curacao automatically interprets this return type as a `text/plain; charset=UTF-8` encoded response body and sets the right response headers accordingly.  Similarly, if your controller method returns a `java.io.File` object, Curacao interprets this as a static resource response &mdash; images, CSS, JavaScript, etc.  As such, Curacao will set the right `Content-Type` response header based on the file's extension, and will automatically stream the `File` contents back to the client.
   
Thrown exceptions are handled in the same way.  For example, Curacao's default response handling behavior for any thrown `java.lang.Exception` is to return a vanilla `500 Internal Server Error` with an empty response body.     

These default behaviors make writing controllers surprisingly pleasant and simple.  However, you can of course, override any of these default behaviors by implementing your own `RenderingResponseTypeMapper`.

```java
@ControllerReturnTypeMapper(MyObject.class)
public final class MyObjectResponseHandler extends RenderingResponseTypeMapper<MyObject> {

  private final DataStore ds_;
  
  /**
   * Yes, response handlers are component injectable too!
   */
  @Injectable
  public MyObjectResponseHandler(final DataStore ds) {
    ds_ = ds;
  }
		
  @Override
  public void render(final AsyncContext context,
                     final HttpServletResponse response,
                     @Nonnull final MyObject obj) throws Exception {
    response.setStatus(200);
    response.setContentType("application/json; charset=UTF-8");
    try(final Writer w = response.getWriter()) {
      // Convert 'MyObject' to JSON using the library of your choice.
      w.write(obj.toJson());
    }
  }
	
}
```

Now that a response handler has been defined for type `MyObject`, anytime a controller method returns and object of type `MyObject`, the `MyObjectResponseHandler` above will be called by Curacao to convert it into JSON automatically.

Thrown exceptions are handled in the same way.

```java
@ControllerReturnTypeMapper(AuthenticationException.class)
public final class AuthenticationExceptionResponseHandler
  extends RenderingResponseTypeMapper<AuthenticationException> {
		
  @Override
  public void render(final AsyncContext context,
                     final HttpServletResponse response,
                     @Nonnull final AuthenticationException ex) throws Exception {
    // Redirect the user to the login page.
    response.sendRedirect("/login");
  }
	
}
```

Here's an example controller that makes use of these response handlers.

```java
@Controller
public final class ResponseHandlerDemoController {

  /**
   * This method returns a 'MyObject' instance, which will trigger Curacao
   * to invoke the MyObjectResponseHandler above to render it as JSON.
   */
  @RequestMapping("^\\/myobject")
  public MyObject getMyObject() {
    return new MyObject();
  }

  /**
   * When a controller throws an 'AuthenticationException', Curacao catches this
   * and invokes the 'AuthenticationExceptionResponseHandler' which redirects
   * the user to the login page.
   */
  @RequestMapping("^\\/home")
  public String home() {
    boolean isLoggedIn = false;
    // Validate that user is authenticated and request contains a valid session.
    if (!isLoggedIn) {
      throw new AuthenticationException();
    }
    return "Hello, world!";
  }

}
```

You can find the [default set of Curacao response handlers here](https://github.com/markkolich/curacao/tree/master/curacao/src/main/java/com/kolich/curacao/handlers/responses/mappers/types).

### Performance

Curacao has been proudly submitted to [TechEmpower's Framework Benchmark test suite](https://github.com/TechEmpower/FrameworkBenchmarks/tree/master/frameworks/Java/curacao).

I'm anxiously waiting on results from [Round 10](http://www.techempower.com/benchmarks/) of their tests, which should include Curacao.  When the test results are available, I intend to publish them here.

### Further Examples

In the spirit of "eating my own dog food", [this very blog is built on Curacao and is fully open source on GitHub](https://github.com/markkolich/blog).  If you're looking for more complex component definitions, and realistic request mapping and response handling examples, the application source of this blog will be a great start.  

Additionally, further examples that demonstrate the flexibility of Curacao can be found in the [curacao-examples project on GitHub](https://github.com/markkolich/curacao/tree/master/curacao-examples/src/main/java/com/kolich/curacao/examples).

### Open Source

Curacao is [free on GitHub](https://github.com/markkolich/curacao) and licensed under the popular [MIT License](https://github.com/markkolich/curacao/blob/master/LICENSE).

Issues and pull requests welcome.

Cheers!