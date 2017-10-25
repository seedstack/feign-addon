/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 * 
 */
package org.seedstack.feign.fixtures;

import javax.inject.Inject;

import org.seedstack.feign.fixtures.apis.TargetableAPI;
import org.seedstack.seed.Application;

import feign.Target.HardCodedTarget;

public class TestTarget extends HardCodedTarget<TargetableAPI> {

    @Inject
    private Application application;

    public TestTarget() {
        super(TargetableAPI.class, "http://localhost:9090/feign/target/");

        System.err.println(application);

        /*System.err
                .println(application.getConfiguration().get(String.class, "sys.tomcat.http.port"));*/

    }

}
