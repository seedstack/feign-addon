/**
 * 
 */
package org.seedstack.feign.internal;

import java.util.Set;

import javax.inject.Inject;

import feign.Target;

/**
 * @author xiabou
 *
 */
public class FeignTargetContainer {

    @Inject
    private Set<Target> targetClasses;
   
    
    public void tryInjetion() {
        System.err.println(targetClasses);
    }
}
