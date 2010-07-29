package sernet.verinice.report.service.commands;

import sernet.verinice.interfaces.GenericCommand;

@SuppressWarnings("serial")
public class LoadCheckDetailsCommand extends GenericCommand {

	private Object[][] result;

	private int checkId;

	public LoadCheckDetailsCommand(int checkId) {
		this.checkId = checkId;
	}

	public Object[][] getResult() {
		return result;
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
