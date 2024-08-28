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

import java.io.File;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for EAR Deployment for the Arquillian environment.
 */
public final class Artifact {

    public static final File MANIFEST_MF_FILE = new File("src/test/resources/META-INF/MANIFEST.MF");
    public static final File BEANS_XML_FILE = new File("src/test/resources/META-INF/beans.xml");
    public static final String LOG_4_J_PROPERTIES = "log4j.properties";
    public static final String META_INF_BEANS_XML = "META-INF/beans.xml";
    private static final Logger logger = LoggerFactory.getLogger(Artifact.class);
    private static final String GROUP_HTTP_CLIENT = "org.apache.httpcomponents";

    public static final String COM_ERICSSON_OSS_SERVICES_CMSUBSCRIBEDEVENTS = "com.ericsson.oss.services.cmsubscribedevents";

    public static final String CMSUBSCRIBEDEVENTSSUBSCRIPTIONSVC_API = "cmsubscribedeventssubscriptionsvc-api";

    public static final String COM_GITHUB_TOMAKEHURST = "com.github.tomakehurst";

    public static final String WIREMOCK_STANDALONE = "wiremock-jre8";

    private Artifact() {
    }

    static void addEarRequiredLibraries(final EnterpriseArchive archive) {
        logger.debug("Adding libs to ear: {}", archive);
        archive.addAsLibraries(resolveAsFiles(GROUP_HTTP_CLIENT, "httpcore"));
        archive.addAsLibraries(resolveAsFiles(GROUP_HTTP_CLIENT, "httpclient"));
        archive.addAsLibraries(resolveAsFiles(COM_ERICSSON_OSS_SERVICES_CMSUBSCRIBEDEVENTS, CMSUBSCRIBEDEVENTSSUBSCRIPTIONSVC_API));
        archive.addAsLibraries(resolveAsFiles(COM_GITHUB_TOMAKEHURST, WIREMOCK_STANDALONE));
        archive.addAsLibraries(resolveAsFiles(COM_ERICSSON_OSS_SERVICES_CMSUBSCRIBEDEVENTS,CMSUBSCRIBEDEVENTSSUBSCRIPTIONSVC_API));
        archive.addAsResource(LOG_4_J_PROPERTIES);
    }

    static Archive<?> createModuleArchive() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "cm-subscribed-event-nbi-test-bean-lib.jar")
            .addAsResource(MANIFEST_MF_FILE)
            .addAsResource(META_INF_BEANS_XML)
            .addPackage(Artifact.class.getPackage());
        logger.debug("Creating Module Archive JAR: {}", archive);
        return archive;
    }

    private static File[] resolveAsFiles(final String groupId, final String artifactId) {
        return Maven.resolver().loadPomFromFile("pom.xml").resolve(groupId + ":" + artifactId).withTransitivity().asFile();
    }
}
