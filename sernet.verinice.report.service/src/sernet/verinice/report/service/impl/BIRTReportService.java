package sernet.verinice.report.service.impl;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;

public class BIRTReportService {
	
	Logger log = Logger.getLogger(BIRTReportService.class.getName());
	
	IReportEngine engine;
	
	public BIRTReportService() {
		EngineConfig config = new EngineConfig();

		IReportEngineFactory factory = (IReportEngineFactory) Platform
				.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		
		engine = factory.createReportEngine(config);
	}
	
	public IRunAndRenderTask createTask(URL rptDesignURL)
	{
		
		IRunAndRenderTask task = null;
		try {
			IReportRunnable design = engine.openReportDesign(rptDesignURL.openStream());
			task = engine.createRunAndRenderTask(design);
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not open report design: " + e);
			throw new IllegalStateException(e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not open report design: " + e);
			throw new IllegalStateException(e);
		}
		
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, BIRTReportService.class.getClassLoader());
		
		return task;
	}
	
	public void render(IRunAndRenderTask task, IReportOptions options)
	{
		IRenderOption renderOptions = ((AbstractOutputFormat) options.getOutputFormat()).createBIRTRenderOptions();
		renderOptions.setOutputFileName(options.getOutputFile().getAbsolutePath());
		
		task.setRenderOption(renderOptions);
		
		try {
			task.run();
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not render design: " + e);
			throw new IllegalStateException(e);
		}
	}

}
