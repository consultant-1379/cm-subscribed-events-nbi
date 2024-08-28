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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException
import com.ericsson.oss.presentation.server.cmsubscribedevents.validation.ErrorMessageWrapper
import com.ericsson.oss.presentation.server.cmsubscribedevents.validation.RestErrorWrapper
import com.ericsson.oss.presentation.server.events.licensing.LicenseValidator
import com.ericsson.oss.presentation.server.rest.resources.EventRootElement

import javax.ws.rs.core.Response

import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SELF_PROPERTY
import static javax.ws.rs.core.Response.Status.BAD_REQUEST
import static javax.ws.rs.core.Response.Status.FORBIDDEN
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR
import static javax.ws.rs.core.Response.Status.NO_CONTENT
import static javax.ws.rs.core.Response.Status.OK
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED

class SubscriptionEntryPointSpec extends CdiSpecification {
    public static final String BAD_REQUEST_SUBSCRIPTION_CLAUSE_COMBINATION_ATTRIBUTES = "Subscription is not correctly structured. Please refer to specification documentation."

    @ObjectUnderTest
    SubscriptionEntryPoint entry

    @MockedImplementation
    LicenseValidator licenseValidator

    @MockedImplementation
    SubscriptionsRestResource subscriptionsRestResource


    def "When a user with valid licenses calls the view all subscriptions rest endpoint with subscriptions present a successful response is returned"() {
        given: "A user has valid licenses for cm-subscribed events"
            licenseValidator.isLicenseInvalid() >> false
            final EventRootElement root = new EventRootElement()
            root.addLink(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS, SELF_PROPERTY)
            subscriptionsRestResource.viewAllSubscriptions() >> { return Response.ok().entity(root.getAsMap()).build() }

        when: "A user with a valid license calls the subscription endpoint"
            final Response response = entry.viewAllSubscriptions()

        then: "Response is successful"
            response.getStatus() == OK.getStatusCode()

    }

    def "When a user with valid licenses calls the view all subscriptions rest endpoint with no subscriptions present a successful response is returned"() {
        given: "A user has valid licenses for cm-subscribed events"
            licenseValidator.isLicenseInvalid() >> false
            final EventRootElement root = new EventRootElement()
            root.addLink(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS, SELF_PROPERTY)
            subscriptionsRestResource.viewAllSubscriptions() >> { return Response.noContent().entity(root.getAsMap()).build() }

        when: "A user with a valid license calls the subscription endpoint"
            final Response response = entry.viewAllSubscriptions()

        then: "NO_CONTENT 204 Response returned"
            response.getStatus() == NO_CONTENT.getStatusCode()

    }

    def "When a user with invalid licenses calls the view all subscriptions rest endpoint a forbidden response is returned"() {
        given: "A user has invalid licenses for cm-subscribed events"
            licenseValidator.isLicenseInvalid() >> true

        when: "A user with a invalid license calls the subscription endpoint"
            final Response response = entry.viewAllSubscriptions()

        then: "Response is forbidden"
            response.getStatus() == FORBIDDEN.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"No license active for this feature, please contact the system administrator.\"}]}"

    }

    def "When a user with invalid role calls the view all subscriptions rest endpoint an unauthorized response is returned"() {
        given: "A user does not have correct role to request subscription"
            subscriptionsRestResource.viewAllSubscriptions() >> { throw new SecurityViolationException(null) }

        when: "A user calls the subscription endpoint"
            final Response response = entry.viewAllSubscriptions()

        then: "Response is unauthorized"
            response.getStatus() == UNAUTHORIZED.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"Security Violation. Access not allowed. Please refer to specification documentation.\"}]}"

    }

    def "When a user calls the view all subscriptions rest endpoint an IO Exception is thrown"() {
        given: "A subscription request causes an IO Exception"
            subscriptionsRestResource.viewAllSubscriptions() >> { throw new IOException() }

        when: "A user calls the view subscription endpoint"
            final Response response = entry.viewAllSubscriptions()

        then: "Internal Server error is returned"
            response.getStatus() == INTERNAL_SERVER_ERROR.getStatusCode()
    }

