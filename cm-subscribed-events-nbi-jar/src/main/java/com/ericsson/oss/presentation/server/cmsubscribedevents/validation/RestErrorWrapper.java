/*
 * ------------------------------------------------------------------------------
 *  *******************************************************************************
 *  * COPYRIGHT Ericsson 2022
 *  *
 *  * The copyright to the computer program(s) herein is the property of
 *  * Ericsson Inc. The programs may be used and/or copied only with written
 *  * permission from Ericsson Inc. or in accordance with the terms and
 *  * conditions stipulated in the agreement/contract under which the
 *  * program(s) have been supplied.
 *  *******************************************************************************
 *  *----------------------------------------------------------------------------
 */
package com.ericsson.oss.presentation.server.cmsubscribedevents.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Placeholder class that contains a List of Errors that can be retrieved in the JSON structure.
 */
public class RestErrorWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestErrorWrapper.class);
    private final List<ErrorMessageWrapper> errors;
    private final FactoryHelper helper;

    /**
     * Constructor takes list of errors.
     *
     * @param errors
     *     - list of errors
     */
    public RestErrorWrapper(final List<ErrorMessageWrapper> errors) {
        this(errors, new FactoryHelper());
    }

    /**
     * Constructor to allow creation of Factory for testing of internal object creation
     *
     * @param errors List of ErrorMessageWrapper errors
     * @param factoryHelper factory for support of testing internally created objects
     */
    RestErrorWrapper(final List<ErrorMessageWrapper> errors, final FactoryHelper factoryHelper) {
        this.errors = new ArrayList<>(errors);
        this.helper = factoryHelper;

    }

    /**
     * Returns a list of errors.
     *
     * @return {@link List} - errors
     */
    public List<ErrorMessageWrapper> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Returns this object as json.
     *
     * @return {@link String} - json
     */
    public String asJsonString() {
        final String stringErrors;
        try {
            LOGGER.debug("RestError::asJsonString(): Attempting to convert this to JSON - [{}]", this);
            final ObjectMapper mapper = helper.retrieveMapper();
            stringErrors = mapper.writeValueAsString(this);
        } catch (final IOException e) {
            LOGGER.error("RestError::asJsonString(): IOException", e);
            throw new CmSubscribedEventsNbiException("Converting to RestError " + this, e);
        }
        return stringErrors;
    }

    /**
     * Internal static Factory for testable creation of static ObjectMapper
     */
    static class FactoryHelper {

        ObjectMapper retrieveMapper() {
            return new ObjectMapper();
        }
    }

}
