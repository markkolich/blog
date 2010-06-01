While working on a new personal project, I decided to pick up and dig into [Spring 3 MVC](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/htmlsingle/spring-framework-reference.html) and [Spring Security](http://projects.spring.io/spring-security/).  I've touched both of these technologies here and there in a number of other projects, but this new opportunity has really opened the door for a deep dive into Spring.

I setup a few Spring 3 controllers, and integrated Spring Security into my web-app.  All went great and so I added a simple form-based login to my Spring Security XML configuration.

### Problem: Overriding UsernamePasswordAuthenticationFilter

When setting up a form-based login via a default Spring Security `<http:security>` configuration, Spring auto generates and configures a [UsernamePasswordAuthenticationFilter](http://static.springsource.org/spring-security/site/docs/3.0.x/apidocs/org/springframework/security/web/authentication/UsernamePasswordAuthenticationFilter.html) bean.  This filter, by default, responds to the URL `/j_spring_security_check` when processing a login `POST` from your web-form.  First, I want to override Spring Security's default login process URL to `/login` instead of `/j_spring_security_check`.  Second, I've configured a Spring 3 controller to display my login web-form when a user visits `/login`.

That said, here's the underlying problem with Spring Security's default `UsernamePasswordAuthenticationFilter`: I want it to accept and process `POST`'s to `/login`, but a `GET` or any HTTP method to `/login` should be forwarded to the next filter in the chain.  Not surprisingly, you cannot do this with Spring Security's default `UsernamePasswordAuthenticationFilter` because it does not `@Override` the `doFilter()` method of `AbstractAuthenticationProcessingFilter`.  In short, there's no way to get and check the incoming HTTP request method and re-route it using the default `UsernamePasswordAuthenticationFilter`.

### Solution: Write your own Spring Security Authentication Filter

If you want a Spring controller to process `GET` requests to `/login`, but Spring Security to intercept and process a `POST` to `/login`, then you'll need to write your own Spring Security authentication filter.  Here's the idea:

```java
public class MyFilter extends AbstractAuthenticationProcessingFilter {

  private static final String DEFAULT_FILTER_PROCESSES_URL = "/login";
  private static final String POST = "POST";

  public MyFilter () {
    super(DEFAULT_FILTER_PROCESSES_URL);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
    HttpServletResponse response) throws AuthenticationException,
    IOException, ServletException {
    // You'll need to fill in the gaps here.  See the source of
    // UsernamePasswordAuthenticationFilter for a working implementation
    // you can leverage.
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res,
    FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;
    if(request.getMethod().equals(POST)) {
      // If the incoming request is a POST, then we send it up
      // to the AbstractAuthenticationProcessingFilter.
      super.doFilter(request, response, chain);
    } else {
      // If it's a GET, we ignore this request and send it
      // to the next filter in the chain.  In this case, that
      // pretty much means the request will hit the /login
      // controller which will process the request to show the
      // login page.
      chain.doFilter(request, response);
    }
  }

}
```

Note the good stuff inside of `doFilter()`.  If the incoming request method is a `POST`, then we send it up to our `AbstractAuthenticationProcessingFilter` to actually process the login.  If it's a `GET`, or any other HTTP request method for that matter, we simply send it to the next filter in the chain.

Finally, remember that you'll need to define your own `FORM_LOGIN_FILTER` inside of your `<security:http>` Spring Security XML configuration to override the default `/j_spring_security_check` URL:

```xml
<security:http auto-config="false" use-expressions="true"
  entry-point-ref="LoginUrlAuthenticationEntryPoint">
  <security:custom-filter position="FORM_LOGIN_FILTER" ref="MyFilter" />
</security:http>

<bean id="LoginUrlAuthenticationEntryPoint"
  class="org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint">
  <property name="loginFormUrl" value="/login" />
</bean>
```

Enjoy!