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
package com.ericsson.oss.presentation.server.cmsubscribedevents.rest.resources;

import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.CM_SUBSCRIBED_EVENTS_NBI;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.CM_SUBSCRIBED_EVENTS_NBI_RESOURCE;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.RBAC_CREATE_PERMISSION;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.RBAC_DELETE_PERMISSION;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.RBAC_READ_PERMISSION;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIPTIONS;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.CM_SUBSCRIBED_EVENTS_NBI_SUBSCRIPTION_DELETED;
import static com.ericsson.oss.services.cmsubscribedevents.constants.InstrumentationConstants.SUBSCRIPTION_ID;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.annotation.Authorize;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.api.SubscriptionInstrumentation;
import com.ericsson.oss.presentation.server.cmsubscribedevents.validation.ErrorMessageWrapper;
import com.ericsson.oss.presentation.server.cmsubscribedevents.validation.RestErrorWrapper;
import com.ericsson.oss.presentation.server.cmsubscribedevents.validation.SubscriptionSchemaValidator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;

/**
 * Authorizes and validates user REST access, sends REST request content the applicable service for processing. Returns subscription Response content
 */
public class SubscriptionsRestResource {

    @Inject
    SystemRecorder systemRecorder;

    @Inject
    private Logger logger;

    @Inject
    private SubscriptionSchemaValidator subscriptionValidator;

    @Inject
    private SubscriptionServiceProxy subscriptionServiceProxy;

    @EServiceRef
    SubscriptionInstrumentation subscriptionInstrumentation;

    /**
     * Authenticates user, passes subscription body for subscription validation and creation
     *
     * @param subscriptionJson
     *     Subscription in JSON format
     * @return Response with created Subscription in JSON format
     */
    @Authorize(resource = CM_SUBSCRIBED_EVENTS_NBI_RESOURCE, action = RBAC_CREATE_PERMISSION)
    public Response postSubscription(final String subscriptionJson) throws IOException {
        logger.info("Received HTTP POST Request for Subscription:{}", subscriptionJson);
        final List<ErrorMessageWrapper> errors = new ArrayList<>();
        if (subscriptionValidator.isInvalidSubscription(subscriptionJson, errors)) {
            logger.info("Validation failed for Subscription:{}", subscriptionJson);
            for (final ErrorMessageWrapper error : errors) {
                logger.info("Validation error : {}", error.getErrorMessage());
            }
            final RestErrorWrapper error = new RestErrorWrapper(errors);

            subscriptionInstrumentation.incrementFailedPostSubscriptions();

            return Response.status(BAD_REQUEST).entity(error.asJsonString()).build();
        }

        //Persist Subscription
        try {
            logger.debug("Executing subscription persistence service create for Subscription:{}", subscriptionJson);
            final String response = subscriptionServiceProxy.getSubscriptionService().createSubscription(subscriptionJson);
            logger.info("Successfully created Subscription:{}", response);

            subscriptionInstrumentation.incrementSuccessfulPostSubscriptions();

            return Response.status(201).entity(response).build();
        } catch (final IOException ex) {
            logger.info("Persist subscription failed Exception: ", ex);

            subscriptionInstrumentation.incrementFailedPostSubscriptions();

            return Response.status(BAD_REQUEST).entity(subscriptionJson).build();
        }
    }

    /**
     * Authenticates user, passes subscription id for view
     *
     * @param id
     *     Subscription ID
     * @return Response with requested Subscription in JSON format in body
     * @throws IOException
     *     - IOException thrown when view cannot access specified resource
     */
    @Authorize(resource = CM_SUBSCRIBED_EVENTS_NBI_RESOURCE, action = RBAC_READ_PERMISSION)
    public Response viewSubscription(final Integer id) throws IOException {
        logger.debug("Received HTTP GET Request for SubscriptionID \"{}\"", id);
        String subscriptionJson;
        subscriptionJson = subscriptionServiceProxy.getSubscriptionService().viewSubscription(id);
        systemRecorder.recordCommand("GET", CommandPhase.FINISHED_WITH_SUCCESS, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS, "Subscription ID: " + id);

        subscriptionInstrumentation.incrementSuccessfulSubscriptionViews();

        return Response
            .status(Response.Status.OK)
            .entity(subscriptionJson)
            .build();
    }

    /**
     * Authenticates user, passes subscription id for delete
     *
     * @param id
     *     Subscription ID
     * @return Response with no content
     * @throws IOException
     *     - IOException thrown when view cannot access specified resource
     */
    @Authorize(resource = CM_SUBSCRIBED_EVENTS_NBI_RESOURCE, action = RBAC_DELETE_PERMISSION)
    public Response deleteSubscription(final Integer id) throws IOException {
        logger.info("Received HTTP DELETE Request for subscription Id={}", id);
        subscriptionServiceProxy.getSubscriptionService().deleteSubscription(id);
        logger.info("Successfully deleted SubscriptionId:{}", id);
        systemRecorder.recordCommand("DELETE", CommandPhase.FINISHED_WITH_SUCCESS, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS, "Subscription ID:" + id);

        final HashMap<String, Object> subscriptionIdMap = new HashMap<>();
        subscriptionIdMap.put(SUBSCRIPTION_ID, id);
        systemRecorder.recordEventData(CM_SUBSCRIBED_EVENTS_NBI_SUBSCRIPTION_DELETED, subscriptionIdMap);

        subscriptionInstrumentation.incrementSuccessfulSubscriptionDeletion();

        return Response.status(Response.Status.NO_CONTENT).build();
    }

     /**
     * Authenticates user, passes all the subscriptions for view
      *
     * @return Response with all Subscriptions in JSON format in body
     * @throws IOException
     *      IOException thrown when view cannot access specified resource
     *
     */
    @Authorize(resource = CM_SUBSCRIBED_EVENTS_NBI_RESOURCE, action = RBAC_READ_PERMISSION)
    public Response viewAllSubscriptions() throws IOException {
        logger.info("Received HTTP GET request for all subscriptions");
        String subscriptionsJson = subscriptionServiceProxy.getSubscriptionService().viewAllSubscriptions();
        systemRecorder.recordCommand("GET", CommandPhase.FINISHED_WITH_SUCCESS, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS, "All Subscriptions:");

        subscriptionInstrumentation.incrementSuccessfulViewAllSubscriptions();

        if(subscriptionsJson.contentEquals("[]")){
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .entity(subscriptionsJson)
                    .build();
        }
        return Response
                .status(Response.Status.OK)
                .entity(subscriptionsJson)
                .build();
        }
}
