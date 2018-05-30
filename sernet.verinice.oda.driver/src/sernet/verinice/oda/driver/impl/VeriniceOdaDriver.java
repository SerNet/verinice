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

import java.io.File;

import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.oda.driver.Activator;
import sernet.verinice.oda.driver.preferences.PreferenceConstants;

public class VeriniceOdaDriver implements IVeriniceOdaDriver {

	
	private static Object instance;
	
	private static final String REPORT_LOGFILE = "verinice-reports.log";
	
	public VeriniceOdaDriver()
	{
		if (instance != null){
			throw new IllegalStateException();
		}
	}
	
	@Override
	public boolean getReportLoggingState(){
	    return Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.REPORT_LOGGING_ENABLED);
	}
	
	@Override
	public String getLogLvl(){
	    return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_LOGGING_LVL);
	}
	
	@Override
	public String getLogFile(){
	    String path = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_LOG_FILE); 
	    if(path != null && !path.endsWith(String.valueOf(File.separatorChar))){
	        path = path + File.separatorChar;
	    }
	    return path + REPORT_LOGFILE;
	}
	
	@Override
	public String getLocalReportLocation(){
	    return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_LOCAL_TEMPLATE_DIRECTORY);
	}
	
	@Override
	public boolean isSandboxEnabled(){
	    String pref = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.REPORT_USE_SANDBOX);
	    return Boolean.parseBoolean(pref);
	}
}
