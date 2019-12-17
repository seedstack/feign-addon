/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

import feign.hystrix.FallbackFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import org.seedstack.feign.fixtures.apis.TimeoutAPI;

public class TimeoutFallbackFactory implements FallbackFactory<TimeoutAPI> {
    private static AtomicBoolean fallbackCalled = new AtomicBoolean(false);

    public static boolean isFallbackCalled() {
        return fallbackCalled.get();
    }

    @Override
    public TimeoutAPI create(Throwable cause) {
        return () -> {
            fallbackCalled.set(true);
            return new Message("Fallback response after timeout", "fallback");
        };
    }
}
