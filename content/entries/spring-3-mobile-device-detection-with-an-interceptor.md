The other day on some Java/JSP tutorial web-site I saw the worst example ever for detecting and properly rendering a mobile capable version of a web-site.  Yes, I'm pointing at you Roseindia.  In every JSP of this example, their mobile User-Agent detection involved one big if/else block:

```java
<%

String userAgent = request.getHeader("User-Agent");
if(userAgent.contains("iPhone")) {
  %> Mobile site! <%
} else {
  %> Regular site! <%
}

%>
```

This wins the worst coding example of the year award.  Here's why this is terrible and you should never use this example:

1. Not all requests contain a `User-Agent` header.  In fact, the `User-Agent` header is purely optional.  In the code above, if the request does not contain a `User-Agent` you'll see a nice `NullPointerException` thrown at userAgent.contains() given that the userAgent is null.
2. Not every mobile device is an iPhone.  What about Blackberry, Andriod or Palm clients?  Blindly assuming that every mobile user is on an iPhone, or other similar device, is horrendously ignorant.
3. Many great frameworks exist, like Spring 3 MVC, that allow you to separate your web-application business and display logic.  This in mind, combing both into a single JSP is a bad idea for a number of reasons.  In an ideal world, your mobile device detection would occur in an interceptor that triggers your MVC framework to render one view for mobile devices, and another view for all others.

This is a fairly common requirement: users visiting your site in a standard web-browser see one view, and users on a mobile device (like a Palm) see a "mobile version" of the same view.  So, here's a way to implement better mobile device detection in your web-application using a Spring 3 [HandlerInterceptorAdapter](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/portlet/handler/HandlerInterceptorAdapter.html).

Note that I assume you are familiar with Spring 3 MVC, and have a working Spring 3 application already up and running.

### Configure Spring

First, you'll need to make the necessary adjustments to your Spring MVC configuration which usually involves tweaking your mvc.xml configuration file.  Regardless of where your MVC XML configuration is, you'll be defining an MVC interceptor for your application like so:

```xml
<mvc:interceptors>

  <mvc:interceptor>
    <mvc:mapping path="/somepath**" />
    <mvc:mapping path="/anotherpath**" />
    <bean class="com.kolich.spring.interceptors.MobileInterceptor"
        init-method="init">
        <property name="mobileUserAgents">
          <list value-type="java.lang.String">
            <value>.*(webos|palm|treo).*</value>
            <value>.*(andriod).*</value>
            <value>.*(kindle|pocket|o2|vodaphone|wap|midp|psp).*</value>
            <value>.*(iphone|ipod).*</value>
            <value>.*(blackberry|opera mini).*</value>
          </list>
        </property>
    </bean>
  </mvc:interceptor>

</mvc:interceptors>
```

This bean, which I will discuss next, extends Spring's abstract [HandlerInterceptorAdapter](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/servlet/handler/HandlerInterceptorAdapter.html).  We will build this interceptor so that it's called right before a Spring view is rendered, giving the interceptor a chance to modify the final view name as necessary.  Also, note that this bean defines a List of regular expressions that the interceptor will use to determine if the client is a mobile device.  You can add or remove regular expressions to this List depending on which mobile devices (a.k.a., `User-Agent`'s) you plan to support.

If you are not familiar with Spring interceptors, you might like to [read up on them here](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/htmlsingle/spring-framework-reference.html#mvc-handlermapping-interceptor).

### The MobileInterceptor

Without further ado, here's my MobileInterceptor:

```java
/**
 * Copyright (c) 2010 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.spring.interceptors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class MobileInterceptor extends HandlerInterceptorAdapter {

  /**
   * The name of the mobile view that the viewer is re-directed to
   * in the event that a mobile device is detected.
   */
  private static final String MOBILE_VIEWER_VIEW_NAME = "mobile";

  /**
   * The User-Agent Http header.
   */
  private static final String USER_AGENT_HEADER = "User-Agent";

  private List<String> mobileAgents_;
  private List<Pattern> uaPatterns_;

  public void init() {
    uaPatterns_ = new ArrayList<Pattern>();
    // Pre-compile the user-agent patterns as specified in mvc.xml
    for(final String ua : mobileAgents_) {
      try {
        uaPatterns_.add(Pattern.compile(ua, Pattern.CASE_INSENSITIVE));
      } catch (PatternSyntaxException e) {
        // Ignore the pattern, if it failed to compile
        // for whatever reason.
      }
    }
  }

  @Override
  public void postHandle(HttpServletRequest request,
    HttpServletResponse response, Object handler,
    ModelAndView mav) throws Exception {
    final String userAgent = request.getHeader(USER_AGENT_HEADER);
    if(userAgent != null) {
      // If the User-Agent matches a mobile device, then we set
      // the view name to the mobile view JSP so that a mobile
      // JSP is rendered instead of a normal view.
      if(isMobile(userAgent)) {
        final String view = mav.getViewName();
        // If the incoming view was "homepage" then this interceptor
        // changes the view name to "homepage-mobile" which, depending
        // on your Spring configuration would probably resolve to
        // "homepage-mobile.jsp" to render a mobile version of your
        // site.
        mav.setViewName(view + "-" + MOBILE_VIEWER_VIEW_NAME);
      }
    }
  }

  /**
   * Returns true of the given User-Agent string matches a suspected
   * mobile device.
   * @param agent
   * @return
   */
  private final boolean isMobile(final String agent) {
    boolean mobile = false;
    for(final Pattern p : uaPatterns_) {
      final Matcher m = p.matcher(agent);
      if(m.find()) {
        mobile = true;
        break;
      }
    }
    return mobile;
  }

  public void setMobileUserAgents(List<String> agents) {
    mobileAgents_ = agents;
  }

}
```

As you probably noticed the real meat of this interceptor bean is inside of the postHandle() method, which examines the User-Agent HTTP request header (if any), checks if it's a mobile device, and if so slightly changes the resulting view name so that a mobile version of the view is rendered instead of the normal version.  According to the Spring documentation, the postHandle() method is "called after HandlerAdapter actually invoked the handler, but before the DispatcherServlet renders the view."  In our case, this is perfect.

Inside of postHandle() my MobileInterceptor retrieves the resolved view name, then if the User-Agent matches that of a known mobile device, it changes the view name by appending "-mobile" to end of it.  For example, say you have a view named "about" that is rendered by "about.jsp".  This interceptor would change the resulting view name to "about-mobile" which would be rendered by "about-mobile.jsp" (assuming you are using a standard [InternalResourceViewResolver](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/web/servlet/view/InternalResourceViewResolver.html) to resolve view names to JSP's).  In other words, this means you can put all of your mobile display logic into about-mobile.jsp, while about.jsp is left in tact for non-mobile clients; you keep your mobile and non-mobile display logic separate in two individual files.  Of course, I don't have to tell you that keeping these separate will make your life as a developer a LOT easier in the long run.

### Putting it all Together

Putting everything together, the `<mvc:interceptor>` XML configuration tells Spring to call my interceptor bean whenever it encounters a specific path.  In this case, I told Spring to watch for the paths /somepath and /anotherpath based on the <mvc:mapping>'s you see above in the XML.  When Spring handles a request for /somepath or /anotherpath it will call the interceptor at the appropriate point in the chain based on the methods overridden by my bean.  In this case, I've overridden the postHandle() method such that Spring will call my interceptor bean to do what it needs to do once the view has been resolved and it's ready to render up some content.  Of course, you could also override preHandle() if you needed the interceptor to be called before a view is selected, and so on.  Again, take a peek at HandlerInterceptorAdapter for all of the gory details.

Enjoy!