/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.internal;

import org.seedstack.shed.exception.ErrorCode;

public enum FeignErrorCode implements ErrorCode {
    BAD_TARGET_CLASS,
    BAD_FALLBACK_CLASS,
    ERROR_INSTANTIATING_CONTRACT,
    ERROR_INSTANTIATING_DECODER,
    ERROR_INSTANTIATING_ENCODER,
    ERROR_INSTANTIATING_TARGET,
    ERROR_INSTANTIATING_FALLBACK,
    ERROR_INSTANTIATING_LOGGER,
    HYSTRIX_NOT_PRESENT
}
