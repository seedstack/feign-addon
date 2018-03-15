/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign;

import feign.Contract;
import feign.Logger;
import feign.Target;
import feign.Target.HardCodedTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.feign.internal.FeignErrorCode;
import org.seedstack.seed.SeedException;

@Config("feign")
public class FeignConfig {
    @NotNull
    private Map<Class<?>, EndpointConfig> endpoints = new HashMap<>();

    public Map<Class<?>, EndpointConfig> getEndpoints() {
        return Collections.unmodifiableMap(endpoints);
    }

    public void addEndpoint(Class<?> endpointClass, EndpointConfig endpoint) {
        endpoints.put(endpointClass, endpoint);
    }

    @SuppressWarnings("rawtypes")
    public static class EndpointConfig {
        @SingleValue
        @NotNull
        private String baseUrl;
        private Class<? extends Contract> contract;
        @NotNull
        private Class<? extends Target> target = HardCodedTarget.class;
        @NotNull
        private Class<? extends Encoder> encoder = JacksonEncoder.class;
        @NotNull
        private Class<? extends Decoder> decoder = JacksonDecoder.class;
        @NotNull
        private Class<? extends Logger> logger = Slf4jLogger.class;
        @NotNull
        private Logger.Level logLevel = Logger.Level.NONE;
        @NotNull
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        @Min(0)
        private int connectTimeout = 10000;
        @Min(0)
        private int readTimeout = 60000;
        @NotNull
        private HystrixWrapperMode hystrixWrapper = HystrixWrapperMode.AUTO;
        private Class<?> fallback;

        public String getBaseUrl() {
            return baseUrl;
        }

        public EndpointConfig setBaseUrl(String baseUrl) {
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

        public Class<? extends Contract> getContract() {
            return contract;
        }

        public EndpointConfig setContract(Class<? extends Contract> contract) {
            this.contract = contract;
            return this;
        }

        public Class<? extends Decoder> getDecoder() {
            return decoder;
        }

        @SuppressWarnings("unchecked")
        public <T> Class<? extends Target<T>> getTarget(Class<T> apiClass) {
            if (target == null) {
                return null;
            } else {
                if (HardCodedTarget.class.isAssignableFrom(target) || apiClass.isAssignableFrom(target)) {
                    return (Class<Target<T>>) target;
                } else {
                    throw SeedException
                            .createNew(FeignErrorCode.BAD_TARGET_CLASS)
                            .put("class", target)
                            .put("api", apiClass);
                }
            }
        }

        public EndpointConfig setTarget(Class<? extends Target> target) {
            this.target = target;
            return this;
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

        @SuppressWarnings("unchecked")
        public <T> Class<T> getFallback() {
            return (Class<T>) fallback;
        }

        public EndpointConfig setFallback(Class<?> fallback) {
            this.fallback = fallback;
            return this;
        }

        public TimeUnit getTimeUnit() {
            return timeUnit;
        }

        public EndpointConfig setTimeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public int getConnectTimeout() {
            return connectTimeout;
        }

        public EndpointConfig setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public int getReadTimeout() {
            return readTimeout;
        }

        public EndpointConfig setReadTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }
    }

    public enum HystrixWrapperMode {
        AUTO, ENABLED, DISABLED,
    }
}
