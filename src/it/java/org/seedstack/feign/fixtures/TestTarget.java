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

import org.seedstack.coffig.Coffig;
import org.seedstack.feign.fixtures.apis.TargetableAPI;
import org.seedstack.seed.Application;

import feign.Target.HardCodedTarget;

public class TestTarget extends HardCodedTarget<TargetableAPI> {

    @Inject
    public TestTarget(Application application) {
        super(TargetableAPI.class, "http://localhost:9090/feign/target/");

        Coffig coffig = application.getConfiguration();

        System.err.println("port" + application.getConfiguration(TestTarget.class).get("port"));

        System.err.println(coffig.get(String.class, "test.port"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "TestTarget []" + super.toString();
    }

}
