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

import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.RECEIVED_VIEW_SUBSCRIPTION_REQUEST_FOR_ID;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIBED_EVENTS_V_1;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIPTION_END_POINT;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.TEXT_LOG_SECURITY_VIOLATION;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.UNEXPECTED_ERROR_OCCURRED;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException;
import com.ericsson.oss.services.cmsubscribedevents.instrumentation.api.SubscriptionInstrumentation;
import com.ericsson.oss.presentation.server.events.licensing.LicenseValidator;
import com.ericsson.oss.presentation.server.events.utils.ErrorResponseHandler;
import java.io.IOException;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;

/**
 * Provides entry point to cmsubscribedevents REST endpoints
 */
@Path(SUBSCRIBED_EVENTS_V_1)
public class SubscriptionEntryPoint {

    @Inject
    private SubscriptionsRestResource subscriptionRest;

    @Inject
    private LicenseValidator licenseValidator;

    @Inject
    private Logger logger;

    @Inject
    private ErrorResponseHandler errorHandler;

    @EServiceRef
    private SubscriptionInstrumentation subscriptionInstrumentation;

    public static final String HAL_JSON = "application/hal+json";

    /**
     * Entry point to view Subscription by ID
     *
     * @param id
     *     Subscription ID
     * @return Response with requested Subscription in JSON format in body
     */
    @GET
    @Produces({ HAL_JSON })
    @Path(SUBSCRIPTION_END_POINT + "/{id}")
    public Response viewSubscriptionById(@PathParam("id") final Integer id) throws IOException {
        if (licenseValidator.isLicenseInvalid()) {

            subscriptionInstrumentation.incrementFailedSubscriptionViews();

            return errorHandler.unlicensedResponse();
        }
        logger.info(RECEIVED_VIEW_SUBSCRIPTION_REQUEST_FOR_ID, id);
        try {
            return subscriptionRest.viewSubscription(id);
        } catch (final SecurityViolationException e) {
            logger.info(TEXT_LOG_SECURITY_VIOLATION, e);

            subscriptionInstrumentation.incrementFailedSubscriptionViews();

            return errorHandler.unauthorizedResponse();
        }
    }

    /**
     * Entry point to create Subscription
     *
     * @param subscriptionJson
     *     Subscription in JSON format
     * @return Response with created Subscription in JSON format
     */
    @POST
    @Produces({ HAL_JSON })
    @Path(SUBSCRIPTION_END_POINT)
    public Response postSubscription(final String subscriptionJson) {
        if (licenseValidator.isLicenseInvalid()) {

            subscriptionInstrumentation.incrementFailedPostSubscriptions();

            return errorHandler.unlicensedResponse();
        }
        logger.debug("Create subscription input JSON: {}", subscriptionJson);
        try {
            return subscriptionRest.postSubscription(subscriptionJson);
        } catch (final SecurityViolationException e) {
            logger.info(TEXT_LOG_SECURITY_VIOLATION, e);

            subscriptionInstrumentation.incrementFailedPostSubscriptions();

            return errorHandler.unauthorizedResponse();
        } catch (IOException e) {
            logger.info(UNEXPECTED_ERROR_OCCURRED, e);

            subscriptionInstrumentation.incrementFailedPostSubscriptions();

            return errorHandler.defaultInternalServerError();
        }
    }

    /**
     * Entry point to delete a Subscription
     *
     * @param subscriptionId
     *    Subscription ID as integer
     * @return Response with created Subscription in JSON format
     */
    @DELETE
    @Produces({ HAL_JSON })
    @Path(SUBSCRIPTION_END_POINT + "/{id}")
    public Response deleteSubscription(@PathParam("id") final Integer subscriptionId) {
        if (licenseValidator.isLicenseInvalid()) {

            subscriptionInstrumentation.incrementFailedSubscriptionDeletion();

            return errorHandler.unlicensedResponse();
        }

        try {
            logger.debug("Received DELETE for subscriptionId={}", subscriptionId);
            return subscriptionRest.deleteSubscription(subscriptionId);
        } catch (final SecurityViolationException e) {
            logger.info(TEXT_LOG_SECURITY_VIOLATION, e);

            subscriptionInstrumentation.incrementFailedSubscriptionDeletion();

            return errorHandler.unauthorizedResponse();
        } catch (final IOException e) {
            logger.info(UNEXPECTED_ERROR_OCCURRED, e);

            subscriptionInstrumentation.incrementFailedSubscriptionDeletion();

            return errorHandler.defaultInternalServerError();
        }
    }

    /**
     * Entry point to view all available Subscriptions.
     *
     * @return Response with all created Subscriptions.
     */
    @GET
    @Produces({ HAL_JSON })
    @Path(SUBSCRIPTION_END_POINT)
    public Response viewAllSubscriptions() throws IOException {
        if (licenseValidator.isLicenseInvalid()) {

            subscriptionInstrumentation.incrementFailedViewAllSubscriptions();

            return errorHandler.unlicensedResponse();
        }
        try {
            return subscriptionRest.viewAllSubscriptions();
        } catch (final SecurityViolationException e) {

            subscriptionInstrumentation.incrementFailedViewAllSubscriptions();

            logger.error(TEXT_LOG_SECURITY_VIOLATION, e);
            return errorHandler.unauthorizedResponse();
        } catch (final IOException e){
            logger.error(UNEXPECTED_ERROR_OCCURRED, e);

            subscriptionInstrumentation.incrementFailedSubscriptionDeletion();

            return errorHandler.defaultInternalServerError();
        }
    }
}

