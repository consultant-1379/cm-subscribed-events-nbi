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
package com.ericsson.oss.presentation.server.cmsubscribedevents.validation;

import java.io.IOException;

/**
 * Runtime exception to be thrown when there is an internal error.
 *
 */
public class CmSubscribedEventsNbiException extends RuntimeException {
    private static final long serialVersionUID = 8895661793956474669L;

    public CmSubscribedEventsNbiException(final String message, final IOException e) {
        super(message, e);
    }
}

