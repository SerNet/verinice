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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jbpm.api.ProcessEngine;

import sernet.hui.common.VeriniceContext;

/**
 * Computes new reminder days for dates that lie in the past. Used from
 * <code>individual-task.jpdl.xml</code>
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DateChecker {

    private static final Logger LOG = Logger.getLogger(DateChecker.class);

    /**
     * Checks if the given date is in the past. If it is, keeps adding the
     * number of days that is specified by the second parameter until the date
     * is no longer in the past.
     * 
     * @param date
     *            the date to check
     * @param daysDeltaString
     *            the number of days to add in each step
     * @return a date that is after the current instant
     */
    public Date checkIfDateIsPast(Date date, String daysDeltaString) {
        return checkIfDateIsPast(date, daysDeltaString, Clock.systemUTC());

    }

    Date checkIfDateIsPast(Date date, String daysDeltaString, Clock clock) {
        Instant now = Instant.now(clock);
        Instant dateAsInstant = date.toInstant();
        if (!dateAsInstant.isBefore(now)) {
            return date;
        }

        int daysDelta = getIntValue(daysDeltaString);
        if (daysDelta <= 0) {
            throw new IllegalArgumentException("Invalid value specified for days delta: "
                    + daysDeltaString + ", must be greater than 0.");
        }
        long daysPassed = ChronoUnit.DAYS.between(dateAsInstant, now);
        long deltaIterations = daysPassed / daysDelta + 1l;
        dateAsInstant = dateAsInstant.atZone(ZoneId.systemDefault())
                .plusDays(deltaIterations * daysDelta).toInstant();
        return Date.from(dateAsInstant);
    }

    /**
     * Tries to parse the given string as a number. If the parsing fails,
     * returns a default of 7.
     * 
     * @param dayString
     *            the string to parse
     * @return the parsed number or <code>7</code> if the number cannot be
     *         parsed.
     */
    public int getIntValue(String dayString) {
        final int daysAWeek = 7;
        int days = daysAWeek;
        try {
            days = Integer.valueOf(dayString);
        } catch (Exception e) {
            LOG.error("Error while parsing day number param: " + dayString, e);
            days = daysAWeek;
        }
        return days;
    }

    protected ProcessEngine getProcessEngine() {
        return (ProcessEngine) VeriniceContext.get(VeriniceContext.JBPM_PROCESS_ENGINE);
    }

}
