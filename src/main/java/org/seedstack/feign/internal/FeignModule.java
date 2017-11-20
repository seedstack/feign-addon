/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.seedstack.feign.internal;

import com.google.inject.AbstractModule;
import java.util.Collection;

class FeignModule extends AbstractModule {
    private final Collection<Class<?>> feignInterfaces;
    private final Collection<Class<?>> bindings;

    FeignModule(Collection<Class<?>> feignInterfaces, Collection<Class<?>> bindings) {
        this.feignInterfaces = feignInterfaces;
        this.bindings = bindings;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        for (Class<?> binding : bindings) {
            bind(binding);
        }

        for (Class<?> feignApi : feignInterfaces) {
            bind(feignApi).toProvider((javax.inject.Provider) new FeignProvider(feignApi));
        }
    }

}
