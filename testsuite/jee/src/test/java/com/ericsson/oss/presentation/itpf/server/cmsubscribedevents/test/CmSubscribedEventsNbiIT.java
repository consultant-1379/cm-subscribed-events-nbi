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

package com.ericsson.oss.presentation.itpf.server.cmsubscribedevents.test;

import static com.ericsson.oss.presentation.itpf.server.cmsubscribedevents.test.Artifact.BEANS_XML_FILE;
import static com.ericsson.oss.presentation.itpf.server.cmsubscribedevents.test.Artifact.MANIFEST_MF_FILE;
import static com.ericsson.oss.presentation.itpf.server.cmsubscribedevents.test.Artifact.addEarRequiredLibraries;
import static com.ericsson.oss.presentation.itpf.server.cmsubscribedevents.test.Artifact.createModuleArchive;
import static com.ericsson.oss.presentation.server.cmsubscribedevents.utils.CmSubscribedEventsNbiConstantsUtil.CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.ericsson.oss.itpf.sdk.licensing.Permission;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for testing the Cm Subscribed Events Nbi subscriptions resource via Arquillian.
 */
@RunWith(Arquillian.class)
public final class CmSubscribedEventsNbiIT {

    public static final String LOCALHOST = "localhost";
    public static final int PORT = 8080;

    public static final int EVENT_LISTENER_PORT_NUMBER = 9997;

    public static final String EVENT_LISTENER_URL = "\"http://localhost:" + EVENT_LISTENER_PORT_NUMBER + "/eventListener/v1/sub1\"";

    public static final String HTTP = "http";
    private static final Logger logger = LoggerFactory.getLogger(CmSubscribedEventsNbiIT.class);
    private static final String CM_EVENTS_NBI_OPERATOR_USER = "cmeventsnbi_operator_user";

    private static final String CM_EVENTS_NBI_ADMINISTRATOR_USER = "cmeventsnbi_administrator_user";
    private static final String NO_ROLE_USER = "user_with_no_role";
    @Rule
    public TestRule watcher = new TestWatcher() {

        @Override
        protected void starting(final Description description) {
            logger.info("*******************************");
            logger.info("Starting test: {}()", description.getMethodName());
        }

        @Override
        protected void finished(final Description description) {
            logger.info("Ending test: {}()", description.getMethodName());
            logger.info("*******************************");
        }
    };

