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
package sernet.hui.common.rules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

/**
 *
 */
public class DateAfterRule implements IValidationRule {
    
    private String hint = Messages.DateAfterDefaultHint;
    
    private static transient Logger LOG = Logger.getLogger(DateAfterRule.class);
    
    private static final String PATTERN = "dd.MM.yyyy";
    
    private Date compareDate;
    
    private SimpleDateFormat formatter;

    /* (non-Javadoc)
     * @see sernet.hui.common.rules.IValidationRule#validate(java.lang.String, java.lang.String[])
     */
    @Override
    public boolean validate(String userInput, String[] params) {
        if(formatter != null && userInput != null){
            try {
//                Date userDate = formatter.parse(userInput);
                Date userDate = new Date(Long.parseLong(userInput));
                return userDate.after(compareDate);
            } catch (Exception e) {
                LOG.error("user given date unparseable", e);
            }
        } 
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.hui.common.rules.IValidationRule#getHint()
     */
    @Override
    public String getHint() {
        return hint;
    }

    /* (non-Javadoc)
     * @see sernet.hui.common.rules.IValidationRule#init(java.lang.String[])
     */
    @Override
    public void init(String[] params) {
        formatter = new SimpleDateFormat(PATTERN, Locale.getDefault());
        formatter.setLenient(true); // set parser to unprecisely parsing
        try {
            if(params != null && params.length == 1){
                compareDate = formatter.parse(params[0]);
                hint = Messages.DateAfterDefaultHint;
            } else if(params != null && params.length == 2){
                hint = params[0];
                compareDate = formatter.parse(params[1]);
            }
        } catch (ParseException e) {
            LOG.error("Date specified by parameter not parseable", e);
            compareDate = null;
        }
    }

}
