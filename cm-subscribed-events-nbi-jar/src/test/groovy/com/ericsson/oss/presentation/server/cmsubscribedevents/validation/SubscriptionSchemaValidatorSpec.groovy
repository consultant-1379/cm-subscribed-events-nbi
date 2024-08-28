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
package com.ericsson.oss.presentation.server.cmsubscribedevents.validation

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.recording.CommandPhase
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder
import com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil

import javax.inject.Inject

import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.CM_SUBSCRIBED_EVENTS_NBI
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.INVALID_JSON_FORMAT_ERROR
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIPTIONS
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIPTION_POST_MUST_CONTAIN_BODY

class SubscriptionSchemaValidatorSpec extends CdiSpecification {

    @ObjectUnderTest
    SubscriptionSchemaValidator subscriptionSchemaValidator

    @Inject
    SystemRecorder systemRecorder

    def "When a Subscription is created with an invalid JSON format, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with an invalid JSON format"
        String subscription = "{x}"
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()
        String expectedError = INVALID_JSON_FORMAT_ERROR

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error is reported"
        inValidSubscription
        expectedError == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                INVALID_JSON_FORMAT_ERROR)
    }

    def "When a Subscription is created with null or invalid values for mandatory values, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with null or invalid values for mandatory values"
        String subscription = createInvalidSubscriptionWithNullValues(notificationRecipientAddr, objectInst, objectClass, scopeType, id)
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error is reported"
        inValidSubscription
        expectedErrorListContents == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)

        where:
        notificationRecipientAddr            | objectInst | objectClass | scopeType  | id || expectedErrorListContents
        null                                 | "/"        | "/"         | "BASE_ALL" | 0  || "ntfSubscriptionControl.notificationRecipientAddress: is missing but it is required"
        "https://site.com/eventListener/v10" | null       | "/"         | "BASE_ALL" | 0  || "ntfSubscriptionControl.objectInstance: is missing but it is required"
        "https://site.com/eventListener/v10" | "/"        | null        | "BASE_ALL" | 0  || "ntfSubscriptionControl.objectClass: is missing but it is required"
        "https://site.com/eventListener/v10" | "/"        | "/"         | null       | 0  || "ntfSubscriptionControl.scope.scopeType: is missing but it is required"
        "https://site.com/eventListener/v10" | "/"        | "/"         | "BASE_ALL" | -1 || "ntfSubscriptionControl.id: is missing but it is required"
    }

    def "When a Subscription is created with invalid values, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with invalid NtfSubscriptionControl value"

        String[] arr = new String[notificationType.size()]
        arr = notificationType.toArray(arr)

        String subscription = createInvalidSubscription(notificationRecipientAddr, objectInst, objectClass, scopeLevel, scopeType, arr)
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error is reported"
        inValidSubscription
        expectedErrorListContents == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)

        where:
        notificationRecipientAddr            | objectInst | objectClass | scopeType  | scopeLevel |  notificationType                       || expectedErrorListContents
        null                                 | "/"        | "/"         | "BASE_ALL" | 0          | ["notifyMOICreation"]                   || "ntfSubscriptionControl.notificationRecipientAddress: must be at least 1 characters long"
        "https://site.com/eventListener/v10" | ""         | "/"         | "BASE_ALL" | 0          | ["notifyMOICreation"]                   || "ntfSubscriptionControl.objectInstance: must be a constant value /"
        "https://site.com/eventListener/v10" | "/"        | ""          | "BASE_ALL" | 0          | ["notifyMOICreation"]                   || "ntfSubscriptionControl.objectClass: must be a constant value /"
        "https://site.com/eventListener/v10" | "/"        | "/"         | null       | 0          | ["notifyMOICreation"]                   || "ntfSubscriptionControl.scope.scopeType: does not have a value in the enumeration [BASE_ALL]"
        "https://site.com/eventListener/v10" | "/"        | "/"         | "BASE_ALL" | -1         | ["notifyMOICreation"]                   || "ntfSubscriptionControl.scope.scopeLevel: must have a minimum value of 0"
        "https://site.com/eventListener/v10" | "/"        | "/"         | "BASE_ALL" | 0          | ["invalid"]                             || "ntfSubscriptionControl.notificationTypes[0]: does not have a value in the enumeration [notifyMOIChanges, notifyMOICreation, notifyMOIDeletion, notifyMOIAttributeValueChanges]"
        "https://site.com/eventListener/v10" | "/"        | "/"         | "BASE_ALL" | 0          | []                                      || "ntfSubscriptionControl.notificationTypes: there must be a minimum of 1 items in the array"
        "https://site.com/eventListener/v10" | "/"        | "/"         | "BASE_ALL" | 0          | ["notifyMOIChanges","notifyMOIChanges"] || "ntfSubscriptionControl.notificationTypes: the items in the array must be unique"
    }

    def "When a Subscription is created with valid fields and values, no error(s) will be reported"() {
        given: "A user tries to create a Subscription with valid NtfSubscriptionControl values"
        String subscription = createValidSubscription()
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "No Subscription error is reported"
        !inValidSubscription
        errors.isEmpty()
        0 * systemRecorder.recordCommand(_)
    }

    def "When a Subscription is created with valid fields and no scope, no error(s) will be reported"() {
        given: "A user tries to create a Subscription with valid NtfSubscriptionControl values"
        String subscription = createValidSubscriptionWithNoScope()
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "No Subscription error is reported"
        !inValidSubscription
        errors.isEmpty()
        0 * systemRecorder.recordCommand(_)

    }

    def "When a Subscription is created with different scopeType and scopeLevel, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with different scopeType's and scopeLevel's"
        String subscription = createSubscriptionWithCustomScope(scopeType, scopeLevel)
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Validation returns the expected result"
        inValidSubscription == invalid

        and: "Error is logged to log viewer"
        errorWrittenToLogViewer * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)

        where:
        scopeType  | scopeLevel | invalid | errorWrittenToLogViewer
        "BASE_ALL" | 0          | false   | 0
        "BASE_ALL" | 1          | true    | 1
        "BASE_ALL" | 2          | true    | 1
    }

    def "When a Subscription is created with an empty scope, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with scope with no content"
        final String subscription = createInvalidSubscriptionWithNoScopeValue()
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error is reported"
        inValidSubscription
        "ntfSubscriptionControl.scope.scopeType: is missing but it is required" == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)
    }

    def "When a Subscription is created with no ntfSubscriptionControl, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with missing ntfSubscriptionControl"
        final String subscription = '{}'
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error is reported"
        inValidSubscription
        "ntfSubscriptionControl: is missing but it is required" == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)

    }

    def "When a Subscription is created with additional entries in the ntfSubscriptionControl, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with unsupported values within the ntfSubscriptionControl"

        String subscription = createInvalidSubscriptionWithAdditionalNtfSubscriptionValue()
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error is reported"
        final String expectedErrorMessage = "ntfSubscriptionControl.invalidParameter: is not defined in the schema and the schema does not allow additional properties"
        inValidSubscription
        expectedErrorMessage == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)

    }

    def "When a Subscription is created with scope containing additional entries, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with unsupported values within the scope"

        String subscription = createInvalidSubscriptionWithAdditionalScopeValue()
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error is reported"
        final String expectedErrorMessage = "ntfSubscriptionControl.scope.scopeInValidParam: is not defined in the schema and the schema does not allow additional properties"
        inValidSubscription
        expectedErrorMessage == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)
    }

    def "When a Subscription is created with an empty body, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with an empty body"
        String subscriptionWithoutBody = ""
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validation is called to check the Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscriptionWithoutBody, errors)
        then: "Subscription error is reported"
        inValidSubscription
        SUBSCRIPTION_POST_MUST_CONTAIN_BODY == errors.get(0).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                SUBSCRIPTION_POST_MUST_CONTAIN_BODY)

    }

    def "When a Subscription is created with multiple errors, appropriate error(s) will be reported"() {
        given: "A user tries to create a Subscription with multiple unsupported values"
        final String subscription = createInvalidSubscriptionWithMultipleErrors()
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean inValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error(s) is reported"
        final String expectedFirstErrorMessage = "ntfSubscriptionControl.objectInstance: must be a constant value /"
        final String expectedSecondErrorMessage = "ntfSubscriptionControl.scope.scopeLevel: must have a maximum value of 0"
        inValidSubscription
        expectedFirstErrorMessage == errors.get(0).getErrorMessage()
        expectedSecondErrorMessage == errors.get(1).getErrorMessage()

        and: "Error is logged to log viewer"
        1 * systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS)
    }

    def "When a Subscription is sent containing duplicate keys, validation error thrown" () {
        given: "A user tries to create a Subscription with duplicated key"
        final String subscription = createInvalidSubscriptionWithDuplicateKeys()
        final List<ErrorMessageWrapper> errors = new ArrayList<ErrorMessageWrapper>()

        when: "Validator is called to check Subscription"
        boolean isValidSubscription = subscriptionSchemaValidator.isInvalidSubscription(subscription, errors)

        then: "Subscription error of duplicated keys is caught and reported"
        final String expectedErrorMessage = INVALID_JSON_FORMAT_ERROR
        isValidSubscription
        expectedErrorMessage == errors.get(0).getErrorMessage()
    }

    private static String createInvalidSubscriptionWithNullValues(notificationRecipientAddr, objectInst, objectClass, scopeType, id) {
        String scope
        if (scopeType == null) {
            scope = String.format("\"scope\":{\"scopeLevel\":0}")
        } else {
            scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":0}", scopeType)
        }

        String subscription
        if (notificationRecipientAddr == null) {
            subscription = String.format("%s", scope)
        } else {
            subscription = String.format("\"notificationRecipientAddress\":\"%s\",%s", notificationRecipientAddr, scope)
        }

        if (objectClass != null) {
            subscription = subscription + "," + String.format("\"objectClass\":\"%s\"", objectClass)
        }
        if (objectInst != null) {
            subscription = subscription + "," + String.format("\"objectInstance\":\"%s\"", objectInst)
        }

        if (id == 0) {
            subscription = subscription + "," + String.format("\"id\":\"%s\"", id)
        }

        return String.format("{\"ntfSubscriptionControl\":{%s}}", subscription)
    }

    private static String createInvalidSubscription(notificationRecipientAddr, objectInst, objectClass, scopeLevel, scopeType, String [] notificationTypes) {

        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d}", scopeType, scopeLevel)
        String address = notificationRecipientAddr == null ? "\"notificationRecipientAddress\":\"\"" : String.format("\"notificationRecipientAddress\":\"%s\"", notificationRecipientAddr)
        String subscription = String.format("%s,%s", address, scope)

        if (notificationTypes.length > 0){
            subscription = subscription + "," + String.format("\"notificationTypes\":[")
            for(int i = 0; i < notificationTypes.length; i++){
                subscription = subscription + String.format("\"%s\"", notificationTypes[i])
                if(i < notificationTypes.length-1){
                    subscription = subscription + ","
                }
            }
            subscription = subscription + String.format("]")
        } else {
            subscription = subscription + "," + String.format("\"notificationTypes\":[]")
        }

        if (objectClass != null) {
            subscription = subscription + "," + String.format("\"objectClass\":\"%s\"", objectClass)
        }
        if (objectInst != null) {
            subscription = subscription + "," + String.format("\"objectInstance\":\"%s\"", objectInst)
        }
        return String.format("{\"ntfSubscriptionControl\":{\"id\":\"1\",%s}}", subscription)
    }

    private static String createValidSubscription() {
        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d}", "BASE_ALL", 0)
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"notificationTypes\":[\"%s\"],\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}", "https://site.com/eventListener/v10", 1, "notifyMOICreation", "/", "/", scope)
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }

    private static String createInvalidSubscriptionWithMultipleErrors() {
        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d}", "BASE_ALL", 9)
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}", "https://site.com/eventListener/v10", 1, "/", "", scope)
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }

    private static String createInvalidSubscriptionWithAdditionalNtfSubscriptionValue() {
        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d}", "BASE_ALL", 0)
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"invalidParameter\":\"%s\",\"id\":\"%d\",\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}", "https://site.com/eventListener/v10", "xxx", 1, "/", "/", scope)
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }

    private static String createInvalidSubscriptionWithAdditionalScopeValue() {
        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d,\"scopeInValidParam\":%d}", "BASE_ALL", 0, 9)
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}", "https://site.com/eventListener/v10", 1, "/", "/", scope)
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }

    private static String createInvalidSubscriptionWithNoScopeValue() {
        String scope = String.format("\"scope\":{}",)
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}", "https://site.com/eventListener/v10", 1, "/", "/", scope)
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }

    private static String createValidSubscriptionWithNoScope() {
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"objectClass\":\"%s\",\"objectInstance\":\"%s\"}", "https://site.com/eventListener/v10", 1, "/", "/")
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }


    private static String createSubscriptionWithCustomScope(scopeType, scopeLevel) {
        String scope = String.format("\"scope\":{\"scopeType\":\"%s\",\"scopeLevel\":%d}", scopeType, scopeLevel)
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"objectClass\":\"%s\",\"objectInstance\":\"%s\",%s}", "https://site.com/eventListener/v10", 1, "/", "/", scope)
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }

    private static String createInvalidSubscriptionWithDuplicateKeys() {
        String subscription = String.format("\"notificationRecipientAddress\":\"%s\",\"notificationRecipientAddress\":\"%s\",\"id\":\"%d\",\"objectClass\":\"%s\",\"objectInstance\":\"%s\"}", "https://site.com/eventListener/v10", "https://site.com/eventListener/v10", 1, "/", "/")
        return String.format("{\"ntfSubscriptionControl\":{%s}", subscription)
    }
}
