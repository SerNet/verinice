/*******************************************************************************
 * Copyright (c) 2021 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/
package sernet.verinice.web;

import java.time.LocalDate;

import org.apache.log4j.Logger;

import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.licensemanagement.LicenseExpiredException;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.model.licensemanagement.NoLicenseAssignedException;

public class LicencedContentDecryptionUtils {

    private static final Logger LOG = Logger.getLogger(LicencedContentDecryptionUtils.class);

    private static final String WEB_MESSAGES_BUNDLE_NAME = "sernet.verinice.web.WebMessages"; //$NON-NLS-1$

    public static String decrypt(Property property) throws LicenseManagementException {
        String encryptedContentId = property.getLicenseContentId();
        String currentUser = getAuthService().getUsername();

        LicenseMessageInfos infos = getLicenseMgmtService().getLicenseMessageInfos(currentUser,
                encryptedContentId, "", null);
        if (infos.isNoLicenseAvailable()) {
            throw new NoLicenseAssignedException(
                    "License " + encryptedContentId + " is not assigned to user: " + currentUser);
        }
        if (infos.getValidUntil().isBefore(LocalDate.now())) {
            throw new NoLicenseAssignedException("License " + encryptedContentId + " has expired");
        }
        String cypherText = property.getPropertyValue();

        return getLicenseMgmtService().decryptRestrictedProperty(encryptedContentId, cypherText,
                currentUser);

    }

    public static String decryptedContentOrErrorMessage(Property firstProperty) {
        try {
            return decrypt(firstProperty);
        } catch (NoLicenseAssignedException e) {
            LOG.error("User has no license assigned for this content", e);
            return Util.getMessage(WEB_MESSAGES_BUNDLE_NAME, "noLicenseAssigned");
        } catch (LicenseExpiredException e) {
            LOG.error("License for restricted content has expired", e);
            return Util.getMessage(WEB_MESSAGES_BUNDLE_NAME, "licenseExpired");
        } catch (LicenseManagementException e) {
            LOG.error("Something went wrong decrypting license restricted information", e);
            return Util.getMessage(WEB_MESSAGES_BUNDLE_NAME, "decryptionError");
        }
    }

    private static ILicenseManagementService getLicenseMgmtService() {
        return (ILicenseManagementService) VeriniceContext.get(VeriniceContext.LICENSE_SERVICE);
    }

    private static IAuthService getAuthService() {
        return (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
    }

    private LicencedContentDecryptionUtils() {
    }
}
