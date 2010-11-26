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
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads the ids and names of various elements of {@link CnATreeElement} (?) that are eligible
 * for a security assesment. They are referenced as chapters since each element
 * turns into a chapter in the 'comprehensive security assessment report'.
 * 
 * TODO samt: Check the comments for explanations of where the ids come from and what they
 * are supposed to mean.  
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 */
@SuppressWarnings("serial")
public class LoadChapterListCommand extends GenericCommand {

	private Object[][] result;

	private int id;

	public LoadChapterListCommand(int id) {
		this.id = id;
	}

	public Object[][] getResult() {
		return result;
	}

	@Override
	public void execute() {
		switch (id) {
		/*
		 * The id used here is more or less a magic number. Maybe a customized command
		 * that finds the elements for this part of the report makes more sense.
		 */
		case 0:
			/* 
			 * Chapters designated for the ISO/IEC
			 */
			result = new Object[][] {
					makeEntry(100, "ISO/IEC Overview"),
			};
			break;
		/*
		 * The id used here is more or less a magic number. Maybe a customized command
		 * that finds the elements for this part of the report makes more sense.
		 */
		case 1:
			/* 
			 * Chapters designated for the IT check *details*
			 */
			result = new Object[][] {
					makeEntry(200, "IT rooms"),
					makeEntry(300, "Office places"),
					makeEntry(400, "System checks")
			};
			break;
		/*
		 * Sort of main chapters
		 * 
		 * The ids used here are supposed to be database ids that are the result of a previous
		 * request.
		 * 
		 * The 100 is a slight exception as it is directly used to find the subchapters for
		 * chapter 4. It should be a goal to get of this direct usage when a better understanding
		 * has developed about the nature of the data.
		 */
		case 100:
			// ISO/IEC
			result = new Object[][] {
					makeEntry(1001, "Security Policy"),
					makeEntry(1002, "Organization of Information Security"),
					makeEntry(1003, "Asset Management"),
					makeEntry(1005, "Physical and Environmental Security"),
					makeEntry(1006, "Communications and Operations Management"),
					makeEntry(1007, "Access Control"),
					makeEntry(1008,
							"Information Systems Acquisition, Development and Maintenance"),
					makeEntry(1009, "Information Security Incident Management"),
					makeEntry(1010, "Business Continuity Management") };
			break;
		case 200:
			// IT rooms
			result = new Object[][] {
					makeEntry(2001, "Server room #1"),
					makeEntry(2002, "Server room #2"),
					makeEntry(2003, "Server room #3"),
					makeEntry(2004, "Workstationroom #1"), };
			break;
		case 300:
			// Office places
			result = new Object[][] { makeEntry(3001, "8th floor"),
					makeEntry(3002, "9th floor"), };
			break;
		case 400:
			// Office places
			result = new Object[][] { makeEntry(4001, "System checks"), };
			break;
		/* 
		 * Subchapters of the ISO/IEC part - correspond to constants in LoadAllFindingsCommand
		 * 
		 * From now one the ids resemble a parent->child relation in the database.
		 */
		case 1001:
			result = new Object[][] {
					makeEntry(10011, "1001-1")
			};
			break;
		case 1002:
			result = new Object[][] {
					makeEntry(10021, "1002-1")
			};
			break;
		case 1003:
			result = new Object[][] {
					makeEntry(10031, "1003-1"),
					makeEntry(10032, "1003-2"),
					makeEntry(10033, "1003-3")
			};
			break;
		case 1004:
			result = new Object[][] {
					makeEntry(10041, "1004-1"),
					makeEntry(10042, "1004-2"),
			};
			break;
		case 1005:
			result = new Object[][] {
					makeEntry(10051, "1005-1"),
					makeEntry(10052, "1005-2"),
			};
			break;
		case 1006:
			result = new Object[][] {
					makeEntry(10061, "1006-1"),
					makeEntry(10062, "1006-2"),
					makeEntry(10063, "1006-3"),
					makeEntry(10063, "1006-4")
			};
			break;
		case 1007:
			result = new Object[][] {
					makeEntry(10071, "1007-1"),
					makeEntry(10072, "1007-2"),
			};
			break;
		case 1008:
			result = new Object[][] {
					makeEntry(10081, "1008-1"),
					makeEntry(10082, "1008-2"),
					makeEntry(10083, "1008-3"),
					makeEntry(10083, "1008-4"),
					makeEntry(10085, "1008-5")
			};
			break;
		case 1009:
			result = new Object[][] {
					makeEntry(10091, "1009-1"),
					makeEntry(10092, "1009-2"),
					makeEntry(10093, "1009-3")
			};
			break;
		case 1010:
			result = new Object[][] {
					makeEntry(10101, "1010-1"),
					makeEntry(10102, "1010-2"),
			};
			break;
		}
	}

	private Object[] makeEntry(int chapterId, String chapterName) {
		return new Object[] { chapterId, chapterName };
	};

}
