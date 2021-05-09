/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;

/**
 * Custom Error Decoder
 */
public class TestErrorDecoder implements ErrorDecoder {

    /**
     * Treat request error, as used in a unit test, resolving with a specific error message in the exception that should be verified in the unit test
     * @param methodKey The method key
     * @param response the feign request response
     * @return Exception
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()){
            case 404 : {
                return new FeignTestException("Feign addon unit test : received HTTP 404 error code");
            }
            default:{
                return new FeignTestException(response.reason());
            }
        }
    }
}
