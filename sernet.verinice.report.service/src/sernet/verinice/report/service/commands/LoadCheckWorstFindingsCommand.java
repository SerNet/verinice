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

import sernet.verinice.interfaces.GenericCommand;

/**
 * Retrieves the worst finding of a 'check' item
 * 
 * UPDATE: Check 'items' are organized in ordinary ControlGroups, which have to have controlgroup_is_NoIso_group (equals 1) set
 * so this class is useless
 * 
 * @author Robert Schuster <r.schuster@tarent.de>,
 * 		   Sebastian Hagedorn <sh@sernet.de>
 *
 */
@SuppressWarnings("serial")
@Deprecated
public class LoadCheckWorstFindingsCommand extends GenericCommand {

	private Object[][] result;

	private int checkId;

	public LoadCheckWorstFindingsCommand(int checkId) {
		this.checkId = checkId;
	}

	public Object[][] getResult() {
		return (result != null) ? result.clone() : null;
	}

	@Override
	public void execute() {
		switch (checkId) {
		case 0:
			// Office places
			result = new Object[][] {
					makeEntry("8th floor", null, 0, 0, null),
					makeEntry("9th floor", "Unerwünschtes Etwas", 1, 2, "Beseitigung des unerwünschtes Etwas")
			};
			break;
		default:
		case 1:
			// System checks
			result = new Object[][] {
					makeEntry("System checks", "<ul><li>Service packs and hotfixes are installed on all client systems automatically.</li><li>Antivirus signatures are updated automatically at minimum once a day and up-to-date at all active	systems.</li></ul>", 1, 1, "Nothing") 
			};
			break;
		}
	}

	private Object[] makeEntry(String checkName, String finding,
			int deviation, int risk, String measure) {
		return new Object[] { checkName, finding, deviation, risk, measure };
	};

}
