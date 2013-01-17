/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.verinice.report.service.commands;

import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;

/**
 *
 */
public class LoadISAReportChapterRisk extends GenericCommand {
    
    private static transient Logger LOG = Logger.getLogger(LoadISAReportChapterRisk.class);
    private static final String PROP_ISATOPIC_RISK = "samt_topic_audit_ra";
    
    public static String[] COLUMNS = new String[]{"title", 
                                                  "riskValue"
                                                 };

    private Integer rootElmnt;
    
    private List<List<String>> result;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
    }

}