    def "When a user calls the view subscriptions rest endpoint with a valid subscriptionID a subscription is returned"() {
        given: "A system has a valid license"
            licenseValidator.isLicenseInvalid() >> false

        and: "The system has a subscription created"
            final Integer subscriptionId = 1
            subscriptionsRestResource.viewSubscription(subscriptionId) >> {
                Response
                        .status(OK)
                        .entity("SubscriptionJSON")
                        .build()
            }

        when: "A user requests a subscription with the id of an existing subscription"
            final Response response = entry.viewSubscriptionById(subscriptionId)

        then: "Response is successful"
            response.getStatus() == OK.getStatusCode()
    }

    def "When a user attempts to POST an empty subscription, they will get an error in response notifying that the JSON is incorrectly structured"() {
        given: "An array of errors will be returned to the Entry Point"
            licenseValidator.isLicenseInvalid() >> false
            final ErrorMessageWrapper errorMessage = new ErrorMessageWrapper(BAD_REQUEST_SUBSCRIPTION_CLAUSE_COMBINATION_ATTRIBUTES)
            final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>(Arrays.asList(errorMessage))
            final RestErrorWrapper error = new RestErrorWrapper(errors)
            subscriptionsRestResource.postSubscription("") >> { return Response.status(BAD_REQUEST).entity(error.asJsonString()).build() }

        when: "A user calls the POST subscription endpoint with no JSON structure"
            final Response response = entry.postSubscription("")

        then: "Bad Request 400 Response with Error message returned"
            String expectedResponse = "{\"errors\":[{\"errorMessage\":\"" + BAD_REQUEST_SUBSCRIPTION_CLAUSE_COMBINATION_ATTRIBUTES + "\"}]}"
            response.getStatus() == BAD_REQUEST.getStatusCode()
            response.getEntity() == expectedResponse
    }

    def "When a user attempts to POST a valid Subscription, they receive the Subscription back with a dynamically generated Subscription ID"() {
        given: "Correctly structured JSON for Subscription is sent in POST request"
            licenseValidator.isLicenseInvalid() >> false
            String dummyId = "999"
            String jsonSent = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://site.com/eventListener/v10\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":3},\"id\":\"" + dummyId + "\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}"
            String subscriptionReturned = "\"{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://site.com/eventListener/v10\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":6},\"id\":\"1\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}\""
            subscriptionsRestResource.postSubscription(jsonSent) >> Response.status(OK).entity(subscriptionReturned).build()

        when: "A user calls the POST subscription endpoint with their JSON"
            final Response response = entry.postSubscription(jsonSent)