    @Deployment(name = "CMSubscribedEventsNbiDeployment")
    public static Archive<?> createTestArchive() {
        logger.info("Creating deployment: CMSubscribedEventsNbiDeployment");
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "CMSubscribedEventsNbiTestEar.ear");
        addEarRequiredLibraries(ear);
        ear.addAsModule(createModuleArchive());
        ear.setManifest(MANIFEST_MF_FILE);
        ear.addAsApplicationResource(BEANS_XML_FILE);
        return ear;
    }

    @Before
    public void init() {

        setupUser(CM_EVENTS_NBI_OPERATOR_USER);

    }

    @Test
    @InSequence(1)
    public void subscribedEventLicenseRuntimeException() throws URISyntaxException, IOException {
        StubbedLicensingServiceSpiBean.setRuntimeExceptionToBeReturnedByStub(true);
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        StubbedLicensingServiceSpiBean.setRuntimeExceptionToBeReturnedByStub(false);

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 500 = FORBIDDEN.
        assertEquals("The expected HTTP 500 was not returned.", SC_INTERNAL_SERVER_ERROR, statusCode);
    }

    @Test
    @InSequence(2)
    public void subscribedEventNoValidLicense() throws IOException, URISyntaxException {
        modifyCmEventsLicensingPermission(Permission.DENIED_NO_VALID_LICENSE);
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        // Reset the license back to Permission.ALLOWED to ensure no failures in other test cases
        modifyCmEventsLicensingPermission(Permission.ALLOWED);

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 403 = FORBIDDEN.
        assertEquals("The expected HTTP 403 was not returned.", SC_FORBIDDEN, statusCode);

    }

    @Test
    @InSequence(3)
    public void subscribedEventValidLicense() throws IOException, URISyntaxException {
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 204 = NO_CONTENT when list of Subscriptions is empty.
        assertEquals("The expected HTTP 204 was not returned.", SC_NO_CONTENT, statusCode);

    }

    @Test
    @InSequence(4)
    public void subscribedEventCreateSubscriptionSuccessTest() throws URISyntaxException, IOException {
        setupUser(CM_EVENTS_NBI_ADMINISTRATOR_USER);
        WireMockServer wireMockServer = new WireMockServer(options().port(EVENT_LISTENER_PORT_NUMBER));
        wireMockServer.start();
        int statusCode;
        String responseMessage;
        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":" + EVENT_LISTENER_URL + ",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"notificationTypes\":[\"notifyMOICreation\"],\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        final StringEntity JSONNtfSubscriptionControl = new StringEntity(subscription);
        final String expectedResponseMessage = subscription.replace("\"id\":\"999\"", "\"id\":\"1\"");
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        final HttpPost httpPostRequest = new HttpPost(uri);
        httpPostRequest.setHeader("Content-type", "application/json");
        httpPostRequest.setEntity(JSONNtfSubscriptionControl);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        logger.debug("Sending RESTful HTTP POST Request: [{}]", httpPostRequest.getRequestLine().getUri());
        try (CloseableHttpResponse response = httpclient.execute(httpPostRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseMessage = EntityUtils.toString(entity, "UTF-8");
            logger.debug("Response ResponseMessage: {}", responseMessage);
        }

        // Check HTTP STATUS 201 = Created.
        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        assertEquals("The expected HTTP 200 was not returned.", SC_CREATED, statusCode);
        final ObjectMapper mapper = new ObjectMapper();
        assertEquals("The expected response message was not returned.", mapper.readTree(responseMessage), mapper.readTree(expectedResponseMessage));
        wireMockServer.stop();

    }

    @Test
    @InSequence(5)
    public void subscribedEventViewAllSubscriptionsWithSubscriptionsPresent() throws IOException, URISyntaxException {
        int statusCode;
        String getResponseMessage;
        String postResponseMessage;

        setupUser(CM_EVENTS_NBI_ADMINISTRATOR_USER);
        WireMockServer wireMockServer = new WireMockServer(options().port(EVENT_LISTENER_PORT_NUMBER));
        wireMockServer.start();

        final String subscription1 = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":" + EVENT_LISTENER_URL + ",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"notificationTypes\":[\"notifyMOICreation\"],\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        final String subscription2 = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":" + EVENT_LISTENER_URL + ",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"notificationTypes\":[\"notifyMOICreation\"],\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";

        String expectedResponse = "[" + subscription1.replace("\"id\":\"999\"", "\"id\":\"1\"") + "," + subscription2.replace("\"id\":\"999\"", "\"id\":\"3\"") + "]";

        final StringEntity JSONNtfSubscriptionControl = new StringEntity(subscription2);

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uriCreate = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        final HttpPost httpPostRequest = new HttpPost(uriCreate);
        httpPostRequest.setHeader("Content-type", "application/json");
        httpPostRequest.setEntity(JSONNtfSubscriptionControl);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uriViewAll = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        final HttpGet httpGetRequest = new HttpGet(uriViewAll);

        logger.debug("Sending RESTful HTTP POST Request: [{}]", httpPostRequest.getRequestLine().getUri());
        try (CloseableHttpResponse response = httpclient.execute(httpPostRequest)) {
            HttpEntity entity = response.getEntity();
            postResponseMessage = EntityUtils.toString(entity, "UTF-8");
            logger.debug("Response ResponseMessage: {}", postResponseMessage);
        }

        logger.info("Sending RESTful HTTP Get Request: [{}]", httpGetRequest.getURI());
        try (CloseableHttpResponse response = httpclient.execute(httpGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            getResponseMessage = EntityUtils.toString(entity);
        }

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 200 = OK.
        assertEquals("The expected HTTP 200 was not returned.", SC_OK, statusCode);

        // Check response message contains root response
        final ObjectMapper mapper = new ObjectMapper();
        assertEquals("The expected response message was not returned.", mapper.readTree(expectedResponse), mapper.readTree(getResponseMessage));

        wireMockServer.stop();
    }

    @Test
    @InSequence(6)
    public void subscribedEventCreateSubscriptionValidationFailureTest() throws URISyntaxException, IOException {
        setupUser(CM_EVENTS_NBI_ADMINISTRATOR_USER);
        int statusCode;
        String responseMessage;
        //missing mandatory attribute ntfSubscriptionControl.notificationRecipientAddress:
        final String expectedResponseMessage = "{\"errors\":[{\"errorMessage\":\"ntfSubscriptionControl.notificationRecipientAddress: is missing but it is required\"}]}";
        StringEntity JSONNtfSubscriptionControl = new StringEntity(
            "{\"ntfSubscriptionControl\":{\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"id\":\"" + 999 + "\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}");
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();
        final HttpPost httpPostRequest = new HttpPost(uri);
        httpPostRequest.setHeader("Content-type", "application/json");
        httpPostRequest.setEntity(JSONNtfSubscriptionControl);
        CloseableHttpClient httpclient = HttpClients.createDefault();

        logger.info("Sending RESTful HTTP POST Request: [{}]", httpPostRequest.getRequestLine().getUri());
        try (CloseableHttpResponse response = httpclient.execute(httpPostRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseMessage = EntityUtils.toString(entity);
            logger.info("Response message: {}", responseMessage);
        }

        // Check HTTP STATUS 400 = Bad Request.
        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        assertEquals("The expected response message was not returned.", expectedResponseMessage, responseMessage);
        assertEquals("The expected HTTP 400 was not returned.", SC_BAD_REQUEST, statusCode);

    }

    @Test
    @InSequence(7)
    public void subscribedEventViewSubscription() throws IOException, URISyntaxException {
        final String subscriptionId = "1";
        final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID = CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId;
        int statusCode;
        String responseMessage;
        final String subscription = "{\"ntfSubscriptionControl\":{\"notificationRecipientAddress\":" + EVENT_LISTENER_URL + ",\"scope\":{\"scopeType\":\"BASE_ALL\",\"scopeLevel\":0},\"notificationTypes\":[\"notifyMOICreation\"],\"id\":\"999\",\"objectClass\":\"/\",\"objectInstance\":\"/\"}}";
        final String expectedResponseMessage = subscription.replace("\"id\":\"999\"", "\"id\":\"1\"");

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseMessage = EntityUtils.toString(entity);
        }

        // Check HTTP STATUS 200 = OK.
        assertEquals("The expected HTTP 200 was not returned.", SC_OK, statusCode);
        final ObjectMapper mapper = new ObjectMapper();
        assertEquals("The expected response message was not returned.", mapper.readTree(expectedResponseMessage), mapper.readTree(responseMessage));
    }
    @Test
    @InSequence(8)
    public void subscribedEventViewSubscriptionFailure() throws IOException, URISyntaxException {
        final String subscriptionId = "999";
        final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID = CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId;
        int statusCode;
        String responseMessage;
        String expectedResponseMessage = "{\"errors\":[{\"errorMessage\":\"Resource could not be found.\"}]}";

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            responseMessage = EntityUtils.toString(entity);
        }

        // Check HTTP STATUS 404 = NOT_FOUND.
        assertEquals("The expected HTTP 404 was not returned.", SC_NOT_FOUND, statusCode);
        assertEquals("The expected response message was not returned", expectedResponseMessage, responseMessage);

    }

    @Test
    @InSequence(9)
    public void subscribedEventDeleteSubscription() throws IOException, URISyntaxException {
        setupUser(CM_EVENTS_NBI_ADMINISTRATOR_USER);
        final String subscriptionId = "1";
        final String CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID = CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS + "/" + subscriptionId;
        int statusCode;
        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/{subscriptionId}
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS_WITH_ID).build();
        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpDelete eventsDeleteRequest = new HttpDelete(uri);
        logger.info("Sending RESTful HTTP Delete Request: [{}]", eventsDeleteRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsDeleteRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }
        // Check HTTP STATUS 204 = NO_CONTENT.
        assertEquals("The expected HTTP 204 was returned.", SC_NO_CONTENT, statusCode);
    }

    @Test
    @InSequence(10)
    public void subscribedEventInvalidUserPermission() throws IOException, URISyntaxException {
        setupUser(NO_ROLE_USER);
        int statusCode;

        // SEND COMMAND: <BASE_URI>/cm/subscribed-events/v1/subscriptions/
        URI uri = new URIBuilder().setScheme(HTTP).setHost(LOCALHOST).setPort(PORT).setPath(CM_SUBSCRIBED_EVENTS_V_1_SUBSCRIPTIONS).build();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet eventsGetRequest = new HttpGet(uri);
        logger.info("Sending RESTful HTTP Get Request: [{}]", eventsGetRequest.getURI());

        try (CloseableHttpResponse response = httpclient.execute(eventsGetRequest)) {
            statusCode = response.getStatusLine().getStatusCode();
        }

        logger.info("Received RESTful HTTP Response Code: [{}]", statusCode);
        // Check HTTP STATUS 401 = UnAuthorized.
        assertEquals("The expected HTTP 401 was not returned.", SC_UNAUTHORIZED, statusCode);

    }

    private void modifyCmEventsLicensingPermission(Permission permission) {
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_5MHzSC, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_CELL_CARRIER, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_ONOFFSCOPE_CORE, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_ONOFFSCOPE_RADIO, permission);
        StubbedLicensingServiceSpiBean.setPermissionToBeReturnedByStub(StubbedLicensingServiceSpiBean.LICENSE_KEY_ONOFFSCOPE_TRANSPORT, permission);
    }

    private void setupUser(final String user) {
        logger.debug("Setting up User");
        String tmpDir;
        final String osName = System.getProperty("os.name");
        if ("Linux".equals(osName)) {
            tmpDir = "/tmp";
        } else {
            tmpDir = System.getProperty("java.io.tmpdir");
        }

        FileWriter fw;
        try {
            fw = new FileWriter(new File(tmpDir, "currentAuthUser"));
            fw.write(user);
            fw.close();
        } catch (final IOException e) {
            logger.error("\n\n" + e.getLocalizedMessage(), e);
            fail("Exception hit: " + e.getMessage());
        }
    }
}
