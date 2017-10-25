/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.seedstack.feign.FeignConfig;
import org.seedstack.feign.FeignConfig.EndpointConfig;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.shed.reflect.Classes;

import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Target;
import feign.Target.HardCodedTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.hystrix.HystrixFeign;

class FeignProvider implements Provider<Object> {

    private static final String FAILURE_CLASS_TEXT = "class";
    private static final Optional<Class<Object>> HYSTRIX_OPTIONAL = Classes
            .optional("com.netflix.hystrix.Hystrix");
    @Configuration
    private FeignConfig config;
    private Class<?> feignApi;

    FeignProvider(Class<?> feignApi) {
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

        if (endpointConfig.getFallback() != null) {
            if (builder instanceof HystrixFeign.Builder) {
                return buildHystrixClient(endpointConfig, builder,
                        instantiateFallback(endpointConfig.getFallback()));
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
            return HYSTRIX_OPTIONAL.map(dummy -> (Feign.Builder) HystrixFeign.builder())
                    .orElse(Feign.builder());
        case ENABLED:
            return HYSTRIX_OPTIONAL.map(dummy -> (Feign.Builder) HystrixFeign.builder())
                    .orElseThrow(() -> (SeedException) SeedException
                            .createNew(FeignErrorCode.HYSTRIX_NOT_PRESENT)
                            .put("endpoint", feignApi.getName()));
        case DISABLED:
            return Feign.builder();
        default:
            throw new IllegalArgumentException(
                    "Unsupported Hystrix mode " + endpointConfig.getHystrixWrapper());
        }
    }

    private Object buildHystrixClient(FeignConfig.EndpointConfig endpointConfig,
            Feign.Builder builder, Object fallback) {
        try {
            Method target = HystrixFeign.Builder.class.getMethod("target", Target.class,
                    Object.class);
            return target.invoke(builder, instantiateTarget(endpointConfig), fallback);
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_BUILDING_HYSTRIX_CLIENT)
                    .put(FAILURE_CLASS_TEXT, fallback);
        }
    }

    private Object instantiateFallback(Class<?> fallback) {
        try {
            return fallback.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_FALLBACK)
                    .put(FAILURE_CLASS_TEXT, fallback);
        }
    }

    private Contract instantiateContract(Class<? extends Contract> contractClass) {
        try {
            return contractClass.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_CONTRACT)
                    .put(FAILURE_CLASS_TEXT, contractClass);
        }
    }

    private Encoder instantiateEncoder(Class<? extends Encoder> encoderClass) {
        try {
            return encoderClass.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_ENCODER)
                    .put(FAILURE_CLASS_TEXT, encoderClass);
        }
    }

    private Decoder instantiateDecoder(Class<? extends Decoder> decoderClass) {
        try {
            return decoderClass.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_DECODER)
                    .put(FAILURE_CLASS_TEXT, decoderClass);
        }
    }

    @SuppressWarnings({ "rawtypes" })
    private Target instantiateTarget(EndpointConfig endpointConfig) {
        Class<? extends Target> targetClass = endpointConfig.getTarget();
        Target<?> target;

        if (targetClass.equals(HardCodedTarget.class)) {
            target = new HardCodedTarget<>(feignApi, endpointConfig.getBaseUrl().toExternalForm());
        } else {
            try {

                target = targetClass.newInstance();
            } catch (Exception e) {
                throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_TARGET)
                        .put(FAILURE_CLASS_TEXT, targetClass);
            }
        }
        if (!target.type().isAssignableFrom(feignApi)) {
            throw SeedException
                    .createNew(FeignErrorCode.ERROR_INSTANTIATING_TARGET_BAD_TARGET_CLASS)
                    .put(FAILURE_CLASS_TEXT, targetClass);
        }
        return target;
    }

    private Logger instantiateLogger(Class<? extends Logger> loggerClass) {
        try {
            return loggerClass.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_LOGGER)
                    .put(FAILURE_CLASS_TEXT, loggerClass);
        }
    }
}