        then: "OK 200 Response returned with Subscription"
            response.getStatus() == OK.getStatusCode()
            response.getEntity() == subscriptionReturned
            !response.getEntity().toString().contains(dummyId)
    }

    def "When a user with invalid licenses calls the create subscriptions rest endpoint a forbidden response is returned"() {
        given: "A user has invalid licenses for cm-subscribed events"
            licenseValidator.isLicenseInvalid() >> true
            String dummyId = "999"
            String jsonSent = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":\"https://site.com/eventListener/v10\",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":3},\"id\":\"" + dummyId + "\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}"

        when: "A user with a invalid license calls the subscription endpoint"
            final Response response = entry.postSubscription(jsonSent)

        then: "Response is forbidden"
            response.getStatus() == FORBIDDEN.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"No license active for this feature, please contact the system administrator.\"}]}"
    }

    def "When a user with invalid role calls the create subscription rest endpoint an unauthorized response is returned"() {
        given: "A user does not have correct role to request subscription"
            subscriptionsRestResource.postSubscription(null) >> { throw new SecurityViolationException(null) }

        when: "A user calls the subscription endpoint"
            final Response response = entry.postSubscription(null)

        then: "Response is unauthorized"
            response.getStatus() == UNAUTHORIZED.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"Security Violation. Access not allowed. Please refer to specification documentation.\"}]}"

    }

    def "When a user with invalid role calls the view subscription rest endpoint an unauthorized response is returned"() {
        given: "A user does not have correct role to request subscription"
            subscriptionsRestResource.viewSubscription(1) >> { throw new SecurityViolationException(null) }

        when: "A user calls the subscription endpoint"
            final Response response = entry.viewSubscriptionById(1)

        then: "Response is unauthorized"
            response.getStatus() == UNAUTHORIZED.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"Security Violation. Access not allowed. Please refer to specification documentation.\"}]}"

    }

    def "When a user with invalid licenses calls the view subscription rest endpoint a forbidden response is returned"() {
        given: "A user has invalid licenses for cm-subscribed events"
            licenseValidator.isLicenseInvalid() >> true

        when: "A user with a invalid license calls the view subscription endpoint"
            final Response response = entry.viewSubscriptionById(1)

        then: "Response is forbidden"
            response.getStatus() == FORBIDDEN.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"No license active for this feature, please contact the system administrator.\"}]}"
    }

    def "When a user calls view subscription an IO Exception is thrown"() {
        given: "A subscription request causes an IO Exception"
            subscriptionsRestResource.viewSubscription(1) >> { throw new IOException() }

        when: "A user calls the view subscription endpoint"
            entry.viewSubscriptionById(1)

        then: "IOException is thrown"
            thrown(IOException)
    }

    def "When a user calls post subscription an IO Exception is thrown"() {
        given: "A subscription request causes an IO Exception"
            subscriptionsRestResource.postSubscription(_) >> { throw new IOException() }

        when: "A user calls the view subscription endpoint"
            final Response response = entry.postSubscription("SubscriptionText")

        then: "Internal Server error is returned"
            response.getStatus() == INTERNAL_SERVER_ERROR.getStatusCode()
    }

     def "When a user attempts to DELETE a valid Subscription, they receive a successful response "() {
        given: "The Subscription ID is valid"
            licenseValidator.isLicenseInvalid() >> false
            int validID = 1
            subscriptionsRestResource.deleteSubscription(validID) >>  Response.status(NO_CONTENT).build()

        when: "A user calls the Delete subscription endpoint with their Subscription ID"
            final Response response = entry.deleteSubscription(validID)

        then: "NO_CONTENT 204 Response returned"
            response.getStatus() == NO_CONTENT.getStatusCode()
    }

    def "When a user attempts to DELETE a subscription with ID <= 0, they will get an error in response notifying that ID can't be <=0"() {
        given: "An array of errors will be returned to the Entry Point"
            licenseValidator.isLicenseInvalid() >> false
            final ErrorMessageWrapper errorMessage = new ErrorMessageWrapper(BAD_REQUEST_SUBSCRIPTION_CLAUSE_COMBINATION_ATTRIBUTES)
            final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>(Arrays.asList(errorMessage))
            final RestErrorWrapper error = new RestErrorWrapper(errors)
            subscriptionsRestResource.deleteSubscription(a) >> { return Response.status(BAD_REQUEST).entity(error.asJsonString()).build() }

        when: "A user calls the DELETE subscription endpoint with an ID less then 1"
            final Response response = entry.deleteSubscription(a)

        then: "Bad Request 400 Response with Error message returned"
            String expectedResponse = "{\"errors\":[{\"errorMessage\":\"" + BAD_REQUEST_SUBSCRIPTION_CLAUSE_COMBINATION_ATTRIBUTES + "\"}]}"
            response.getStatus() == BAD_REQUEST.getStatusCode()
            response.getEntity() == expectedResponse

        where:
            a  || _
            0  || _
            -1 || _

    }

    def "When a user with invalid licenses calls the delete subscriptions rest endpoint a forbidden response is returned"() {
        given: "A user has invalid licenses for cm-subscribed events"
            licenseValidator.isLicenseInvalid() >> true
            final int dummyInt = 1

        when: "A user with a invalid license calls the subscription endpoint"
            final Response response = entry.deleteSubscription(dummyInt)

        then: "Response is forbidden"
            response.getStatus() == FORBIDDEN.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"No license active for this feature, please contact the system administrator.\"}]}"
    }

    def "When a user with invalid role calls the delete subscriptions rest endpoint an unauthorized response is returned"() {
        given: "A user does not have correct role to delete subscription"
            subscriptionsRestResource.deleteSubscription(1) >> { throw new SecurityViolationException(null) }

        when: "A user calls the subscription endpoint"
            final Response response = entry.deleteSubscription(1)
        
        then: "Response is unauthorized"
            response.getStatus() == UNAUTHORIZED.getStatusCode()
            response.entity == "{\"errors\":[{\"errorMessage\":\"Security Violation. Access not allowed. Please refer to specification documentation.\"}]}"

    }

    def"When a user calls the delete subscription an IO Exception is thrown"(){
        given: "A subscription request causes an IO Exception"
            subscriptionsRestResource.deleteSubscription(_ as Integer) >> { throw new IOException() }

        when: "A user calls the view subscription endpoint"
            final Response response = entry.deleteSubscription(1)

        then: "Internal Server error is returned"
            response.getStatus() == INTERNAL_SERVER_ERROR.getStatusCode()
    }

}
