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


import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

class RestErrorWrapperSpec extends Specification {
    String error1 = "Error 1"
    String error2 = "Error 2"

    def "Retrieving error information from a RestErrorWrapper"() {
        given: "A RestErrorWrapper contains error messages"
        RestErrorWrapper restErrorWrapper = new RestErrorWrapper(createPopulatedErrorMessageWrapperList())

        when: "A calling class requests all the errors"
        List<ErrorMessageWrapper> returnedErrorMessageWrapperList = restErrorWrapper.getErrors()

        then: "All the errors are returned"
        returnedErrorMessageWrapperList.get(0).getErrorMessage() == error1
        returnedErrorMessageWrapperList.get(1).getErrorMessage() == error2

    }

    def "Retrieving error information from a RestErrorWrapper in JSON Format"() {
        given: "A RestErrorWrapper contains error messages"
        RestErrorWrapper restErrorWrapper = new RestErrorWrapper(createPopulatedErrorMessageWrapperList())

        when: "A calling class requests all the errors in JSON"
        String returnedErrorMessagesInJSON = restErrorWrapper.asJsonString()

        then: "All the errors are returned in a JSON string"
        returnedErrorMessagesInJSON == '{"errors":[{"errorMessage":"' + error1 + '"},{"errorMessage":"' + error2 + '"}]}'
    }

    def "Object mapper throws an error when trying to retrieve errors"() {
        given: "Object mapper throws an exception when called"
        RestErrorWrapper.FactoryHelper mockFactoryHelper = Mock(RestErrorWrapper.FactoryHelper)
        ObjectMapper mockMapper = Mock(ObjectMapper)
        mockMapper.writeValueAsString(_) >> { throw new IOException() }
        mockFactoryHelper.retrieveMapper() >> { return mockMapper }
        List<ErrorMessageWrapper> errorMessageWrapperList = createPopulatedErrorMessageWrapperList()
        RestErrorWrapper restErrorWrapper = new RestErrorWrapper(errorMessageWrapperList, mockFactoryHelper)

        when: "Retrieval of error messages is executed"
        restErrorWrapper.asJsonString()

        then: "Exception is thrown"
        thrown(CmSubscribedEventsNbiException)
    }

    private ArrayList<ErrorMessageWrapper> createPopulatedErrorMessageWrapperList() {
        ErrorMessageWrapper errorMessageWrapper1 = new ErrorMessageWrapper(error1)
        ErrorMessageWrapper errorMessageWrapper2 = new ErrorMessageWrapper(error2)
        List<ErrorMessageWrapper> errorMessageWrapperList = new ArrayList<ErrorMessageWrapper>()
        errorMessageWrapperList.add(errorMessageWrapper1)
        errorMessageWrapperList.add(errorMessageWrapper2)
        return errorMessageWrapperList
    }
}
