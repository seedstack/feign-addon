/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.api.plugin.request.ClasspathScanRequest;
import org.kametic.specifications.Specification;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class FeignPlugin extends AbstractSeedPlugin {
    private final Specification<Class<?>> FEIGN_INTERFACE_SPECIFICATION = new FeignInterfaceSpecification();
    private Collection<Class<?>> feignApis = new ArrayList<>();

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

    @Override
    protected InitState initialize(InitContext initContext) {
        Map<Specification, Collection<Class<?>>> scannedClasses = initContext.scannedTypesBySpecification();
        feignApis.addAll(scannedClasses.get(FEIGN_INTERFACE_SPECIFICATION));
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        return new FeignModule(feignApis);
    }

}
