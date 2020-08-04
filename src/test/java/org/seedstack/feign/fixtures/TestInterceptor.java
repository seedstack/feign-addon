/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.seedstack.seed.Application;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class TestInterceptor implements RequestInterceptor {
    public static boolean called = false;
    @Inject
    private Application application;

    @Override
    public void apply(RequestTemplate template) {
        called = true;
        assertThat(application).isNotNull();
    }
}
