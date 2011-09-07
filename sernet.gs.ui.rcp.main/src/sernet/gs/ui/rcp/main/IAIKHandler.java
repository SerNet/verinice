/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import org.apache.log4j.Logger;

import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Session;
import iaik.pkcs.pkcs11.Slot;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;
import iaik.pkcs.pkcs11.TokenInfo;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class IAIKHandler {

    private static final Logger LOG = Logger.getLogger(IAIKHandler.class);

    String pkcs11DriverPath;
    
    private Module p11Module;

    /**
     * @param pkcs11Driver 
     * 
     */
    public IAIKHandler(String pkcs11Driver) {
        super();
        pkcs11DriverPath = pkcs11Driver;
        initialisePKCS11();
    }

    /**
     * @param module
     */
    public IAIKHandler(Module module) {
        super();
        p11Module = module;
        initialisePKCS11();
    }

    private void initialisePKCS11() {
        // Get an instance of the PKCS #11 module
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Using driver: " + pkcs11DriverPath);
            }
            if(p11Module==null) {
                p11Module = Module.getInstance(pkcs11DriverPath);
                p11Module.initialize(null);
            }
            Slot[] slotsWithToken = p11Module.getSlotList(Module.SlotRequirement.TOKEN_PRESENT);
            if (slotsWithToken == null || slotsWithToken.length < 1) {
                LOG.error("No card found.");
            }
            for (int i = 0; i < slotsWithToken.length; i++) {
                if (LOG.isInfoEnabled()) {
                    TokenInfo tokenInfo = slotsWithToken[i].getToken().getTokenInfo();
                    LOG.info("PKI card found");
                    LOG.info("manufacturer: " + tokenInfo.getManufacturerID());
                    LOG.info("model: " + tokenInfo.getModel());
                    LOG.info("label: " + tokenInfo.getLabel());
                }
                Token p11Token = slotsWithToken[i].getToken();
                login(p11Token);
            }
        } catch (Exception e) {
            LOG.error("Error while IAIK init", e);

        }
    }

    private void login(Token p11Token) {
        try {
            TokenInfo tokenInfo = p11Token.getTokenInfo();
            Session p11Session = p11Token.openSession(Token.SessionType.SERIAL_SESSION, Token.SessionReadWriteBehavior.RW_SESSION, null, null);
            if (tokenInfo.isLoginRequired()) {
                p11Session.login(Session.UserType.USER, null);
            }
        } catch (TokenException e) {
            LOG.error("Error while IAIK login", e);
        }
    }
}
