/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.rules;

import org.apache.log4j.Logger;

public abstract class RuleFactory {

    private static final Logger LOG = Logger.getLogger(RuleFactory.class);
    
    private static final String STD_ERR_MSG = "Klasse für angegebene Regel nicht gefunden: ";
    
	public static IFillRule getDefaultRule(String name) {
		IFillRule rule;
		try {
			rule = (IFillRule) Class.forName("sernet.hui.common.rules."+name).newInstance(); //$NON-NLS-1$
			return rule;
		} catch (InstantiationException e) {
		    LOG.error(STD_ERR_MSG + name); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
		    LOG.error(STD_ERR_MSG + name); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
		    LOG.error(STD_ERR_MSG + name); //$NON-NLS-1$
		}
		return new NullRule();
	}
	
	public static IValidationRule getValidationRule(String name){
	    IValidationRule rule;
        try {
            rule = (IValidationRule) Class.forName("sernet.hui.common.rules."+name).newInstance(); //$NON-NLS-1$
            return rule;
        } catch (InstantiationException e) {
            LOG.error(STD_ERR_MSG + name); //$NON-NLS-1$
        } catch (IllegalAccessException e) {
            LOG.error(STD_ERR_MSG + name); //$NON-NLS-1$
        } catch (ClassNotFoundException e) {
            LOG.error(STD_ERR_MSG + name); //$NON-NLS-1$
        }
        return null;	    
	}
}
