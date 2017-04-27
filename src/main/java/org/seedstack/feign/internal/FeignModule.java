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
    private final Collection<Class<?>> feignApis;

    FeignModule(Collection<Class<?>> feignApis) {
        this.feignApis = feignApis;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        for (Class<?> feignApi : feignApis) {
            bind(feignApi).toProvider((javax.inject.Provider) new FeignProvider(feignApi));
        }
    }
}
