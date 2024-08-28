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

import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.CM_SUBSCRIBED_EVENTS_NBI;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.INVALID_JSON_FORMAT_ERROR;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIPTIONS;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.SUBSCRIPTION_POST_MUST_CONTAIN_BODY;

import com.fasterxml.jackson.core.JsonParser;
import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.slf4j.Logger;

public class SubscriptionSchemaValidator {

    @Inject
    SystemRecorder systemRecorder;

    @Inject
    Logger logger;

    /**
     * Checks the {@link String} to confirm the Subscription sent is valid.
     *
     * @param subscriptionJson
     *     JSON sent in request parsed as string to be validated
     * @param errors
     *     list of errors if any
     * @return boolean - valid/invalid
     */
    public boolean isInvalidSubscription(final String subscriptionJson, final List<ErrorMessageWrapper> errors) throws IOException {
        JsonNode jsonNode;
        if (subscriptionJson.equals("")) {
            errors.add(new ErrorMessageWrapper(SUBSCRIPTION_POST_MUST_CONTAIN_BODY));
            systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                SUBSCRIPTION_POST_MUST_CONTAIN_BODY);
            return true;
        }
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        try {
            jsonNode = objectMapper.readTree(subscriptionJson);
        } catch (IOException e) {
            errors.add(new ErrorMessageWrapper(INVALID_JSON_FORMAT_ERROR));
            logger.info(INVALID_JSON_FORMAT_ERROR,e);
            systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                INVALID_JSON_FORMAT_ERROR);
            return true;
        }
        final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream schemaNtfSubscriptionControlStream = classLoader.getResourceAsStream("schema/nftSubscriptionControl.json")) {
            final JsonSchema ntfSubscriptionControlSchema = schemaFactory.getSchema(schemaNtfSubscriptionControlStream);
            final Set<ValidationMessage> validationResult = ntfSubscriptionControlSchema.validate(jsonNode);
            if (validationResult.isEmpty()) {
                return false;
            } else {
                validationResult.forEach(vm -> errors.add(new ErrorMessageWrapper(vm.getMessage().replaceFirst("[$][.]", ""))));
                systemRecorder.recordCommand("POST", CommandPhase.FINISHED_WITH_ERROR, CM_SUBSCRIBED_EVENTS_NBI, SUBSCRIPTIONS,
                    CmSubscribedEventsNbiConstantsUtil.VALIDATION_ERRORS);
                return true;
            }
        }

    }
}
