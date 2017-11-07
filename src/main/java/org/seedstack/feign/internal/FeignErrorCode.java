/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import org.seedstack.shed.exception.ErrorCode;

enum FeignErrorCode implements ErrorCode {
    ERROR_BUILDING_HYSTRIX_CLIENT,
    ERROR_INSTANTIATING_CONTRACT,
    ERROR_INSTANTIATING_DECODER,
    ERROR_INSTANTIATING_ENCODER,
    ERROR_INSTANTIATING_TARGET,
    ERROR_INSTANTIATING_TARGET_BAD_TARGET_CLASS,
    ERROR_INSTANTIATING_FALLBACK,
    ERROR_INSTANTIATING_LOGGER,
    HYSTRIX_NOT_PRESENT 
}
