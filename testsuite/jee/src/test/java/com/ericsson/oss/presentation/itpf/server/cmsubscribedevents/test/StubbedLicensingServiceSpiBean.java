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

import com.ericsson.oss.itpf.sdk.licensing.Permission;
import com.ericsson.oss.itpf.sdk.licensing.spi.LicensingServiceSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ejb.Stateless;

/**
 * Stubbed {@code LicensingServiceSPI} to test the API in Arquillian.-
 */
@Stateless
public class StubbedLicensingServiceSpiBean implements LicensingServiceSPI {

    static final String LICENSE_KEY_5MHzSC = "FAT1023443";
    static final String LICENSE_KEY_CELL_CARRIER = "FAT1023603";
    static final String LICENSE_KEY_ONOFFSCOPE_RADIO = "FAT1023988";
    static final String LICENSE_KEY_ONOFFSCOPE_CORE = "FAT1023989";
    static final String LICENSE_KEY_ONOFFSCOPE_TRANSPORT = "FAT1023990";
    private static boolean throwRuntimeException = false;
    private static final Logger logger = LoggerFactory.getLogger(StubbedLicensingServiceSpiBean.class);
    private static final java.util.Map<String, Permission> mapOflicenseKeyToPermission = new java.util.HashMap<>();

    static {
        mapOflicenseKeyToPermission.put(LICENSE_KEY_5MHzSC, Permission.ALLOWED);
        mapOflicenseKeyToPermission.put(LICENSE_KEY_CELL_CARRIER, Permission.ALLOWED);
    }

    @Override
    public boolean isLicensedFeature(final String featureName) {
        logger.debug("Running isLicensedFeature");
        return false;
    }

    @Override
    public java.util.Map<String, Boolean> areLicensedFeatures(final String[] featureNames) {
        logger.debug("Running areLicensedFeatures");
        return null;
    }

    @Override
    public Permission validatePermission(final String licenseKeyId) {
        if (throwRuntimeException) {
            throw new RuntimeException("Runtime Exception");
        }
        logger.debug("validatePermission method invoked, will return Permission value {}", mapOflicenseKeyToPermission.get(licenseKeyId));
        return mapOflicenseKeyToPermission.get(licenseKeyId);
    }

    /**
     * Method to allow control on what this stubbed instance of {@code LicensingService} should return.
     *
     * @param licenseKeyId           the license key identity to return the supplied {@code permissionToBeReturned} value
     * @param permissionToBeReturned the {@code Permission} type to be returned when tests call {@link #validatePermission(String)}
     */
    public static void setPermissionToBeReturnedByStub(final String licenseKeyId, final Permission permissionToBeReturned) {
        logger.debug("Stubbing permission - new value to be returned for {} is {}", licenseKeyId, permissionToBeReturned);
        mapOflicenseKeyToPermission.put(licenseKeyId, permissionToBeReturned);
    }

    /**
     * Method to make {@code LicensingService} throw a runtime exception when test call {@link # validatePermission(String licenseKeyId)}
     *
     * @param returnException {@code returnException} value
     */
    public static void setRuntimeExceptionToBeReturnedByStub(boolean returnException) {
        throwRuntimeException = returnException;
    }
}
