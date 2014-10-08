/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sernet.gs.service.FileUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ReportDepositTest extends CommandServiceProvider {

    private static final String REPORT_DIR = "/sernet/verinice/report/service/impl";
    private static final String RPTSUFFIX = ".rptdesign";
    private static final String DEPOSIT_DIR_PART_1 = "bin" + File.separator + "WEB-INF" + File.separator;
    private static final String DEPOSIT_DIR_PART_2 = "reportDeposit";
    
    @Resource(name = "reportdepositService")
    private IReportDepositService depositService;
    
    @Before
    public void setUp() throws Exception {
        (new File(DEPOSIT_DIR_PART_1 + DEPOSIT_DIR_PART_2)).mkdirs();
    }

    @After
    public void tearDown() throws CommandException {
        FileUtil.deleteDirectory(new File(DEPOSIT_DIR_PART_1 + DEPOSIT_DIR_PART_2)); 
        FileUtil.deleteDirectory(new File(DEPOSIT_DIR_PART_1));   
    }
    
    @Test
    public void testAddToServerDeposit() throws Exception {
        List<ReportTemplateMetaData> addedMetadataList = addAllFilesToDeposit();
        checkMetadataInDeposit(addedMetadataList, true);
    }
    
    @Test
    public void testRemoveFromServer() throws Exception {
        List<ReportTemplateMetaData> addedMetadataList = addAllFilesToDeposit();
        checkMetadataInDeposit(addedMetadataList, true);
        for (ReportTemplateMetaData metadata : addedMetadataList) {
            depositService.removeFromServer(metadata);
        }
        checkMetadataInDeposit(addedMetadataList, false);
    }

    private void checkMetadataInDeposit(List<ReportTemplateMetaData> checkMetadataList, boolean expected) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        Set<ReportTemplateMetaData> metadataSet = depositService.getServerReportTemplates();
        for (ReportTemplateMetaData metadata : checkMetadataList) {
            if(expected) {
                assertTrue("Report metadata not found in deposit: " + metadata.getFilename(), metadataSet.contains(metadata));
            } else {
                assertFalse("Report metadata found in deposit: " + metadata.getFilename(), metadataSet.contains(metadata));
            }
        }
    }

    private List<ReportTemplateMetaData> addAllFilesToDeposit() throws URISyntaxException, IOException {
        URL reportDirectory = ReportDepositTest.class.getResource(REPORT_DIR);
        assertNotNull("Report directory not found: " + REPORT_DIR, reportDirectory);
        File dir = new File(reportDirectory.toURI());
        assertNotNull("Report directory not found: " + REPORT_DIR, dir);
        assertTrue("Report directory path is not a directory: " + REPORT_DIR, dir.isDirectory());
        List<String> rptFileNames = getRptfileList(dir);
        assertNotNull("No RPT files found in directory: " + REPORT_DIR, rptFileNames);
        assertFalse("No RPT files found in directory: " + REPORT_DIR, rptFileNames.isEmpty());
        List<ReportTemplateMetaData> metadataList = new ArrayList<ReportTemplateMetaData>(rptFileNames.size());
        for (String fileName : rptFileNames) {
            metadataList.add(addFileToDeposit(dir, fileName));
        }
        return metadataList;
    }

    private ReportTemplateMetaData addFileToDeposit(File dir, String fileName) throws IOException {
        byte[] fileData = FileUtil.getFileData(new File(dir, fileName));
        ReportTemplateMetaData metadata = new ReportTemplateMetaData(fileName, 
                    fileName.substring(0, fileName.indexOf(".")), 
                    new IReportDepositService.OutputFormat[]{IReportDepositService.OutputFormat.PDF});
        depositService.addToServerDeposit(metadata, fileData);
        return metadata;
    }
    
    private List<String> getRptfileList(File dir) {
        String[] rptFileNames = dir.list(new FilenameFilter() {
          @Override
          public boolean accept(File current, String name) {
            return name.endsWith(RPTSUFFIX);
          }
        });
        return Arrays.asList(rptFileNames);
    }
}
