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
