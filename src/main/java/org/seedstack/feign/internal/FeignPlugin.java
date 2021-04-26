/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import com.google.common.collect.Lists;
import feign.Contract;
import feign.Target;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.seedstack.feign.FeignConfig;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.core.internal.crypto.CryptoPlugin;

import javax.net.ssl.SSLContext;
import java.util.*;
import java.util.function.Predicate;

public class FeignPlugin extends AbstractSeedPlugin {
    private final Collection<Class<?>> feignInterfaces = new ArrayList<>();
    private final Set<Class<?>> bindings = new HashSet<>();
    private SSLContext sslContext;

    @Override
    public String name() {
        return "feign";
    }

    @Override
    protected Collection<Class<?>> dependencies() {
        return Lists.newArrayList(CryptoPlugin.class);
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .predicate(FeignInterfacePredicate.INSTANCE)
                .predicate(FeignInterceptorPredicate.INSTANCE)
                .build();
    }

    @Override
    protected InitState initialize(InitContext initContext) {
        Map<Predicate<Class<?>>, Collection<Class<?>>> scannedClasses = initContext.scannedTypesByPredicate();
        feignInterfaces.addAll(scannedClasses.get(FeignInterfacePredicate.INSTANCE));

        // Add bindings for endpoints
        for (FeignConfig.EndpointConfig endpointConfig : getConfiguration(FeignConfig.class).getEndpoints().values()) {
            Class<? extends Target<?>> target = endpointConfig.getTarget(Object.class);
            if (!target.equals(Target.HardCodedTarget.class)) {
                bindings.add(target);
            }
            Class<? extends Contract> contract = endpointConfig.getContract();
            if (contract != null) {
                bindings.add(contract);
            }
            bindings.add(endpointConfig.getEncoder());
            bindings.add(endpointConfig.getDecoder());
            if(endpointConfig.getErrorDecoder() !=null) {
                bindings.add(endpointConfig.getErrorDecoder());
            }
            if(endpointConfig.getRetryer() !=null){
                bindings.add(endpointConfig.getRetryer());
            }
            bindings.add(endpointConfig.getLogger());
            Class<?> fallback = endpointConfig.getFallback();
            if (fallback != null) {
                bindings.add(fallback);
            }
        }

        // Add bindings for all interceptors
        bindings.addAll(scannedClasses.get(FeignInterceptorPredicate.INSTANCE));

        // Retrieve SSL context if any
        initContext.dependency(CryptoPlugin.class).sslContext().ifPresent(sslContext -> this.sslContext = sslContext);

        //Adds binding for general retryer
        if(getConfiguration(FeignConfig.class).getRetryer()!=null) {
            bindings.add(getConfiguration(FeignConfig.class).getRetryer());
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new FeignModule(feignInterfaces, bindings, sslContext);
    }

}
