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
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;

/**
 * Retrieves the values needed for the 'deviation/risk' tables that are part of each worst
 * finding.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("serial")
public class LoadDeviationRiskTableCommand extends GenericCommand{

	private List<List<String>> result;
	
	private int chapterId;
	private String chapterName;
	
	public LoadDeviationRiskTableCommand(int chapterId, String chapterName)
	{
		this.chapterId = chapterId;
		this.chapterName = chapterName;
	}
	
	public List<List<String>> getResult()
	{
		return result;
	}

	@Override
	public void execute() {
		ArrayList<String> l = new ArrayList<String>();
		
		// TODO: Actually retrieve those value from the verinice database with the use
		// of the chapter id.
		
		// chapterName
		l.add(chapterName);
		
		// OK: - Low Medium High
		l.add("0");
		l.add("2");
		l.add("5");
		l.add("2");
		
		// Minor: - Low Medium High
		l.add("1");
		l.add("4");
		l.add("1");
		l.add("0");
		
		// Major: - Low Medium High
		l.add("2");
		l.add("3");
		l.add("15");
		l.add("1");
		
		result = new ArrayList<List<String>>();
		result.add(l);
	}
	
}
