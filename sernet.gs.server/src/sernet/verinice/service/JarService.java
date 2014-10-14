/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IJarService;

/**
 *
 */
public class JarService implements IJarService {
    
    private static final Logger LOG = Logger.getLogger(JarService.class);

    private static final String DEPOSIT_LOCATION = "WebContent/WEB-INF/reportDeposit/";
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IJarService#getDirectory()
     */
    @Override
    public File getReportDepositDirectory() {
        StringBuilder sb = new StringBuilder();
        try{
            sb.append(FileUtils.toFile(JarService.class.getProtectionDomain().getCodeSource().getLocation().toURI().toURL()).getAbsolutePath());
            if(!sb.toString().endsWith(String.valueOf(File.separatorChar))){
               sb.append(File.separatorChar); 
            }
            sb.append(DEPOSIT_LOCATION);
            File f = new File(sb.toString());
            return f;
        } catch (RuntimeException re){
            LOG.error("Error while handling jar stuff", re);
        } catch (URISyntaxException e) {
            LOG.error("Error while handling jar stuff", e);
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            LOG.error("Error while handling jar stuff", e);
            throw new RuntimeException(e);
        }
        return null;
    }


}
