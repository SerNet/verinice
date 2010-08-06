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
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;

/** This commands loads the values that should appear in the introduction part of the
 * 'comprehensive security assessment report'.
 * 
 * TODO: Decide upon appearance, order and (most importantly) on how to retrieve the values.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("serial")
public class LoadSamtIntroTableCommand extends GenericCommand{

	private List<List<String>> result;
	
	static HashMap<String, String> hardcodeTable = new HashMap<String, String>();
	
	static
	{
		// TODO: Values taken from demo report - serve as placeholders.
		hardcodeTable.put("companyName", "TEST - Company 1");
		hardcodeTable.put("ceo", "Schulze Peter");
		hardcodeTable.put("itManager", "Gustav Hafensaenger");
		hardcodeTable.put("ciso", "Dr. Gerd Meier");
		hardcodeTable.put("auditor", "Max Mustermann, Hans Müller");
		hardcodeTable.put("scope", "IT Area of TEST - Company 1");
		hardcodeTable.put("basis", "Assessment based on ISO/IEC 27001 with additional physical checks and system checks");
		hardcodeTable.put("date", "30.07.2007 - 03.08.2007");
		hardcodeTable.put("language", "English");
		hardcodeTable.put("version", "0.4");
	}
	
	public List<List<String>> getResult()
	{
		return result;
	}

	@Override
	public void execute() {
		List<List<String>> l = new ArrayList<List<String>>();
		
		l.add(mvars("Company", "companyName"));
		l.add(mvars("CEO", "ceo"));
		l.add(mvars("IT Manager", "itManager"));
		l.add(mvars("CISO", "ciso"));
		l.add(mvars("Auditor", "auditor"));
		l.add(mvars("Scope", "scope"));
		l.add(mvars("Basis", "basis"));
		l.add(mvars("Date", "date"));
		l.add(mvars("Language", "language"));
		l.add(mvars("Version", "version"));
		
		result = l;
	}
	
	private List<String> mvars(String label, String key)
	{
		List<String> l = new ArrayList<String>();
		l.add(label);
		
		
		String value = hardcodeTable.get(key);
		if (value == null)
		{
			l.add("'" + key + "'");
		}
		else
		{
			l.add(value);
		}
		
		return l;
	}
	
}
