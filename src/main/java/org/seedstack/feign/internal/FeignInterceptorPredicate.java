/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import feign.RequestInterceptor;
import feign.RequestLine;
import org.seedstack.shed.reflect.ClassPredicates;

import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/**
 * The predicate matches classes that are Interfaces and have methods annotated with @{@link RequestLine}
 *
 * @author adrien.domurado@gmail.com
 */
class FeignInterceptorPredicate implements Predicate<Class<?>> {
    static final FeignInterceptorPredicate INSTANCE = new FeignInterceptorPredicate();

    private FeignInterceptorPredicate() {
        // no external instantiation
    }

    @Override
    public boolean test(Class<?> candidate) {
        return ClassPredicates
                .classImplements(RequestInterceptor.class)
                .and(ClassPredicates.classModifierIs(Modifier.ABSTRACT).negate())
                .test(candidate);
    }
}
