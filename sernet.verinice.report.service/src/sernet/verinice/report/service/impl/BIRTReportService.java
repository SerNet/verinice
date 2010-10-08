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
package sernet.verinice.report.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IDataExtractionOption;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IResultSetItem;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.IScalarParameterDefn;
import org.eclipse.birt.report.model.api.DefaultResourceLocator;
import org.eclipse.birt.report.model.api.IResourceLocator;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.ModuleOption;

import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.oda.driver.impl.Query;
import sernet.verinice.report.service.Activator;

public class BIRTReportService {
	
	Logger log = Logger.getLogger(BIRTReportService.class.getName());
	
	IReportEngine engine;

    private IReportRunnable design;
    
    private IResourceLocator resourceLocator;

	public BIRTReportService() {
		EngineConfig config = new EngineConfig();
		
		// Custom resource locator which tries to retrieve resources for the reports
		// from the *package* where the BIRTReportService class resides.
		resourceLocator = new IResourceLocator() {
			IResourceLocator defaultLocator = new DefaultResourceLocator();

			@Override
			public URL findResource(ModuleHandle moduleHandle, String fileName,
					int type, Map appContext) {
				URL url = findByClassloader(fileName);
				if (url == null)
					url = defaultLocator.findResource(moduleHandle, fileName, type, appContext);

				if (url == null && log.isLoggable(Level.WARNING))
					log.warning(String.format("Report resource '%s' could not neither be found through internal resource loader nor through the default one.", fileName));
				return url;
			}
			
			@Override
			public URL findResource(ModuleHandle moduleHandle, String fileName, int type) {
				URL url = findByClassloader(fileName);
				if (url == null)
					url = defaultLocator.findResource(moduleHandle, fileName, type);

				if (url == null && log.isLoggable(Level.WARNING))
					log.warning(String.format("Report resource '%s' could not neither be found through internal resource loader nor through the default one.", fileName));
				return url;
			}
			
			/**
			 * Finds resources in package of class BIRTReportService.
			 * 
			 * <p>Important: If report resource are moved into a different package
			 * this method *must* be adjusted.</p>
			 * 
			 * @param resource
			 * @return
			 */
			private URL findByClassloader(String resource)
			{
				return BIRTReportService.class.getResource(resource);
			}
			
		};
		
		HashMap hm = config.getAppContext();
		hm.put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, BIRTReportService.class.getClassLoader());
		
		config.setAppContext(hm);


		IReportEngineFactory factory = (IReportEngineFactory) Platform
				.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		
		engine = factory.createReportEngine(config);
	}
	
	public IRunAndRenderTask createTask(URL rptDesignURL)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(ModuleOption.RESOURCE_LOCATOR_KEY, resourceLocator);
		
		IRunAndRenderTask task = null;
		try {
			design = engine.openReportDesign(null, rptDesignURL.openStream(), map);
			task = engine.createRunAndRenderTask(design);
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not open report design: " + e);
			throw new IllegalStateException(e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not open report design: " + e);
			throw new IllegalStateException(e);
		}
		
		return task;
	}
	
	public IDataExtractionTask createExtractionTask(URL rptDesignURL)
	{
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(ModuleOption.RESOURCE_LOCATOR_KEY, resourceLocator);
		
		IRunTask task = null;
		try {
			IReportRunnable design = engine.openReportDesign(null, rptDesignURL.openStream(), map);
			task = engine.createRunTask(design);
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not open report design: " + e);
			throw new IllegalStateException(e);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not open report design: " + e);
			throw new IllegalStateException(e);
		}
		
		task.getAppContext().put(EngineConstants.APPCONTEXT_CLASSLOADER_KEY, BIRTReportService.class.getClassLoader());
		
		File f;
		try {
			f = File.createTempFile("verinice", ".rptdocument");
		} catch (IOException e) {
			log.log(Level.SEVERE, "Could not create temporary file for report document.");
			throw new IllegalStateException(e);
		}
		
		try {
			task.run(f.getAbsolutePath());
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not create report: " + e);
			throw new IllegalStateException(e);
		}
		
		task.close();
		
		IReportDocument document = null;
		try {
			document = engine.openReportDocument(f.getAbsolutePath());
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not open report document: " + e);
			throw new IllegalStateException(e);
		}
		
		return engine.createDataExtractionTask(document);
	}
	
	@SuppressWarnings("unchecked")
	public void extract(IDataExtractionTask task, IReportOptions options, int resultSetIndex)
	{
		IDataExtractionOption extractionOptions = ((AbstractOutputFormat) options.getOutputFormat()).createBIRTExtractionOptions();
		try {
			extractionOptions.setOutputStream(new FileOutputStream(options.getOutputFile()));
		} catch (FileNotFoundException e) {
			log.log(Level.SEVERE, "Could not prepare output stream: " + e);
			throw new IllegalStateException(e);
		}
		
		//Choose first result set
		List<IResultSetItem> resultSetList;
		try {
			resultSetList = (List<IResultSetItem>) task.getResultSetList();
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not prepare extraction: " + e);
			throw new IllegalStateException(e);
		}
		IResultSetItem resultItem = (IResultSetItem) resultSetList.get( resultSetIndex );
		task.selectResultSet(resultItem.getResultSetName());
		
		try {
			task.extract(extractionOptions);
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not extract data: " + e);
			throw new IllegalStateException(e);
		} catch (BirtException e) {
			log.log(Level.SEVERE, "Could not extract data: " + e);
			throw new IllegalStateException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void render(IRunAndRenderTask task, IReportOptions options)
	{
		IRenderOption renderOptions = ((AbstractOutputFormat) options.getOutputFormat()).createBIRTRenderOptions();
		renderOptions.setOutputFileName(options.getOutputFile().getAbsolutePath());

		// Makes the chosen root element available via the appContext variable 'rootElementId'
		task.getAppContext().put(IVeriniceOdaDriver.ROOT_ELEMENT_ID_NAME, options.getRootElement());

		task.setRenderOption(renderOptions);
		
		try {
			task.run();
		} catch (EngineException e) {
			log.log(Level.SEVERE, "Could not render design: " + e);
			throw new IllegalStateException(e);
		}
	}

}
