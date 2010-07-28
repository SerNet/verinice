package sernet.verinice.report.service.commands;

import sernet.verinice.interfaces.GenericCommand;

@SuppressWarnings("serial")
public class LoadWorstFindingsCommand extends GenericCommand {

	private Object[][] result;

	private int type;

	public LoadWorstFindingsCommand(int type) {
		this.type = type;
	}

	public Object[][] getResult() {
		return result;
	}

	@Override
	public void execute() {
		switch (type) {
		case 0:
			// ISO/IEC chapters
			result = new Object[][] {
					makeEntry(1, "Security Policy", "finding", 0, 1, "measure"),
					makeEntry(2, "Organization of Information Security",
							"finding", 1, 2, "measure"),
					makeEntry(3, "Asset Management", "finding", 2, 3, "measure"),
					makeEntry(4, "Human Resources Security", "finding", 0, 0,
							"measure"),
					makeEntry(5, "Physical and Environmental Security",
							"finding", 1, 0, "measure"),
					makeEntry(6, "Communications and Operations Management",
							"finding", 2, 2, "measure"),
					makeEntry(7, "Access Control", "finding", 2, 1, "measure"),
					makeEntry(
							8,
							"Information Systems Acquisition, Development and Maintenance",
							"finding", 1, 1, "measure"),
					makeEntry(9, "Information Security Incident Management",
							"finding", 0, 2, "measure"),
					makeEntry(10, "Business Continuity Management", "finding",
							0, 0, "measure") };
			break;
		default:
		case 1:
			// IT rooms
			result = new Object[][] {
					makeEntry(0, "Serverroom #1", "Package material (combustible material) inside the data center.", 1, 3, "Remove package material.\nRecommendation:\nRemove own data center and move the server to the new data center, because of cost & security synergies."), 
					makeEntry(1, "Serverroom #2", "Package material (combustible material) inside the data center.", 1, 2, "Remove package material.\nRecommendation:\nRemove own data center and move the server to the new data center, because of cost & security synergies."), 
					makeEntry(2, "Serverroom #3", "Package material (combustible material) inside the data center.", 1, 1, "Remove package material.\nRecommendation:\nRemove own data center and move the server to the new data center, because of cost & security synergies.") 
			};
			break;
		}
	}

	private Object[] makeEntry(int no, String name, String finding,
			int deviation, int risk, String measure) {
		return new Object[] { no, name, finding, deviation, risk, measure + no };
	};

}
