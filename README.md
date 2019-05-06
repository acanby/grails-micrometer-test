# Unable to use Micrometer for Grails endpoints

As Grails 3.3.x is built on top of Spring Boot 1.5.x, I figured I would try and get Micrometer working for metrics.

**TL;DR: It works for built in endpoints, e.g `/health`, but not Grails endpoints.**

When trying to meter a Grails endpoint, the below exception is logged:
```
2019-05-06 15:20:45.406 DEBUG --- [nio-8080-exec-3] i.m.s.web.servlet.WebMvcMetricsFilter    : Unable to time request

java.lang.IllegalArgumentException: HandlerMapping requires a Grails web request
	at org.springframework.util.Assert.notNull(Assert.java:134)
	at org.grails.web.mapping.mvc.UrlMappingsHandlerMapping.getHandlerInternal(UrlMappingsHandlerMapping.groovy:130)
	at org.springframework.web.servlet.handler.AbstractHandlerMapping.getHandler(AbstractHandlerMapping.java:352)
	at org.springframework.web.servlet.handler.HandlerMappingIntrospector.getMatchableHandlerMapping(HandlerMappingIntrospector.java:123)
	at io.micrometer.spring.web.servlet.WebMvcMetricsFilter.doFilterInternal(WebMvcMetricsFilter.java:86)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.web.filter.CorsFilter.doFilterInternal(CorsFilter.java:96)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.springframework.boot.actuate.autoconfigure.MetricsFilter.doFilterInternal(MetricsFilter.java:103)
	at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107)
	at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193)
	at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166)
	at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:198)
	at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:96)
	at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:493)
	at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:140)
	at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:81)
	at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:87)
	at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:342)
	at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:800)
	at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:66)
	at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:806)
	at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1498)
	at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:49)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	at java.lang.Thread.run(Thread.java:748)
```

It should be noted that this *does not* impact the actual request, but does prevent it from being automatically `@Timed`.

## Steps to reproduce
* Run the application in one terminal `./gradlew bootRun`
* In another terminal, build up some data:
    ```bash
    # Built in health endpoint
    curl -X GET http://localhost:8080/health
    curl -X GET http://localhost:8080/health
    
    # Added by Micrometer
    curl -X GET http://localhost:8080/prometheus
    curl -X GET http://localhost:8080/prometheus
    curl -X GET http://localhost:8080/prometheus
    
    # A simple Grails endpoint
    curl -X GET http://localhost:8080/test
    curl -X GET http://localhost:8080/test
    curl -X GET http://localhost:8080/test
    ```
 * Check the Prometheus output
    ```bash
    curl -s -X GET http://localhost:8080/prometheus | grep http_server_requests_seconds
    ```

    The non Grails requests are shown, but the Grails request is absent:
    ```
    # HELP http_server_requests_seconds  
    # TYPE http_server_requests_seconds summary
    http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/health",} 2.0
    http_server_requests_seconds_sum{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/health",} 0.205125181
    http_server_requests_seconds_count{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/prometheus",} 3.0
    http_server_requests_seconds_sum{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/prometheus",} 0.045857583
    # HELP http_server_requests_seconds_max  
    # TYPE http_server_requests_seconds_max gauge
    http_server_requests_seconds_max{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/health",} 0.203338939
    http_server_requests_seconds_max{exception="None",method="GET",outcome="SUCCESS",status="200",uri="/prometheus",} 0.0360817
    ```

