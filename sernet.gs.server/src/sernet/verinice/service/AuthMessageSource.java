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
package sernet.verinice.service;

import java.util.Locale;
import java.util.Properties;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class AuthMessageSource extends ReloadableResourceBundleMessageSource implements IRemoteMessageSource {

    public Properties getAllMessages() {
        return getAllMessages(Locale.getDefault());
    }
    
    public Properties getAllMessages(Locale locale) {
        PropertiesHolder holder = getMergedProperties(locale);
        Properties properties = null;
        if(holder!=null) {
            properties = holder.getProperties();
        }
        if(properties==null) {
            properties = new Properties();
        }
        return properties;
        
    }
}
