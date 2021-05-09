/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import com.google.inject.Injector;
import feign.*;
import feign.Target.HardCodedTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.hystrix.FallbackFactory;
import feign.hystrix.HystrixFeign;
import org.seedstack.feign.FeignConfig;
import org.seedstack.feign.FeignConfig.EndpointConfig;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.shed.reflect.Classes;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.net.ssl.SSLContext;

class FeignProvider<T> implements Provider<Object> {
    private static final boolean HYSTRIX_PRESENT = Classes.optional("com.netflix.hystrix.Hystrix").isPresent();
    private final Class<T> feignApi;
    private final SSLContext sslContext;
    @Configuration
    private FeignConfig config;
    @Inject
    private Injector injector;

    FeignProvider(Class<T> feignApi, SSLContext sslContext) {
        this.feignApi = feignApi;
        this.sslContext = sslContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get() {
        FeignConfig.EndpointConfig endpointConfig = config.getEndpoints().get(feignApi);
        Feign.Builder builder = createBuilder(endpointConfig);

        // Encoder and decoder
        builder.encoder(instantiateEncoder(endpointConfig.getEncoder()));
        builder.decoder(instantiateDecoder(endpointConfig.getDecoder()));

        // Contract
        if (endpointConfig.getContract() != null) {
            builder.contract(instantiateContract(endpointConfig.getContract()));
        }

        if(endpointConfig.getErrorDecoder() != null){
            builder.errorDecoder(instantiateErrorDecoder(endpointConfig.getErrorDecoder()));
        }

        //Retry configuration
        setUpRetryOption(builder, endpointConfig);

        // Logger
        builder.logger(instantiateLogger(endpointConfig.getLogger()));
        builder.logLevel(endpointConfig.getLogLevel());

        // Timeouts and follow redirects
        builder.options(new Request.Options(
                endpointConfig.getConnectTimeout(), endpointConfig.getTimeUnit(),
                endpointConfig.getReadTimeout(), endpointConfig.getTimeUnit(),
                endpointConfig.isFollowRedirects())
        );

        // HTTP(s) client
        if (sslContext != null) {
            builder.client(new Client.Default(sslContext.getSocketFactory(), null));
        } else {
            builder.client(new Client.Default(null, null));
        }

        // Interceptors
        endpointConfig.getInterceptors().forEach(i -> builder.requestInterceptor(injector.getInstance(i)));

        Class<T> fallback = endpointConfig.getFallback();
        if (fallback != null) {
            if (builder instanceof HystrixFeign.Builder) {
                if (FallbackFactory.class.isAssignableFrom(fallback)) {
                    return ((HystrixFeign.Builder) builder).target(
                            instantiateTarget(endpointConfig),
                            (FallbackFactory<T>) instantiateFallback(fallback)
                    );
                } else {
                    return ((HystrixFeign.Builder) builder).target(
                            instantiateTarget(endpointConfig),
                            (T) instantiateFallback(fallback)
                    );
                }
            } else {
                throw SeedException.createNew(FeignErrorCode.HYSTRIX_NOT_PRESENT)
                        .put("endpoint", feignApi.getName());
            }
        } else {
            return builder.target(instantiateTarget(endpointConfig));
        }
    }

    private Feign.Builder createBuilder(FeignConfig.EndpointConfig endpointConfig) {
        switch (endpointConfig.getHystrixWrapper()) {
            case AUTO:
                if (HYSTRIX_PRESENT) {
                    return HystrixFeign.builder();
                } else {
                    return Feign.builder();
                }
            case ENABLED:
                if (HYSTRIX_PRESENT) {
                    return HystrixFeign.builder();
                } else {
                    throw SeedException.createNew(FeignErrorCode.HYSTRIX_NOT_PRESENT)
                            .put("endpoint", feignApi.getName());
                }
            case DISABLED:
                return Feign.builder();
            default:
                throw new IllegalArgumentException("Unsupported Hystrix mode " + endpointConfig.getHystrixWrapper());
        }
    }

    private void setUpRetryOption(Feign.Builder builder, FeignConfig.EndpointConfig endpointConfig){
        //If an endpoint has specific retry configuration, this configuration has priority
        //Else, applying global configuration if found, and finally, the builder will use default Feign configuration
        if(endpointConfig.getRetryer() !=null  || endpointConfig.getRetry()!=null){
            //Setting up specific retry configuration for this builder
            setUpEndPointConfiguredRetryOption(builder, endpointConfig);
        }
        else if(config.getRetryer() !=null || config.getRetry() !=null){
            //Applying global configuration for this builder
            setUpGlobalConfiguredRetryOption(builder);
        }
    }

    private void setUpEndPointConfiguredRetryOption(Feign.Builder builder, FeignConfig.EndpointConfig endpointConfig){
        if(endpointConfig.getRetryer() !=null  && endpointConfig.getRetry()!=null){
            throw SeedException.createNew(FeignErrorCode.ERROR_TWO_RETRYER_ENDPOINT_CONFIGURATIONS).put("endpoint", feignApi.getName());
        }
        if(endpointConfig.getRetryer() !=null){
            builder.retryer(instanciateRetryer(endpointConfig.getRetryer()));
        }
        else{
            applyRetryConfigurationToBuilder( builder, endpointConfig.getRetry());
        }
    }
    private void setUpGlobalConfiguredRetryOption(Feign.Builder builder){
        if(config.getRetryer() !=null  && config.getRetry()!=null){
            throw SeedException.createNew(FeignErrorCode.ERROR_TWO_RETRYER_GLOBAL_CONFIGURATIONS);
        }
        if(config.getRetryer() !=null){
            builder.retryer(instanciateRetryer(config.getRetryer()));
        }
        else {
            applyRetryConfigurationToBuilder(builder, config.getRetry());
        }
    }

    private void applyRetryConfigurationToBuilder(Feign.Builder builder, FeignConfig.RetryConfig retryConfig){
        if(retryConfig.isActive()){
            builder.retryer(new FeignConfigurableRetryer(retryConfig.getPeriod(), retryConfig.getMaxPeriod(), retryConfig.getMaxAttempts()));
        }else{
            //De-activating retry for this builder
            builder.retryer(Retryer.NEVER_RETRY);
        }
    }

    private Object instantiateFallback(Class<?> fallback) {
        try {
            return injector.getInstance(fallback);
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_FALLBACK)
                    .put("class", fallback);
        }
    }

