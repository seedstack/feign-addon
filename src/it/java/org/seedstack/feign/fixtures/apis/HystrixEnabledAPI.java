/**
 * 
 */
package org.seedstack.feign.fixtures.apis;

import org.seedstack.feign.FeignApi;
import org.seedstack.feign.fixtures.Message;

import feign.RequestLine;


@FeignApi
public interface HystrixEnabledAPI {

    @RequestLine("GET /message")
    Message getMessage();

}
