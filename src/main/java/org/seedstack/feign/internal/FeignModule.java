/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import feign.Target;

class FeignModule extends AbstractModule {
    private final Collection<Class<?>> feignApis;
    private final List<Class<? extends Target<?>>> feignTargets = new ArrayList<>();

    FeignModule(Collection<Class<?>> feignApis, Collection<Class<?>> targetClasses) {
        this.feignApis = feignApis;
        resolveFeignTargets(targetClasses);

    }

    @SuppressWarnings("unchecked")
    private void resolveFeignTargets(Collection<Class<?>> targetClasses) {
        targetClasses.stream()
                .map(x -> (Class<? extends Target<?>>) x)
                .forEach(feignTargets::add);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void configure() {
        Multibinder<Target> targetMultibinder = Multibinder.newSetBinder(binder(), Target.class);
        for (Class<? extends Target> targetClass : feignTargets) {
            targetMultibinder.addBinding().to((Class<? extends Target>) targetClass);
        }

        //TODO: find out what's going on here. 
        //Somehow the multibinder is not working as expected.
        FeignTargetContainer container = new FeignTargetContainer();
        requestInjection(container);

        container.tryInjetion();

        for (Class<?> feignApi : feignApis) {
            bind(feignApi).toProvider((javax.inject.Provider) new FeignProvider(feignApi));
        }
    }

}
