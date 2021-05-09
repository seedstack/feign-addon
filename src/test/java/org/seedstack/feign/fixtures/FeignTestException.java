/*
 * Copyright © 2013-2021, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

/**
 * Feign addon test exception
 */
public class FeignTestException extends Exception{
    /**
     * Statndard constructor
     * @param message exception message
     */
    public FeignTestException(String message){
        super(message);
    }
}
