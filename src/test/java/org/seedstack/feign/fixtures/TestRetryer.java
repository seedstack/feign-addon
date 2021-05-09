/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

import feign.RetryableException;
import feign.Retryer;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

public class TestRetryer implements Retryer {
    @Logging
    private static Logger logger;

    private static final int MAX_RETRYS=3;
    private static boolean called=false;
    private static int countCalls;

    public TestRetryer(){
        countCalls=1;
    }
    @Override
    public void continueOrPropagate(RetryableException e) {
        int currentCount = TestRetryer.getCountCalls();
        logger.info("Retrying. Current attempt : {} - Max attempts : {}", currentCount, MAX_RETRYS);
        increaseCountCalls();
        if(currentCount<MAX_RETRYS){
            return;
        }
        throw e;
    }

    private static void increaseCountCalls(){
        countCalls++;
        called=true;
    }

    public static int getCountCalls(){
        return countCalls;
    }

    public static boolean isCalled(){
        return called;
    }

    @Override
    public Retryer clone() {
        return this;
    }
}
