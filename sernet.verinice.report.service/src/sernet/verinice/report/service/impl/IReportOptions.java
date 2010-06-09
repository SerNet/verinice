package sernet.verinice.report.service.impl;

import java.io.File;

/**
 * Collection of options that is used to set up a report generation process.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public interface IReportOptions {
	
	IOutputFormat getOutputFormat();

	File getOutputFile();
	
	boolean isToBeCompressed();
	
	boolean isToBeEncrypted();
	
}
