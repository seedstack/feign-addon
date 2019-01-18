# Version 1.3.0 (2019-01-18)

* [fix] Properly use SeedStack SSLContext for HTTPS feign connections.
* [chg] Update feign to 10.1.0.

# Version 1.2.0 (2018-05-07)

* [chg] The `baseUrl` attribute in `FeignConfig.EndpointConfig` has been changed from `URL` to `String` to allow for incompletely resolved URLs at startup.

# Version 1.1.0 (2017-11-24)

* [new] Support fallback classes based on `FallbackFactory` to access the exception if any.
* [new] Add configuration options for timeouts. 

# Version 1.0.0 (2017-05-02)

* [new] Extracted from the Netflix add-on. 
