/**
 * 
 */
package org.seedstack.feign.internal;

import org.kametic.specifications.AbstractSpecification;
import org.seedstack.shed.reflect.ClassPredicates;

import feign.Target;

class FeignTargetInterfaceSpecification extends AbstractSpecification<Class<?>> {

    @Override
    public boolean isSatisfiedBy(Class<?> candidate) {
        return ClassPredicates
                .classIsDescendantOf(Target.class)
                .test(candidate);
    }

}
