/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import feign.Retryer;

/**
 * This Retryer wraps Feign's default one with the required parameters
 */
public class FeignConfigurableRetryer extends Retryer.Default {

    /**
     * Invokes default retryer constructor with required parameters
     * @param period Startng retry period between to retrys
     * @param maxPeriod maximum period between two retrys ( period is increaded by 1.5 between each attempt)
     * @param maxAttempts maximum attempts to perform
     */
    public FeignConfigurableRetryer(long period, long maxPeriod, int maxAttempts){
        super(period, maxPeriod, maxAttempts);
    }
}
