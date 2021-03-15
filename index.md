---
title: "Feign"
addon: "Feign"
repo: "https://github.com/seedstack/feign-addon"
description: Official integration of OpenFeign, the easy-to-use HTTP client.
author: "Adrien DOMURADO"
tags:
    - API
    - REST
    - micro-service
zones:
    - Addons
noMenu: true    
---

This component allows you to define an HTTP client with a simple Java interface that you can then inject and use transparently
in your code.<!--more-->

{{< dependency g="org.seedstack.addons.feign" a="feign" >}}

## How to use

First, you need to create an interface annotated by `@FeignApi`, with each method being an HTTP call. Annotate each method with `@RequestLine`:

```java
@FeignApi
public interface Api {
    @RequestLine("GET /message")
    List<Message> getMessages();
    
    @RequestLine("GET /message/{id}")
    Message getMessage(@Param("id") int id);
}
```

Then, you can use this API by injecting it:

```java
public class MyClass {
    @Inject
    private Api api;
    @Logging
    private Logger logger;
    
    public void someMethod() {
        List<Message> messages = api.getMessages();
        for (Message message : messages) {
            logger.info("Received message from {}", message.author);
        }
    }
}
```

{{% callout info %}}
For more information about OpenFeign, look into its documentation: [https://github.com/OpenFeign/feign](https://github.com/OpenFeign/feign).
{{% /callout %}}

## Configuration

Configuration is done by 

Feign is configurable by endpoint, and in its basic form is:

{{% config p="feign" %}}
```yaml
feign:
    endpoints:
        com.mycompany.myapp.Api: http://base.url.to.api:port

```

`com.mycompany.myapp.Api` is the fully qualified name of your api interface, and you can add as many as you want.
`http://base.url.to.api:port` is the base URL of this API.

With all the options, the configuration file looks like this:

```yaml
feign:
    endpoints:
        com.mycompany.myapp.Api: 
            baseUrl: http://base.url.to.api:port
            encoder: feign.jackson.JacksonEncoder
            decoder: feign.jackson.JacksonDecoder
            errorDecoder: com.mycompany.myapp.MyApiErrorDecoder
            logger: feign.slf4j.SLF4JLogger 
            logLevel: NONE
            hystrixWrapper: AUTO
            fallback: com.mycompany.myapp.Fallback
            retryer: com.mycompany.myapp.MyRetryer
```
The values in this example are the default values, except for `baseUrl` and `fallback` that don't have default values.

* `baseUrl` is this only mandatory option.
* `encoder` and `decoder` let you configure how your data is transformed when sent and received, respectively.
* `errorDecoder` let you define a custom injectable errorDecoder related to this API for request error handling. (See [https://github.com/OpenFeign/feign#error-handling](https://github.com/OpenFeign/feign#error-handling) )
* `logger` let you choose which logger to use to log the requests.
* `logLevel` is an enum (`NONE`, `BASIC`, `HEADERS`, `FULL`).
* `hystrixWrapper` is an enum (`AUTO`, `ENABLED`, `DISABLED`). Feign comes with Hystrix circuit-breaker support. `DISABLED` disables this functionality. `ENABLED` tells Feign to wrap all requests in Hystrix mechanism, but the lib Hystrix must be in the classpath of your project. `AUTO` mode will scan the classpath and if Hystrix is present, will wrap requests with it.
* `fallback` takes a fully qualified class name and is only relevant when Hystrix is used. The fallback class must implement your API interface and will be used to return default values in case the requests are in error.
* `retryer` let you define a custom injectable Retryer related to this endpoint for retry policy handling. ( See [https://github.com/OpenFeign/feign#retry](https://github.com/OpenFeign/feign#retry) ). See below for more retry configuration options.


## Authentication

To call an API protected with authentication you can specify a header in your Feign interface with the `@Headers` annotation (example for basic authentication):

```java
@FeignApi
@Headers({"Authorization: Basic {credentials}"})
public interface neosdServer {

    @RequestLine("GET /file/getfilesprop")
    List<NeosdFile> getfilesprop(@Param("credentials") String credentials);

    @RequestLine("GET /file/getfiles")
    List<String> getfiles(@Param("credentials") String credentials);
}
```

{{% callout info %}}
Note that `@Headers` can also be used on individual methods. 
{{% /callout %}}

Then, pass the credentials as method parameter. An example implementation, with credentials coming from your application configuration, coudl be:

```java
public class MyClass {
    @Configuration("myApp.credentials.user")
    private String username;
    @Configuration("myApp.credentials.password")
    private String password;
    @Inject
    private NeoSdClient client;

    public void myMethod() {
        List<String> files = client.getFiles(encodeCredentials());
    }

    private String encodeCredentials() {
        return BaseEncoding
                .base64()
                .encode((username + ":" + password)
                        .getBytes(Charsets.UTF_8));
    }
}
```

## Fallback

A fallback class is done by implementing the Feign interface and return default values from it:

```java
public class Fallback implements Api {
     @Override
     List<Message> getMessages() {
        // return your default value here 
     }
        
     @Override
     Message getMessage(@Param("id") int id) {
        // return your default value here
     }
}
```

{{% callout info %}}
For more information on Hystrix: [https://github.com/Netflix/Hystrix/wiki](https://github.com/Netflix/Hystrix/wiki)
{{% /callout %}}

## Retry configuration

Feign plugin retry configuration has two levels :  global configuration and endpoint configuration.

The endpoint configuration has priority upon global configration, letting you having global settings and, if necessary specific settings for particular endpoints.

To resume, if an endpoint has a retry configuration set, it will be applyed, if not, the global configuration will be applyed, if there is no retry configuration set (global or specific ), the default feign retry configuration will be applyed.

There are two type of retry configuration : standard parameters or retryer class configuration. Both types can't be defined for the same configuration level (global /endpoint ). 


### Standard parameters configuration

The following retry parameters can be set :
* `period` : The initial time period, in milliseconds, between two requests attemps. Feign will increase this period by half ( multiply by 1.5 ) until it reaches the maximum allowed period.
* `maxPeriod` : Maximum allowed period, in milliseconds, between two attempts. Feign won't increase a retry period more than this parameter.
* `maxAttempts` : Maximum retry attempts for a single request. If the request still fails after the last attempt, the feign API will throw an exception.

#### Global configuration

Here is a sample for global retry configuration, applyed to each endpoint with no retry/retryer configuration

```yaml
feign:
    retry:
        period: 100
        maxPeriod: 1000
        maxAttempts: 3
    endpoints:
        com.mycompany.myapp.Api: http://base.url.to.api:port
```

#### Endpoint configuration

Here is a sample for specific endpoint retry configuration, applyed only to this specific endpoint :

```yaml
feign:
    endpoints:
        com.mycompany.myapp.Api: http://base.url.to.api:port
            retry:
                period: 100
                maxPeriod: 1000
                maxAttempts: 3
```

### Retryer class configuration

With feign, you can define a retryer having the role to determine if a request should be retryed or not, letting you decide of the retry policy you want to apply to your endpoint ( See [https://github.com/OpenFeign/feign#retry](https://github.com/OpenFeign/feign#retry) ). This feature is supported in the seedstack feign plugin. Your custom retryer class has to implement the feign "Retryer" interface and added to your plugin configuration. Note that this class is injectable.

#### Global configuration

Here is a sample for global retryer configuration, applyed to each endpoint with no retry/retryer configuration

```yaml
feign:
    retryer: com.mycompany.myapp.MyRetryer
    endpoints:
        com.mycompany.myapp.Api: http://base.url.to.api:port
```

#### Endpoint configuration

Here is a sample for a specific endpoint retryer configuration, applyed only to this specific endpoint :

```yaml
feign:
    endpoints:
        com.mycompany.myapp.Api: http://base.url.to.api:port
            retryer: com.mycompany.myapp.MyRetryer
```