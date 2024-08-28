/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.presentation.server.cmsubscribedevents.rest.resources

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification

class JaxRsActivatorSpec extends CdiSpecification {

    @ObjectUnderTest
    JaxRsActivator jaxRsActivator

    def "When JAX-RS is called Subscription Entry Point and Subscription Resource are returned "() {
        when: "JAX-RS Activator is called"
        def resources = jaxRsActivator.getClasses()

        then: "Subscription Entry Point and Subscription Resource are returned"
        resources.size() == 2;
        resources.iterator().next().getClass().isInstance(SubscriptionEntryPoint)
        resources.iterator().next().getClass().isInstance(SubscriptionsRestResource)
    }
}
