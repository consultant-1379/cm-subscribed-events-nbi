/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 *   The copyright to the computer program(s) herein is the property of
 *   Ericsson Inc. The programs may be used and/or copied only with written
 *   permission from Ericsson Inc. or in accordance with the terms and
 *   conditions stipulated in the agreement/contract under which the
 *   program(s) have been supplied.
 *   ******************************************************************************
 *   ------------------------------------------------------------------------------
 */

package com.ericsson.oss.presentation.server.cmsubscribedevents.validation

import spock.lang.Specification

class CmSubscribedNbiExceptionSpec extends Specification{
    def "Retrieve error message output from CmSubscribedNbiException"(){
        given: "A CmSubscribedNbiException is created"
        CmSubscribedEventsNbiException cmSubscribedEventsNbiException = new CmSubscribedEventsNbiException("myMessage", new IOException("otherMessage"))
        when: "When a calling class reads the message"
        def message = cmSubscribedEventsNbiException.getMessage()
        then: "The exception message is reported"
        message == "myMessage"
    }
}
