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
package com.ericsson.oss.itpf.sdk.security.accesscontrol.classic;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityAction;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityResource;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Implementation of EAccessControl interface for testing.
 */
public class EAccessControlAltImplMock extends EAccessControlImpl implements EAccessControl {
    private static final Logger logger = LoggerFactory.getLogger(EAccessControlAltImplMock.class);

    /**
     * The expected authorized calls to isAuthorized. The string format is based on the arguments passed to isAuthorized:subject;resource;action see
     * the access control annotations for more details
     */
    private static final String[] OPERATOR = new String[] {
            "cmeventsnbi_operator_user;cm-subscribed-events-nbi;read"
    };

    /**
     * The expected authorized calls to isAuthorized. The string format is based on the arguments passed to isAuthorized:subject;resource;action see
     * the access control annotations for more details
     */
    private static final String[] ADMINISTRATOR = new String[]{
            "cmeventsnbi_administrator_user;cm-subscribed-events-nbi;read",
            "cmeventsnbi_administrator_user;cm-subscribed-events-nbi;create",
            "cmeventsnbi_administrator_user;cm-subscribed-events-nbi;delete"
    };

    /**
     * Same as AUTHORIZED but for unauthorized requests.
     */
    private static final String[] UNAUTHORIZED = new String[]{
            "user_with_no_role;cm-subscribed-events-nbi;read",
    };

    @Override
    public ESecuritySubject getAuthUserSubject() throws SecurityViolationException {
        logger.debug("************************************************************");
        logger.debug("AccessControlAltImplForTest IS NOT FOR PRODUCTION USE.");
        logger.debug("AccessControlAltImplForTest: getAuthUserSubject called.");
        logger.debug("************************************************************");

        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String useridFile = String.format("%s/currentAuthUser", tmpDir);
        String currentUser;
        try {
            currentUser = new String(Files.readAllBytes(Paths.get(useridFile)));
        } catch (final IOException ioe) {
            logger.error("Error reading {}, Details: {}", useridFile, ioe.getMessage());
            currentUser = "ioerror";
        }

        logger.debug("AccessControlAltImplForTest: getAuthUserSubject: user is <{}>", currentUser);
        return new ESecuritySubject(currentUser);
    }

    @Override
    public boolean isAuthorized(final ESecuritySubject secSubject, final ESecurityResource secResource, final ESecurityAction secAction)
            throws SecurityViolationException, IllegalArgumentException {
        logger.debug("************************************************************");
        logger.debug("AccessControlAltImplForTest IS NOT FOR PRODUCTION USE.");
        logger.debug("AccessControlAltImplForTest: isAuthorized 1 called");
        logger.debug("************************************************************");
        final String action = secAction.getActionId().toLowerCase();
        final String subject = secSubject.getSubjectId().toLowerCase();
        final String resource = secResource.getResourceId().toLowerCase();
        final String authorization = String.format("%s;%s;%s", subject, resource, action);

        if (Arrays.asList(OPERATOR).contains(authorization)) {
            return true;
        }
        if (Arrays.asList(ADMINISTRATOR).contains(authorization)) {
            return true;
        }
        if (Arrays.asList(UNAUTHORIZED).contains(authorization)) {
            throw new SecurityViolationException("The cm-subscribed-events-nbi dummy access control UNAUTHORIZED ACCESS for " + subject);
        }

        throw new IllegalStateException(
                "The cm-subscribed-events-nbi dummy access control doesn't expect the authorization string : " + authorization
                        + ". Add the authorization string to " + EAccessControlAltImplMock.class + ".UNAUTHORIZED or .AUTHORIZED");
    }
}
