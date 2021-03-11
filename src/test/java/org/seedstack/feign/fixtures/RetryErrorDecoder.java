/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;

import java.util.Date;

public class RetryErrorDecoder implements ErrorDecoder {

    private static int callCount=0;

    /**
     * If the response status id HTTP 500, feign should throw a RetryableException
     * @param methodKey the request method key
     * @param response the response to analyse
     * @return Exception to throw or null
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        increaseCount();
        if(response.status()==500){
            return new RetryableException(response.status(),"Server error", response.request().httpMethod(),new Date(5000), response.request());
        }
        return null;
    }

    public static void increaseCount(){
        callCount++;
    }

    public static void resetCount(){
        callCount=0;
    }

    public static int getCallCount(){
        return callCount;
    }
}
