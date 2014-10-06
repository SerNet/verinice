/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 *
 */
public class ReportSupplierImpl implements IReportSupplier{

    public ReportSupplierImpl(){}

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.reports.IReportSupplier#getReportTemplates()
     */
    @Override
    public List<ReportTemplateMetaData> getReportTemplates() {
        try {
            return Arrays.asList(getReportMetaData(getLocalRptdesigns()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ReportMetaDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (PropertyFileExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ArrayList<ReportTemplateMetaData>(0);
    }

    private ReportTemplateMetaData[] getReportMetaData(String[] rptDesigns) throws IOException, ReportMetaDataException, PropertyFileExistsException{
        List<ReportTemplateMetaData> list = new ArrayList<ReportTemplateMetaData>();
        for(ReportTemplateMetaData metaData : ServiceFactory.lookupReportDepositService().getReportTemplates(getLocalRptdesigns())){
            metaData.setServer(false);
            list.add(metaData);
        }
        List<ReportTemplateMetaData> localList = new ArrayList<ReportTemplateMetaData>();
        for(ReportTemplateMetaData metaData : ServiceFactory.lookupReportDepositService().getReportTemplates(getClientRemoteRptdesigns())){
            metaData.setServer(true);
            localList.add(metaData);
        }
        return list.toArray(new ReportTemplateMetaData[list.size()]);
    }

    private String[] getLocalRptdesigns(){
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(getClientLocalRptdesigns()));
        list.addAll(Arrays.asList(getClientRemoteRptdesigns()));
        return list.toArray(new String[list.size()]);
    }

    private String[] getClientLocalRptdesigns(){
        List<String> list = new ArrayList<String>(0);
        //    DirFilter = null means no subdirectories
        IOFileFilter filter = new SuffixFileFilter("rptdesign", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(CnAWorkspace.getInstance().getLocalReportTemplateDir()), filter, null);
        while(iter.hasNext()){
            list.add(iter.next().getAbsolutePath());
        }
        return list.toArray(new String[list.size()]);
    }

    private String[] getClientRemoteRptdesigns(){
        List<String> list = new ArrayList<String>();
        //          // DirFilter = null means no subdirectories
        IOFileFilter filter = new SuffixFileFilter("rptdesign", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(CnAWorkspace.getInstance().getRemoteReportTemplateDir()), filter, null);
        while(iter.hasNext()){
            list.add(iter.next().getAbsolutePath());
        }
        return list.toArray(new String[list.size()]);
    }

}
