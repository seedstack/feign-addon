/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/**
 *
 */

package org.seedstack.feign.fixtures;

import feign.Target.HardCodedTarget;
import javax.inject.Inject;
import org.seedstack.feign.fixtures.apis.TargetableAPI;
import org.seedstack.seed.Application;

public class TestTarget extends HardCodedTarget<TargetableAPI> {

    @Inject
    public TestTarget(Application application) {
        super(TargetableAPI.class, String.format("%sfeign/target/",
                application.getConfiguration().getMandatory(String.class,
                        "web.runtime.baseUrl")));
    }

    @Override
    public String toString() {
        return "TestTarget []" + super.toString();
    }

}
