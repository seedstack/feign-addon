/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import org.kametic.specifications.AbstractSpecification;
import org.seedstack.shed.reflect.ClassPredicates;

import feign.Target;

class FeignTargetInterfaceSpecification extends AbstractSpecification<Class<?>> {

    @Override
    public boolean isSatisfiedBy(Class<?> candidate) {
        return ClassPredicates
                .classIsDescendantOf(Target.class)
                .test(candidate);
    }

}
