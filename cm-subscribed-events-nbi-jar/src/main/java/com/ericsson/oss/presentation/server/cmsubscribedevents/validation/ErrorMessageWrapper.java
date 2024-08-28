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

/**
 * Contains Error Messages adhering to a structure that can produce JSON format REST error responses.
 */
public class ErrorMessageWrapper {

    private final String errorMessage;

    /**
     * Constructor takes an errorMessage.
     *
     * @param errorMessage - the error errorMessage
     */
    public ErrorMessageWrapper(final String errorMessage) {
        this.errorMessage = errorMessage;
    }


    /**
     * Gets the errorMessage.
     *
     * @return String - error
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
