/*
 * Copyright © 2013-2018, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.feign.internal;

import feign.Contract;
import feign.Target;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.kametic.specifications.Specification;
import org.seedstack.feign.FeignConfig;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;

public class FeignPlugin extends AbstractSeedPlugin {
    private final Specification<Class<?>> FEIGN_INTERFACE_SPECIFICATION = new FeignInterfaceSpecification();
    private final Collection<Class<?>> feignInterfaces = new ArrayList<>();
    private final Set<Class<?>> bindings = new HashSet<>();

    @Override
    public String name() {
        return "feign";
    }

    @Override
    public Collection<ClasspathScanRequest> classpathScanRequests() {
        return classpathScanRequestBuilder()
                .specification(FEIGN_INTERFACE_SPECIFICATION)
                .build();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected InitState initialize(InitContext initContext) {
        Map<Specification, Collection<Class<?>>> scannedClasses = initContext.scannedTypesBySpecification();
        feignInterfaces.addAll(scannedClasses.get(FEIGN_INTERFACE_SPECIFICATION));

        // Add simple bindings
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
            bindings.add(endpointConfig.getLogger());
            Class<?> fallback = endpointConfig.getFallback();
            if (fallback != null) {
                bindings.add(fallback);
            }
        }

        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new FeignModule(feignInterfaces, bindings);
    }

}
