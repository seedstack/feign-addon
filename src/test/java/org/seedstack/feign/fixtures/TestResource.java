/*
 * Copyright © 2013-2020, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.feign.fixtures;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

@Path("/feign")
public class TestResource {
    @Logging
    private Logger logger;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/message")
    public Message say() {
        return new Message("Hello World !", "computer");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/protected-message")
    public Message protectedSay() {
        return new Message("Hello World !", "computer");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/timeout")
    public Message timeout() {
        try {
            logger.info("Sleeping for 2 secs");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ignore
        }
        return new Message("bad!", "computer");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/target/message")
    public Message targetSay() {
        return new Message("I was routed trough a custom target", "or i thought so");
    }
}