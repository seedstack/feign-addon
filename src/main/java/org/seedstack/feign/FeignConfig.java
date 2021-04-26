/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign;

import feign.Contract;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.Target;
import feign.Target.HardCodedTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;
import org.seedstack.feign.internal.FeignErrorCode;
import org.seedstack.seed.SeedException;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    public enum HystrixWrapperMode {
        AUTO, ENABLED, DISABLED,
    }
    private Class<?extends Retryer> retryer;
    private RetryConfig retry;

    public FeignConfig setRetryer(Class<? extends Retryer> retryerClass){
        this.retryer=retryerClass;
        return this;
    }
    public Class<?extends Retryer> getRetryer(){
        return this.retryer;
    }

    public FeignConfig setRetry(RetryConfig retryConfig){
        this.retry=retryConfig;
        return this;
    }

    public RetryConfig getRetry(){
        return this.retry;
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
        private boolean followRedirects = true;
        @NotNull
        private List<Class<? extends RequestInterceptor>> interceptors = new ArrayList<>();
        @NotNull
        private HystrixWrapperMode hystrixWrapper = HystrixWrapperMode.AUTO;
        private Class<?> fallback;
        private Class<?extends ErrorDecoder> errorDecoder;
        private Class<? extends Retryer> retryer;
        private RetryConfig retry;

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

        public EndpointConfig setDecoder(Class<? extends Decoder> decoder) {
            this.decoder = decoder;
            return this;
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

        public boolean isFollowRedirects() {
            return followRedirects;
        }

        public EndpointConfig setFollowRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public List<Class<? extends RequestInterceptor>> getInterceptors() {
            return Collections.unmodifiableList(interceptors);
        }

        public void addInterceptor(Class<? extends RequestInterceptor> interceptor) {
            this.interceptors.add(interceptor);
        }

        public Class<? extends ErrorDecoder> getErrorDecoder() {
            return errorDecoder;
        }

        public EndpointConfig setErrorDecoder(Class<? extends ErrorDecoder> errorDecoder) {
            this.errorDecoder = errorDecoder;
            return this;
        }

        public Class<? extends Retryer> getRetryer(){return retryer;}

        public EndpointConfig setRetryer(Class<? extends Retryer> retryer){
            this.retryer=retryer;
            return this;
        }

        public EndpointConfig setRetry(RetryConfig retryConfig){
            this.retry=retryConfig;
            return this;
        }

        public RetryConfig getRetry(){
            return this.retry;
        }
    }
    public static class RetryConfig{
        //Retry default configuration this is the Feign actual default values
        //As we are wrapping the feign default retryer, we need to redefine them here.
        public static final int DEFAULT_PERIOD_IN_MILLIS = 100;
        public static final long DEFAULT_MAX_PERIOD_IN_MILLIS = TimeUnit.SECONDS.toMillis(1);
        public static final int DEFAULT_MAX_ATTEMPTS = 5;
        public static final boolean DEFAULT_ACTIVE_RETRY = true;

        private boolean active= DEFAULT_ACTIVE_RETRY;
        @Min(0)
        private long period= DEFAULT_PERIOD_IN_MILLIS;
        @Min(0)
        private long maxPeriod= DEFAULT_MAX_PERIOD_IN_MILLIS;
        @Min(1)
        private int maxAttempts= DEFAULT_MAX_ATTEMPTS;

        public RetryConfig setActive(boolean retryActive){
            this.active=retryActive;
            return this;
        }

        public boolean isActive(){
            return this.active;
        }

        public RetryConfig setPeriod(long period){
            this.period=period;
            return this;
        }

        public long getPeriod(){
            return this.period;
        }

        public RetryConfig setMaxPeriod(long maxPeriod){
            this.maxPeriod=maxPeriod;
            return this;
        }

        public long getMaxPeriod(){
            return this.maxPeriod;
        }

        public RetryConfig setMaxAttempts(int maxAttempts){
            this.maxAttempts=maxAttempts;
            return this;
        }

        public int getMaxAttempts(){
            return this.maxAttempts;
        }
    }
}
