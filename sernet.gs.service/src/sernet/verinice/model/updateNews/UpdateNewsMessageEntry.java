/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.updateNews;

import java.io.Serializable;
import java.util.Locale;

/**
 * representation of a update news message, which needs to be
 * formatted like this (in json):
 * 
 * {
 *    "version" : "1.13.0",
 *    "message" : "<h1> Update News Headline </h1>Some text
 *              that describes the Update and informs <p> the user</p>",
 *    "message_de" : "<h1> Update News Überschrift </h1>Text
 *              der das Update beschreibt und den 
 *              <p>Benutzer informiert</p>",
 *    "updatesite" : "http://path_to/updateSite"          
 *           
 * }
 *
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class UpdateNewsMessageEntry implements Serializable{
    
    /**
     * 
     */
    private static final long serialVersionUID = 20160907133319L;
    private String version;
    private String message;
    private String message_de;
    private String updatesite;
    
    /**
     * parses the configured version to update to 
     * out of the json formatted news message
     */
    public String getVersion(){return version;}
    
    /**
     * parses the configured news message to display
     *  out of the json formatted news message
     *  locale defines if german message should be used or international one
     */
    public String getMessage(Locale locale){
        if(Locale.GERMAN.equals(locale) || Locale.GERMANY.equals(locale)){
            return message_de;
        } else {
            return message;
        }

    }
    public String getMessageDE(){return message_de;}
    
    /**
     * parses the configured updatesite out of the json formatted news message
     */
    public String getUpdateSite(){return updatesite;}
}