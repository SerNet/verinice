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

import java.io.IOException;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportService;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;


public class ReportService implements IReportService {
	
	private IReportType[] reportTypes;
	
	private static final Logger LOG = LoggerFactory.getLogger(ReportService.class);
	
    private static final String PROPERTIES_FILE_EXTENSION = "properties";
    private static final char EXTENSION_SEPARATOR_CHAR = '.';
    
    private static final String PROPERTIES_FILENAME = "filename";
    private static final String PROPERTIES_OUTPUTFORMATS = "outputformats";
    private static final String PROPERTIES_OUTPUTNAME = "outputname";
    
	/*
	 * (non-Javadoc)
	 * @see sernet.verinice.interfaces.report.IReportService#getReportTypes()
	 * 
	 * List built-in reports offered by this report service.
	 * 
	 */
	@Override
	public IReportType[] getReportTypes() {
		if (reportTypes == null){
			reportTypes = new IReportType[] { 
//		        new UserReportType(), 
//		        new SamtReportType(), 
//		        new SamtComplianceReport(),
//		        new ISAActionReport(),
//		        new ComprehensiveSamtReportType(),
//		        
//		        new ISMRiskManagementResultsReport(), // ISO 27k1 Reports (english)
//		        new ISMRiskManagementResultsReportDe(), // ISO 27k1 Report (german)
//		        
//		        new RiskTreatmentReport(),
//		        
//		        new ControlMaturityReport(),
//		        new StatementOfApplicabilityReport(),
//		        new InventoryOfAssetsReport(),
//		      		        
//		        new VulnerabilitiesReport(), // new Export Reports, since v1.6.0
//		        new ThreatsReport(),
//		        new ScenariosReport(),
//		        new ResponsesReport(),
//		        new RequirementsReport(),
//		        new RecordsReport(),
//		        new ProcessesReport(),
//		        new PersonsReport(),
//		        new IncidentsReport(),
//		        new ExceptionsReport(),
//		        new DocumentsReport(),
//		        new TasksReport(),//Tasks in english
//		        new AufgabenReport(),//Aufgaben in deutsch
//                new VVBSIG_SofortMeldungReport(),
//                new VVBSIG_StatMeldungReport(),		        
//		        
//		        new StrukturanalyseReport(), // BSI reports
//		        new AbhaengigkeitenReport(),
//		        new AllItemsReport(), // this is report ID "schutzbedarf"
//		        new ModellierungReport(),
//		        new BasisSichCheckReport(),
//		        new ErgaenzendeSicherheitsanalyseReport(),
//		        new GSRisikoanalyseReport(),
//		        new ManagementRisikoBewertung(),
//		        new RealisierungsplanReport(),
//		        new GraphischerUmsetzungsstatusReport(),
//		        new AuditberichtReport()
		        new GenericReportType()
		    };
		}
		return reportTypes.clone();
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportService#getOutputFormat(java.lang.String)
     */
    @Override
    public IOutputFormat getOutputFormat(String formatLabel) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportService#getOutputFormats(java.lang.String[])
     */
    @Override
    public IOutputFormat[] getOutputFormats(String[] formatLabel) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.report.IReportService#getReportTemplates(java.lang.String[])
     */
    @Override
    public ReportTemplateMetaData[] getReportTemplates(String[] rptDesignFiles) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return null;
    }

    @Override
    public IRenderOption getRenderOptions(String format){
        if("pdf".equalsIgnoreCase(format)){
            return getPDFRenderOption();
        } else if("xls".equalsIgnoreCase(format)){
            return getXLSRenderOption();
        } else if("doc".equalsIgnoreCase(format)){
            return getDOCRenderOption();
        } else if("html".equalsIgnoreCase(format)){
            return getHTMLRenderOption();
        } else if("odt".equalsIgnoreCase(format)){
            return getODTRenderOption();
        } else if("ods".equalsIgnoreCase(format)){
            return getODSRenderOption();
        }
        return null;
    }
    
    private IRenderOption getPDFRenderOption(){
        PDFRenderOption pdfOptions = new PDFRenderOption();
        pdfOptions.setOutputFormat("pdf");
        pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES);
        return pdfOptions;
    }
    
    private IRenderOption getXLSRenderOption(){
        EXCELRenderOption excelOptions = new EXCELRenderOption();
        excelOptions.setOutputFormat("xls");
        return excelOptions;
    }
    
    private IRenderOption getDOCRenderOption(){
        RenderOption options = new RenderOption();
        options.setOutputFormat("doc");
        return options;
    }
    
    private IRenderOption getHTMLRenderOption(){
        HTMLRenderOption htmlOptions = new HTMLRenderOption();
        htmlOptions.setHtmlPagination(false);
        htmlOptions.setOutputFormat("html");
        htmlOptions.setImageDirectory(".");
        return htmlOptions;
    }
    
    private IRenderOption getODSRenderOption(){
        RenderOption options = new RenderOption();
        options.setOutputFormat("ods");
        return options;
    }
	
    private IRenderOption getODTRenderOption(){
        RenderOption options = new RenderOption();
        options.setOutputFormat("odt");
        return options;
    }
    
}
