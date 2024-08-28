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
package com.ericsson.oss.presentation.server.cmsubscribedevents.utils;

/**
 * Contains static end-point and resources
 */
public final class CmSubscribedEventsNbiConstantsUtil {

    public static final String SUBSCRIPTION_END_POINT = "/subscriptions";

    public static final String CM_SUBSCRIBED_EVENTS_NBI_RESOURCE = "cm-subscribed-events-nbi";

    /**
     * NBI Permissions (RBAC).
     */
    public static final String RBAC_READ_PERMISSION = "read";

    public static final String RBAC_CREATE_PERMISSION = "create";

    public static final String RBAC_DELETE_PERMISSION = "delete";

    /**
     * HAL+JSON.
     */
    public static final String SELF_PROPERTY = "self";

    public static final String SUBSCRIBED_EVENTS_V_1 = "subscribed-events/v1";

    public static final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS = "/cm/subscribed-events/v1/subscriptions";

    /**
     * System Recorder fields
     */

    public static final String CM_SUBSCRIBED_EVENTS_NBI = "cm-subscribed-events-nbi";

    public static final String SUBSCRIPTIONS = "subscriptions";

    /**
     * Error responses
     */

    public static final String RECEIVED_VIEW_SUBSCRIPTION_REQUEST_FOR_ID = "Received view subscription request for id = {}";

    public static final String TEXT_LOG_SECURITY_VIOLATION = "Received SecurityViolationException = ";

    public static final String RESOURCE_COULD_NOT_BE_FOUND = "Resource could not be found.";

    public static final String SUBSCRIPTION_POST_MUST_CONTAIN_BODY = "Subscription POST must contain body.";

    public static final String UNEXPECTED_ERROR_OCCURRED = "Internal Server Error =";

    public static final String INVALID_JSON_FORMAT_ERROR = "Invalid JSON format error";

    public static final String VALIDATION_ERRORS = "Validation errors";

    public static final String HTTPS_CONFIGURATION_ERROR = "HTTPS configuration error, unable to retrieve keystore.";

    /**
     * End-points and resources.
     */
    private CmSubscribedEventsNbiConstantsUtil() {
    }

}
