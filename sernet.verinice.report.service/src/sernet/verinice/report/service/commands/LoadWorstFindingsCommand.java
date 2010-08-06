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

import java.util.HashMap;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads the worst finding for a given {@link CnATreeElement} (?).
 *
 * TODO samt: Needs to be implemented in a way that it really finds out the
 * worst finding of a given element. It is not completely clear whether the
 * element of which this is being done is really a {@link CnATreeElement} or
 * a specific subclass. 
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadWorstFindingsCommand extends GenericCommand {

	private Object[][] result;

	private int id;
	
	static HashMap<Integer, Object[]> hardcodedData = new HashMap<Integer, Object[]>();
	
	static
	{
		// ISO/IEC entries
		makeEntry(1001, "Security Policy", "finding", 0, 1, "measure");
		makeEntry(1002, "Organization of Information Security",
				"finding", 1, 2, "measure");
		makeEntry(1003, "Asset Management", "finding", 2, 3, "measure");
		makeEntry(1004, "Human Resources Security", "finding", 0, 0,
				"measure");
		makeEntry(1005, "Physical and Environmental Security",
				"finding", 1, 0, "measure");
		makeEntry(1006, "Communications and Operations Management",
				"finding", 2, 2, "measure");
		makeEntry(1007, "Access Control", "finding", 2, 1, "measure");
		makeEntry(
				1008,
				"Information Systems Acquisition, Development and Maintenance",
				"finding", 1, 1, "measure");
		makeEntry(1009, "Information Security Incident Management",
				"finding", 0, 2, "measure");
		makeEntry(1010, "Business Continuity Management", "finding",
				0, 0, "measure");
		
		// IT-Rooms entry
		makeEntry(2000, "Serverroom #1", "Package material (combustible material) inside the data center.", 1, 3, "Remove package material.\nRecommendation:\nRemove own data center and move the server to the new data center, because of cost & security synergies."); 
		makeEntry(2001, "Serverroom #2", "Package material (combustible material) inside the data center.", 1, 2, "Remove package material.\nRecommendation:\nRemove own data center and move the server to the new data center, because of cost & security synergies."); 
		makeEntry(2002, "Serverroom #3", "Package material (combustible material) inside the data center.", 1, 1, "Remove package material.\nRecommendation:\nRemove own data center and move the server to the new data center, because of cost & security synergies.");
		makeEntry(2003, "Workstationroom #1", "Package material (combustible material) inside the data center.", 1, 0, "Remove package material.\nRecommendation:\nRemove own data center and move the server to the new data center, because of cost & security synergies.");
	}
	
	private static void makeEntry(int id, String name, String finding,
			int deviation, int risk, String measure) {
		hardcodedData.put(id, new Object[] { id, name, finding, deviation, risk, measure + id });
	}

	public LoadWorstFindingsCommand(int id) {
		this.id = id;
	}

	public Object[][] getResult() {
		return result;
	}

	@Override
	public void execute() {
		result = new Object[][] { hardcodedData.get(id) };
	}

}
