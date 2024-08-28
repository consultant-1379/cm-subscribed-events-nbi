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

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.cmsubscribedevents.api.SubscriptionService;
import javax.enterprise.context.ApplicationScoped;

/**
 * Proxy for Subscription Service.
 */

@ApplicationScoped
public class SubscriptionServiceProxy {

    @EServiceRef
    SubscriptionService subscriptionService;

    public SubscriptionService getSubscriptionService() {
        return subscriptionService;
    }

}
