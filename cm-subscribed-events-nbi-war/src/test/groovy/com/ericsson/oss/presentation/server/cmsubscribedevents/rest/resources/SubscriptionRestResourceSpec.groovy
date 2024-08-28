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
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService
import static org.junit.Assert.assertEquals;

import javax.inject.Inject

import static javax.ws.rs.core.Response.Status.BAD_REQUEST
import static javax.ws.rs.core.Response.Status.CREATED
import static javax.ws.rs.core.Response.Status.NO_CONTENT
import static javax.ws.rs.core.Response.Status.OK

class SubscriptionRestResourceSpec extends CdiSpecification {

    @ObjectUnderTest
    SubscriptionsRestResource resource

    @Inject
    SubscriptionService subscriptionService

    def "Check that the view all subscriptions when no subscriptions are present returns correct output"() {
        given: "using any Parameter Set"
            subscriptionService.viewAllSubscriptions() >> "[]"
            def response = resource.viewAllSubscriptions()

        when: "you get the response"
            String responseAsString = response.getEntity().toString()
            int responseStatus = response.getStatus()

        then: "the proper urls are returned in the response"
            responseStatus == NO_CONTENT.getStatusCode()
            responseAsString == "[]"
    }

    def "Check that the post url returns the correct output"() {
        given: "A user creates a subscription"
            final String validSubscription = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"999","objectClass":"/","objectInstance":"/"}}'
            final String expectedResponseMessageValidSubscription = validSubscription.replace('"id":"999""', '"id":"777"')
            subscriptionService.createSubscription(validSubscription) >> expectedResponseMessageValidSubscription

        when: "Subscription is created"
            def response = resource.postSubscription(validSubscription)
            String responseAsString = response.getEntity().toString()

        then: "the proper urls are returned in the response"
            responseAsString == expectedResponseMessageValidSubscription

    }

    def "Check that the post url handles general exceptions"() {
        given: "A user creates a subscription that causes an exception"
            final String exceptionCausingSubscription = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"999","objectClass":"/","objectInstance":"/"}}'
            subscriptionService.createSubscription(exceptionCausingSubscription) >> { throw new IOException() }

        when: "Subscription is not created"
            def response = resource.postSubscription(exceptionCausingSubscription)
            String responseAsString = response.getEntity().toString()
            int responseStatus = response.getStatus()

        then: "An error message is returned"
            exceptionCausingSubscription == responseAsString
            responseStatus == BAD_REQUEST.getStatusCode()
    }

    def "Check that the post url handles validation exceptions"() {
        given: "A user creates a subscription that causes an validation exception"
            final String invalidValidationSubscription = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"999","objectClass":"","objectInstance":"/"}}'
            def expectedValidationError = '{"errors":[{"errorMessage":"ntfSubscriptionControl.objectClass: must be a constant value /"}]}'

        when: "Subscription is not created"
            def response = resource.postSubscription(invalidValidationSubscription)
            String responseAsString = response.getEntity().toString()
            int responseStatus = response.getStatus()

        then: "An validation error message is returned"
            expectedValidationError == responseAsString
            responseStatus == BAD_REQUEST.getStatusCode()
    }

    def "Check the create Subscription handles the validation of valid notificationFilter property"() {
        given: "A user creates a subscription post body with a valid notificationFilter"
            final String validationSubscription = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"999","notificationFilter":\"' + validNotificationFilter + '\" ,"objectClass":"/","objectInstance":"/"}}'

        when: "The valid Subscription is sent to be created"
            int responseStatus = resource.postSubscription(validationSubscription).getStatus()

        then: "The subscription is created successfully"
            responseStatus == CREATED.getStatusCode()

        where:
            validNotificationFilter                             | _
            "//test1"                                           | _
            "     //test1"                                      | _
            "//test1     "                                      | _
            "     //test1     "                                 | _
            "//test1/"                                          | _
            "//test1/     "                                     | _
            "//test_1/"                                         | _
            "//test_1/test2"                                    | _
            "//test_1/test2/Test_3/"                            | _
            "//test_1/test2/Test_3/test4"                       | _
            "//test1/1test"                                     | _
            "//test1/1test/test"                                | _
            "//test1/1test/test | //test1/test2/TEST3"          | _
            "//test1/1test/test/ | //test1/test2/TEST3"         | _
            "//test1/1test/test/|//test1/test2/TEST3"           | _
            "//test1/1test/test|//test1/test2/TEST3"            | _
            "//test1/1test/test|//test1/test2/TEST3/"           | _
            "//test1/1test/test     |     //test1/test2/TEST3"  | _
            "//test1/1test/test     |     //test1/test2/TEST3/" | _
    }

    def "Check the create Subscription handles the validation of invalid notificationFilter property"() {
        given: "A user creates a subscription post body with an invalid notificationFilter"
            final String invalidValidationSubscription = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"999","notificationFilter":\"' + invalidNotificationFilter + '\" ,"objectClass":"/","objectInstance":"/"}}'

        when: "The invalid Subscription is sent to be created"
            def response = resource.postSubscription(invalidValidationSubscription)
            String responseAsString = response.getEntity().toString()
            int responseStatus = response.getStatus()

        then: "An validation error message is returned"
            def expectedValidationError = '{"errors":[{"errorMessage":"ntfSubscriptionControl.notificationFilter: does not match the regex pattern ^\\\\s*\\\\/\\\\/([A-Za-z0-9]_?\\\\/?(\\\\s*\\\\|\\\\s*\\\\/\\\\/[A-Za-z0-9]_?)?)+\\\\s*$"}]}'
            expectedValidationError == responseAsString
            responseStatus == BAD_REQUEST.getStatusCode()

        where:
            invalidNotificationFilter                                     | _
            ""                                                            | _
            " "                                                           | _
            "/"                                                           | _
            "//"                                                          | _
            "/test"                                                       | _
            "//@"                                                         | _
            "//test@"                                                     | _
            "//Test/ |"                                                   | _
            "//Test/ |      "                                             | _
            "//Test__test"                                                | _
            "//Test_test/Test__test/"                                     | _
            "//Test_test/Test__test/Test"                                 | _
            "//Test_test/Test/Test__test"                                 | _
            "//Test/test test"                                            | _
            "//Test/ ||"                                                  | _
            "//Test/ | /"                                                 | _
            "//Test/ | //"                                                | _
            "//Test/ | //test |test |"                                    | _
            "//Test///test"                                               | _
            "//Test/ | //test1/test2]"                                    | _
            "//Test/ | //test1/test2] //test"                             | _
            "//Test/ | //test1//    "                                     | _
            "//Test/ | //test1//test"                                     | _
            "//ManagedElement/EUtranCell222FDD2 | SubNetwork/SubNetwork/" | _
            null                                                          | _
    }

    def "Check that a view with a valid subscription ID returns a subscription"() {
        given: "A subscription exists"
            String validSubscription = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"999","objectClass":"/","objectInstance":"/"}}'
            subscriptionService.viewSubscription(999) >> validSubscription

        when: "A user requests the subscription specifying the subscription ID"
            def response = resource.viewSubscription(999)

        then: "Subscription information is returned"
            response.getEntity().toString() == validSubscription

        and: "Expected Status code is returned"
            response.getStatus() == OK.statusCode
    }

    def "Check that the delete url returns the correct output"() {
        given: "Subscription ID is valid"
            final int validId = 1

        when: "A user attempts to delete a subscription"
            def response = resource.deleteSubscription(validId)
            int responseStatus = response.getStatus()

        then: "The proper response is returned "
            responseStatus == NO_CONTENT.statusCode
    }

    def "Check that a view all subscriptions with subscriptions present will return the correct output"() {
        given: "Multiple Subscriptions exist"
            String validSubscriptionOne = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"888","objectClass":"/","objectInstance":"/"}}'
            String validSubscriptionTwo = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"995","objectClass":"/","objectInstance":"/"}}'
            String validSubscriptionThree = '{"ntfSubscriptionControl":{"notificationRecipientAddress":"https://site.com/eventListener/v10","id":"8","objectClass":"/","objectInstance":"/"}}'
            String subscriptions = "["+ validSubscriptionOne+ "," + validSubscriptionTwo+ ","+ validSubscriptionThree+ "]"
            subscriptionService.viewAllSubscriptions() >> subscriptions

        when: "A user requests just to view all subscriptions"
            def response = resource.viewAllSubscriptions()

        then: "All subscriptions with information should be returned"
            response.getStatus() == OK.statusCode
            assertEquals("The expected response message was not returned.", response.getEntity(), subscriptions)
    }

}
