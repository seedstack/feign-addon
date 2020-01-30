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
    
    public static void main(String... args) {
        List<Message> messages = api.getMessages();
        for (Message message : messages) {
            System.out.println(message.author);
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
            logger: feign.slf4j.SLF4JLogger 
            logLevel: NONE
            hystrixWrapper: AUTO
            fallback: com.mycompany.myapp.Fallback
```
The values in this example are the default values, except for `baseUrl` and `fallback` that don't have default values.

* `baseUrl` is this only mandatory option.
* `encoder` and `decoder` let you configure how your data is transformed when sent and received, respectively.
* `logger` let you choose which logger to use to log the requests.
* `logLevel` is an enum (`NONE`, `BASIC`, `HEADERS`, `FULL`).
* `hystrixWrapper` is an enum (`AUTO`, `ENABLED`, `DISABLED`). Feign comes with Hystrix circuit-breaker support. `DISABLED` disables this functionality. `ENABLED` tells Feign to wrap all requests in Hystrix mechanism, but the lib Hystrix must be in the classpath of your project. `AUTO` mode will scan the classpath and if Hystrix is present, will wrap requests with it.
* `fallback` takes a fully qualified class name and is only relevant when Hystrix is used. The fallback class must implement your API interface and will be used to return default values in case the requests are in error.

## Authentication

To call an API protected with authentication you can specify a header in your Feign interface with the `@Headers` annotation (example for basic authentication):

```yaml
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

```yaml
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
