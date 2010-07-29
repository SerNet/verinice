package sernet.verinice.report.service.commands;

import sernet.verinice.interfaces.GenericCommand;

@SuppressWarnings("serial")
public class LoadISOChapterList extends GenericCommand {

	private Object[][] result;

	private int id;

	public LoadISOChapterList(int id) {
		this.id = id;
	}

	public Object[][] getResult() {
		return result;
	}

	@Override
	public void execute() {
		switch (id) {
		case 0:
			// ISO/IEC
			result = new Object[][] {
					makeEntry(1, "Security Policy"),
					makeEntry(2, "Organization of Information Security"),
					makeEntry(3, "Asset Management"),
					makeEntry(5, "Physical and Environmental Security"),
					makeEntry(6, "Communications and Operations Management"),
					makeEntry(7, "Access Control"),
					makeEntry(8,
							"Information Systems Acquisition, Development and Maintenance"),
					makeEntry(9, "Information Security Incident Management"),
					makeEntry(10, "Business Continuity Management") };
			break;
		case 1:
			// Checks
			result = new Object[][] {
					makeEntry(1, "Server room #1"),
					makeEntry(2, "Server room #2"),
					makeEntry(3, "Server room #3"),
			};
		}
	}

	private Object[] makeEntry(int chapterId, String chapterName) {
		return new Object[] { chapterId, chapterName };
	};

}
