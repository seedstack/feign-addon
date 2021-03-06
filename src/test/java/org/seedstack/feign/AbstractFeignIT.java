/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign;

import feign.FeignException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.feign.fixtures.FeignTestException;
import org.seedstack.feign.fixtures.Message;
import org.seedstack.feign.fixtures.TestContract;
import org.seedstack.feign.fixtures.TestInterceptor;
import org.seedstack.feign.fixtures.apis.*;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.Logging;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public abstract class AbstractFeignIT {
    @Logging
    private Logger logger;

    @Configuration("runtime.web.baseUrl")
    private URL baseUrl;

    @Inject
    private TestAPI testAPI;

    @Inject
    private TimeoutAPI timeoutAPI;

    @Inject
    private TestContractAPI contractAPI;

    @Inject
    private HystrixEnabledAPI hystrixEnabledAPI;

    @Inject
    private HystrixDisabledAPI hystrixDisabledAPI;

    @Inject
    private TargetableAPI targetableAPI;

    @Inject
    private ErrorDecoderTestAPI errorDecoderTestAPI;

    @Test
    public void feignClientIsInjectable() throws Exception {
        assertThat(testAPI).isNotNull();
    }

    @Test
    public void feignContractClientIsInjectable() throws Exception {
        assertThat(contractAPI).isNotNull();
    }

    @Test
    public void feignHystrixEnabledClientIsInjectable() throws Exception {
        assertThat(hystrixEnabledAPI).isNotNull();
    }

    @Test
    public void feignHystrixDisabledClientIsInjectable() throws Exception {
        assertThat(hystrixDisabledAPI).isNotNull();
    }

    @Test
    public void feignTargetableClientIsInjectable() throws Exception {
        assertThat(targetableAPI).isNotNull();
    }

    @Test
    public void testNominalCall() {
        Message message = testAPI.getMessage();
        assertThat(message.getBody()).isEqualTo("Hello World !");
        assertThat(message.getAuthor()).isEqualTo("computer");
        assertThat(TestInterceptor.called).isTrue();
    }

    @Test
    public void testProtectedCall() {
        Message message = testAPI.getProtectedMessage("dGVzdDp0ZXN0");
        assertThat(message.getBody()).isEqualTo("Hello World !");
        assertThat(message.getAuthor()).isEqualTo("computer");
    }

    @Test
    public void testProtectedCallInvalidAuthz() {
        Message message = testAPI.getProtectedMessage("xxx");
        assertThat(message.getBody()).isEqualTo("Fallback protected response");
        assertThat(message.getAuthor()).isEqualTo("fallback");
    }

    @Test
    public void testFallback() {
        Message message = testAPI.get404();
        assertThat(message.getBody()).isEqualTo("Error code: 404 !");
        assertThat(message.getAuthor()).isEqualTo("fallback");
    }

    @Test
    public void testTimeout() {
        logger.info("Issuing request");
        Message message = timeoutAPI.getMessage();
        logger.info("After timeout");
        assertThat(message.getBody()).isEqualTo("Fallback response after timeout");
        assertThat(message.getAuthor()).isEqualTo("fallback");
    }

    @Test
    public void testContractNominalCall() {
        Message message = contractAPI.getMessage();
        assertThat(message.getBody()).isEqualTo("Hello World !");
        assertThat(message.getAuthor()).isEqualTo("computer");
        assertThat(TestContract.hasBeenUsed()).isTrue();
    }

    @Test
    public void testHystrixEnabledNominalCall() {
        Message message = hystrixEnabledAPI.getMessage();
        assertThat(message.getBody()).isEqualTo("Hello World !");
        assertThat(message.getAuthor()).isEqualTo("computer");
    }

    @Test
    public void testHystrixDisabledNominalCall() {
        Message message = hystrixDisabledAPI.getMessage();
        assertThat(message.getBody()).isEqualTo("Hello World !");
        assertThat(message.getAuthor()).isEqualTo("computer");
    }

    @Test
    public void testTargetableNominalCall() {
        Message message = targetableAPI.getMessage();
        assertThat(message.getBody()).isEqualTo("I was routed trough a custom target");
        assertThat(message.getAuthor()).isEqualTo("or i thought so");
    }

    @Test
    public void testErrorDecoderAPI(){
        boolean exceptionHasBeenRaised=false;
        assertThat(errorDecoderTestAPI).isNotNull();
        try {
            errorDecoderTestAPI.fakeRequest();
        }
        catch(Exception e){
            assertThat(e.getCause()).isInstanceOf(FeignTestException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Feign addon unit test : received HTTP 404 error code");
            exceptionHasBeenRaised=true;
        }
        assertThat(exceptionHasBeenRaised).isTrue();
    }

}
