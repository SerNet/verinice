package sernet.verinice.report.service.commands;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads the available elements of {@link CnATreeElement} (?) that are eligible
 * for a security assesment. They are referenced as chapters since each element
 * turns into a chapter in the 'comprehensive security assessment report'.
 * 
 * TODO: The input value is arbitrarily chosen to be an integer. It is likely
 * that this needs to be the name of an entity. The result of this command is
 * likely to be a list of the children of the initial entity.
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
		case 0:
			/* 
			 * Chapters designated for the ISO/IEC 
			 */
			result = new Object[][] {
					makeEntry(100, "ISO/IEC Overview"),
			};
			break;
		/* 
		 * Chapters designated for the IT check *details*
		 */
		case 1:
			result = new Object[][] {
					makeEntry(200, "IT rooms"),
					makeEntry(300, "Office places"),
					makeEntry(400, "System checks")
			};
			break;
		/*
		 * Sort of main chapters
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
