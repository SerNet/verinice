/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 *     Sebastian Hagedorn <sh@sernet.de> - report logging
 ******************************************************************************/
package sernet.verinice.oda.driver.impl;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import sernet.verinice.interfaces.oda.IImageProvider;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.oda.driver.Activator;
import sernet.verinice.oda.driver.preferences.PreferenceConstants;

public class VeriniceOdaDriver implements IVeriniceOdaDriver {

	private VeriniceURLStreamHandlerService urlStreamHandlerFactory;
	
	private Map<String, Object> vars = new HashMap<String, Object>();
	
	private static Object instance;
	
	public VeriniceOdaDriver()
	{
		if (instance != null){
			throw new IllegalStateException();
		}
	}
	
	protected void activate(ComponentContext ctx)
	{
		this.urlStreamHandlerFactory = Activator.getDefault().getURLStreamHandlerService();
	}
	
	@Override
	public void setImageProvider(String name, IImageProvider imageProvider)
	{
		urlStreamHandlerFactory.setImageProvider(name, imageProvider);
	}
	
	@Override
	public void removeImageProvider(String name)
	{
		urlStreamHandlerFactory.remove(name);
	}
	
	@Override
	public void setScriptVariables(Map<String, Object> vars)
	{
		this.vars = vars;
	}

	@Override
	public Map<String, Object> getScriptVariables() {
		return vars;
	}
	
	public boolean getReportLoggingState(){
	    return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.REPORT_LOGGING_ENABLED);
	}
	
	public String getLogLvl(){
	    return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_LOGGING_LVL);
	}
	
	public String getLogFile(){
	    return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_LOG_FILE);
	}
	
	public void setUseReportCache(boolean useReportCache){
	    Activator.getDefault().setUseReportCache(useReportCache);
	}

}
