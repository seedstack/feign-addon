/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign;

import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Config("feign")
public class FeignConfig {
    private Map<Class<?>, EndpointConfig> endpoints = new HashMap<>();

    public Map<Class<?>, EndpointConfig> getEndpoints() {
        return Collections.unmodifiableMap(endpoints);
    }

    public void addEndpoint(Class<?> endpointClass, EndpointConfig endpoint) {
        endpoints.put(endpointClass, endpoint);
    }

    public static class EndpointConfig {
        @SingleValue
        @NotNull
        private URL baseUrl;

        private Class<? extends Encoder> encoder = JacksonEncoder.class;

        private Class<? extends Decoder> decoder = JacksonDecoder.class;

        private Class<? extends Logger> logger = Slf4jLogger.class;

        private Logger.Level logLevel = Logger.Level.NONE;

        private HystrixWrapperMode hystrixWrapper = HystrixWrapperMode.AUTO;

        private Class<?> fallback;

        public URL getBaseUrl() {
            return baseUrl;
        }

        public EndpointConfig setBaseUrl(URL baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Class<? extends Encoder> getEncoder() {
            return encoder;
        }

        public EndpointConfig setEncoder(Class<? extends Encoder> encoder) {
            this.encoder = encoder;
            return this;
        }

        public Class<? extends Decoder> getDecoder() {
            return decoder;
        }

        public EndpointConfig setDecoder(Class<? extends Decoder> decoder) {
            this.decoder = decoder;
            return this;
        }

        public Class<? extends Logger> getLogger() {
            return logger;
        }

        public EndpointConfig setLogger(Class<? extends Logger> logger) {
            this.logger = logger;
            return this;
        }

        public Logger.Level getLogLevel() {
            return logLevel;
        }

        public EndpointConfig setLogLevel(Logger.Level logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public HystrixWrapperMode getHystrixWrapper() {
            return hystrixWrapper;
        }

        public EndpointConfig setHystrixWrapper(HystrixWrapperMode hystrixWrapper) {
            this.hystrixWrapper = hystrixWrapper;
            return this;
        }

        public Class<?> getFallback() {
            return fallback;
        }

        public EndpointConfig setFallback(Class<?> fallback) {
            this.fallback = fallback;
            return this;
        }
    }

    public enum HystrixWrapperMode {
        AUTO,
        ENABLED,
        DISABLED,
    }
}
