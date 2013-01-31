/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.gs.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import sernet.verinice.model.bsi.ITVerbund;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ItVerbundWrapper {
    
    private static final Logger LOG = Logger.getLogger(ItVerbundWrapper.class);
    
    private ITVerbund itVerbund;

    public ItVerbundWrapper(ITVerbund itVerbund) {
        super();
        this.itVerbund = itVerbund;
    }

    public ITVerbund getItVerbund() {
        return itVerbund;
    }

    public void setItVerbund(ITVerbund itVerbund) {
        this.itVerbund = itVerbund;
    }
    
    public Integer getDbId() {
        return (getItVerbund()!=null) ? getItVerbund().getDbId() : null;
    }
    
    public String getTitle() {
        return (getItVerbund()!=null) ? getItVerbund().getTitle() : null;
        
    }
    
    public String getTitleEscaped() {
        try {
            return (getTitle()!=null) ? URLEncoder.encode(getTitle(), "utf8") : null;
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error while encoding.", e);
            return getTitle();
        }
    
    }
}
