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

package com.ericsson.oss.presentation.server.cmsubscribedevents.rest.exception.handlers;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.cmsubscribedevents.api.exceptions.HttpsConfigurationException;


import com.ericsson.oss.services.cmsubscribedevents.instrumentation.api.SubscriptionInstrumentation;

import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.HTTPS_CONFIGURATION_ERROR;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.CM_SUBSCRIBED_EVENTS_NBI;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIPTIONS;
import static com.ericsson.oss.presentation.server.events.utils.CmEventsNbiConstants.ERROR_RESPONSE_TEMPLATE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.text.MessageFormat;



/**
 * This Exception handler will handle any {@link HttpsConfigurationException} thrown and return a Response Object with the error messages in the response
 * object body in JSON format. When JAX-RS sees that a HttpsConfigurationException has been thrown by application code, it catches the exception
 * and calls its getResponse() method to obtain a Response to send back to the client. These classes need to be registered in web.xml.
 */
@Provider
public class HttpsConfigurationExceptionHandler implements ExceptionMapper<HttpsConfigurationException> {

    @Inject
    SystemRecorder systemRecorder;

    @EServiceRef
    SubscriptionInstrumentation subscriptionInstrumentation;
    private static final MessageFormat ERROR_JSON_FORMAT = new MessageFormat(ERROR_RESPONSE_TEMPLATE);

    @Override
    public Response toResponse(final HttpsConfigurationException exception) {
        final String responseBody = ERROR_JSON_FORMAT.format(new Object[]{HTTPS_CONFIGURATION_ERROR});

        subscriptionInstrumentation.incrementFailedPostSubscriptions();
        systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                HTTPS_CONFIGURATION_ERROR);

        return Response.status(NOT_FOUND).entity(responseBody).type(MediaType.APPLICATION_JSON).build();
    }
}
