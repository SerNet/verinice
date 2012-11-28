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

/**
 * parameter 0 is always the hint, parameter 1 needs to be the regex
 */
public class RegExRule implements IValidationRule {
    
    private String hint = Messages.RegExDefaultHint;
    
    private String regEx;
    
    /* (non-Javadoc)
     * @see sernet.hui.common.rules.IValidationRule#validate(java.lang.String, java.lang.String[])
     */
    @Override
    public boolean validate(String userInput, String[] params) {
        return userInput.matches(regEx);
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
    public void init(String[] params, String hint) {
        if( params != null && params.length == 1){
            regEx = params[0];
        }
        if(hint != null && !hint.equals("")){
            this.hint = hint;
        } else {
            this.hint = Messages.RegExDefaultHint;
        }
    }

}
