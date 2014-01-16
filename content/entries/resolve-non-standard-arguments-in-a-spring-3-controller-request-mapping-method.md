Spring 3 is great at automatically resolving standard arguments into a controller request method.

For example, a primitive Spring controller might look like this ...

```java
@Controller
@RequestMapping(value="/somepath")
public final class MyController {

  @RequestMapping(method={RequestMethod.GET, RequestMethod.HEAD})
  public ModelAndView someMethod(final HttpServletRequest request,
    final Principal principal) {
    // Extract some special object needed to process the request from
    // the session -- this object is bound to the session elsewhere on
    // a successful authentication.
    final MyObject obj = (MyObject)request.getSession().getAttribute("myobjkey");
    // Do actual work.
    /* ... */
    return new ModelAndView("someview");
  }

}
```

In this case, Spring knows the [HttpServletRequest](http://download.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html) argument represents the incoming Servlet request, and the [Principal](http://download.oracle.com/javase/6/docs/api/java/security/Principal.html) argument is the object representing the authenticated user (in the event that you're using Spring Security to manage authentication in your web-application).  On method invocation, Spring automatically resolves these arguments for you.  Neat!

However, the repetition becomes obvious where in every controller, you need to fetch the same MyObject from the session, over and over again.  Instead of repeating that line of code in every method of every controller that needs access to MyObject, what if you could tell Spring how to resolve MyObject automatically on invocation?

Let's say you've defined a custom object and bound it to the session on a successful authentication (either on your own or via Spring Security) ...

```java
import java.util.UUID;

public final class MyObject {

  // A unique and static identifier.
  private final UUID id_;

  public MyObject() {
    id_ = UUID.randomUUID();
  }

  public UUID getId() {
    return id_;
  }

}
```

Good news!  Spring can automatically resolve an argument of type MyObject if used as an argument into a controller request method.

### Meet WebArgumentResolver

The [WebArgumentResolver](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/bind/support/WebArgumentResolver.html) interface let's you define a bean that tells Spring where to find a custom argument of any type when used in a controller request method.  Here's an example that tells Spring where to find an argument of type MyObject bound to the session ...

```java
import static javax.servlet.jsp.PageContext.SESSION_SCOPE;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

public class SessionExtractingWebArgumentResolver implements WebArgumentResolver {

  @Override
  public Object resolveArgument(final MethodParameter mp,
    final NativeWebRequest nwr) throws Exception {
    Object argument = UNRESOLVED;
    if(mp.getParameterType().equals(MyObject.class)) {
      // Assumes that a MyObject is bound to the session elsewhere using
      // attribute key "myobjkey" on a successful authentication.
      if((argument = nwr.getAttribute("myobjkey", SESSION_SCOPE)) == null) {
        throw new Exception("Fail, no MyObject bound to session!");
      }
    }
    return argument;
  }

}
```

Now that we've defined our [WebArgumentResolver](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/bind/support/WebArgumentResolver.html) bean, we can wire it into in our Spring MVC configuration.

### Wire it Into Spring MVC

Wire your custom [WebArgumentResolver](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/bind/support/WebArgumentResolver.html) into Spring's [AnnotationHandlerMethodAdapter](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/servlet/mvc/annotation/AnnotationMethodHandlerAdapter.html) using a quick declaration in your Spring MVC XML configuration.  Here's an example ...

```xml
<bean class="org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter">
  <property name="customArgumentResolver">
    <bean class="your.package.SessionExtractingWebArgumentResolver" />
  </property>
</bean>
```

Yay!  Now, if Spring encounters a MyObject argument in a controller request method, it will look it up using our custom [WebArgumentResolver](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/bind/support/WebArgumentResolver.html) and extract it from the session accordingly.

### An Improved Controller

Now that Spring can resolve MyObject automatically, we can use it as an argument into any controller request method ...

```java
@RequestMapping(method={RequestMethod.GET, RequestMethod.HEAD})
public ModelAndView betterMethod(final MyObject my) {
  /* ... */
}
```

Great!  No more ugly repetition, and less code.  That's a win-win, baby.

Enjoy.
