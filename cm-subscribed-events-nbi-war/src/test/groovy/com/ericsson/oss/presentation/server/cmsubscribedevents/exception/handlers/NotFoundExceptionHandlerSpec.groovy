/*
 * ------------------------------------------------------------------------------
 * *****************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *   The copyright to the computer program(s) herein is the property of
 *   Ericsson Inc. The programs may be used and/or copied only with written
 *   permission from Ericsson Inc. or in accordance with the terms and
 *   conditions stipulated in the agreement/contract under which the
 *   program(s) have been supplied.
 *   ******************************************************************************
 *   ------------------------------------------------------------------------------
 */

package com.ericsson.oss.presentation.server.cmsubscribedevents.exception.handlers


import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.presentation.server.cmsubscribedevents.rest.exception.handlers.NotFoundExceptionHandler

import javax.ws.rs.NotFoundException
import javax.ws.rs.core.Response

import static javax.ws.rs.core.Response.Status.NOT_FOUND

class NotFoundExceptionHandlerSpec extends CdiSpecification {
    @ObjectUnderTest
    NotFoundExceptionHandler notFoundExceptionHandler

    def "Error message output from NotFoundExceptionHandler contains the expected content"() {
        given: "NotFoundExceptionHandler exists"
        when: "A NotFoundException is handled"
        Response response = notFoundExceptionHandler.toResponse(new NotFoundException())
        then: "Response is returned with 404 status"
        response.getStatus() == NOT_FOUND.statusCode
        and: "Error message is included in the response body in json format"
        response.getEntity() == "{\"errors\":[{\"errorMessage\":\"Resource could not be found.\"}]}"
    }
}