    private Contract instantiateContract(Class<? extends Contract> contractClass) {
        try {
            return injector.getInstance(contractClass);
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_CONTRACT)
                    .put("class", contractClass);
        }
    }

    private Encoder instantiateEncoder(Class<? extends Encoder> encoderClass) {
        try {
            return injector.getInstance(encoderClass);
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_ENCODER)
                    .put("class", encoderClass);
        }
    }

    private Decoder instantiateDecoder(Class<? extends Decoder> decoderClass) {
        try {
            return injector.getInstance(decoderClass);
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_DECODER)
                    .put("class", decoderClass);
        }
    }

    private ErrorDecoder instantiateErrorDecoder(Class<? extends ErrorDecoder> errorDecoderClass){
        try{
            return injector.getInstance(errorDecoderClass);
        }catch (Exception e){
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_ERROR_DECODER)
                    .put("class", errorDecoderClass);
        }
    }

    private Retryer instanciateRetryer(Class<? extends Retryer> retryerClass){
        try{
            return injector.getInstance(retryerClass);
        }
        catch(Exception e){
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_RETRYER)
                    .put("class", retryerClass);
        }
    }

    private Target<T> instantiateTarget(EndpointConfig endpointConfig) {
        Class<? extends Target<T>> targetClass = endpointConfig.getTarget(feignApi);
        if (HardCodedTarget.class.equals(targetClass)) {
            return new HardCodedTarget<>(feignApi, endpointConfig.getBaseUrl());
        } else {
            try {
                return injector.getInstance(targetClass);
            } catch (Exception e) {
                throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_TARGET)
                        .put("class", targetClass);
            }
        }
    }

    private Logger instantiateLogger(Class<? extends Logger> loggerClass) {
        try {
            return injector.getInstance(loggerClass);
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_LOGGER)
                    .put("class", loggerClass);
        }
    }
}
