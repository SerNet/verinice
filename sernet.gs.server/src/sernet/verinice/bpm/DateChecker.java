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
package sernet.verinice.bpm;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jbpm.api.ProcessEngine;

import sernet.hui.common.VeriniceContext;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DateChecker {

    private static final Logger LOG = Logger.getLogger(DateChecker.class);
    
    public Date checkIfDateIsPast(Date date, String dayString) {
        int days = getIntValue(dayString);
        Calendar newDate = Calendar.getInstance();
        newDate.setTime(date);
        Calendar now = Calendar.getInstance();
        if(newDate.before(now)) {
            newDate.add(Calendar.DAY_OF_MONTH, days);
            return checkIfDateIsPast(newDate.getTime(), String.valueOf(days));
        }
        return newDate.getTime();
    }

    /**
     * @param dayString
     * @return
     */
    public int getIntValue(String dayString) {
        final int daysAWeek = 7;
        int days = daysAWeek;
        try {
            days = Integer.valueOf(dayString);
        } catch(Exception e) {
            LOG.error("Error while parsing day number param: " + dayString, e);
            days = daysAWeek;
        }
        return days;
    }
    
    protected ProcessEngine getProcessEngine() {
        return (ProcessEngine) VeriniceContext.get(VeriniceContext.JBPM_PROCESS_ENGINE);
    }
    
}
