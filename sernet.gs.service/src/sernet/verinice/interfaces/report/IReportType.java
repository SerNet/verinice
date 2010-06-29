package sernet.verinice.interfaces.report;

public interface IReportType {

	/**
	 * Returns an application usable id.
	 * 
	 * @return
	 */
	String getId();

	/**
	 * Returns a humand-readable name for this report type.
	 * 
	 * @return
	 */
	String getLabel();

	/**
	 * Returns a human-readable description of this report type.
	 * 
	 * @return
	 */
	String getDescription();

	/**
	 * Retrieves the possible output formats.
	 * 
	 * @return
	 */
	IOutputFormat[] getOutputFormats();
	
	void createReport(IReportOptions reportOptions);
}
