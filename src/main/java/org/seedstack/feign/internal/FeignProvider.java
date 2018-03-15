/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.feign.internal;

import com.google.inject.Injector;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Target;
import feign.Target.HardCodedTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.hystrix.FallbackFactory;
import feign.hystrix.HystrixFeign;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Provider;
import org.seedstack.feign.FeignConfig;
import org.seedstack.feign.FeignConfig.EndpointConfig;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.shed.reflect.Classes;

class FeignProvider<T> implements Provider<Object> {
    private static final boolean HYSTRIX_PRESENT = Classes.optional("com.netflix.hystrix.Hystrix").isPresent();
    private final Class<T> feignApi;
    @Configuration
    private FeignConfig config;
    @Inject
    private Injector injector;

    FeignProvider(Class<T> feignApi) {
        this.feignApi = feignApi;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get() {
        FeignConfig.EndpointConfig endpointConfig = config.getEndpoints().get(feignApi);
        Feign.Builder builder = createBuilder(endpointConfig);
        builder.encoder(instantiateEncoder(endpointConfig.getEncoder()));
        builder.decoder(instantiateDecoder(endpointConfig.getDecoder()));
        if (endpointConfig.getContract() != null) {
            builder.contract(instantiateContract(endpointConfig.getContract()));
        }
        builder.logger(instantiateLogger(endpointConfig.getLogger()));
        builder.logLevel(endpointConfig.getLogLevel());
        TimeUnit timeUnit = endpointConfig.getTimeUnit();
        builder.options(new Request.Options(
                (int) timeUnit.toMillis(endpointConfig.getConnectTimeout()),
                (int) timeUnit.toMillis(endpointConfig.getReadTimeout()))
        );

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
