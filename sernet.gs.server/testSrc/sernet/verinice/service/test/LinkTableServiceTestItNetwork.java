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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.ILinkTableService;
import sernet.verinice.service.linktable.LinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableService;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkTableServiceTestItNetwork extends BeforeEachVNAImportHelper {

    private static final Logger LOG = Logger.getLogger(LinkTableServiceTestItNetwork.class);

    private static final String VNA_FILENAME = "LinkTableServiceTestItNetwork.vna";
    
    // A table with all objects per IT network
    private static final String VLT_FILENAME = "LinkTableServiceTestItNetwork.vlt";
    
    // A table with modules and safeguards per risk analysis
    private static final String VLT_FILENAME_RISK_ANALYSIS = "LinkTableServiceTestRiskAnalysis.vlt";

    private static final String SOURCE_ID = "b29b23";
    private static final String EXT_ID_ITVERBUND = "ENTITY_ae7f1b38-3b2d-43d4-9d63-ed2b8a70868c";

    @Resource(name="cnaTreeElementDao")
    protected IBaseDao<CnATreeElement, Long> elementDao;

    ILinkTableService service = new LinkTableService();

    @Test
    public void testItNetwotk() throws CommandException, IOException {
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ITVERBUND);
        testAllObjects(org);
        testRiskAnalysis(org);
    }

    private void testAllObjects(CnATreeElement org) throws CommandException, IOException {      
        ILinkTableConfiguration configuration = VeriniceLinkTableIO.readLinkTableConfiguration(getAllObjectsVltFilePath());
        LinkTableConfiguration changedConfiguration = cloneConfiguration(configuration);
        changedConfiguration.addScopeId(org.getScopeId());

        String tempVltPath = File.createTempFile(this.getClass().getSimpleName(), ".vlt").getAbsolutePath();
        VeriniceLinkTableIO.write(changedConfiguration, tempVltPath);

        List<List<String>> resultTable = service.createTable(tempVltPath);
        checkAllObjectsTable(resultTable);
        FileUtils.deleteQuietly(new File(tempVltPath));
    }

    private void testRiskAnalysis(CnATreeElement org) throws CommandException, IOException {
        ILinkTableConfiguration configuration = VeriniceLinkTableIO.readLinkTableConfiguration(getRiskAnalysisVltFilePath());
        LinkTableConfiguration changedConfiguration = cloneConfiguration(configuration);
        changedConfiguration.addScopeId(org.getScopeId());

        String tempVltPath = File.createTempFile(this.getClass().getSimpleName(), ".vlt").getAbsolutePath();
        VeriniceLinkTableIO.write(changedConfiguration, tempVltPath);

        List<List<String>> resultTable = service.createTable(tempVltPath);
        checkRiskAnalysisTable(resultTable);
        FileUtils.deleteQuietly(new File(tempVltPath));
    }
    

    private void checkAllObjectsTable(List<List<String>> resultTable) {
        assertEquals(79, resultTable.size());
        assertEquals(19, resultTable.get(0).size());
        
        assertEquals("RECPLAST", resultTable.get(72).get(0));
        assertEquals("Anwendungen", resultTable.get(72).get(1));
        assertEquals("Internet-Recherche", resultTable.get(72).get(2));

        assertEquals("RECPLAST", resultTable.get(62).get(0));
        assertEquals("Gebäude", resultTable.get(62).get(3));
        assertEquals("Produktionshalle", resultTable.get(62).get(4));

        assertEquals("RECPLAST", resultTable.get(40).get(0));
        assertEquals("IT-Systeme: Server", resultTable.get(40).get(9));
        assertEquals("Datei- und Druckserver", resultTable.get(40).get(10));

        assertEquals("RECPLAST", resultTable.get(31).get(0));
        assertEquals("Mitarbeiter", resultTable.get(31).get(13));
        assertEquals("A. Admin", resultTable.get(31).get(14));
    }
    
    private void checkRiskAnalysisTable(List<List<String>> resultTable) {
        if (LOG.isDebugEnabled()) {
            int i = 0;
            for (List<String> list : resultTable) {
                StringBuilder sb = new StringBuilder();
                sb.append(i).append(" - ");
                for (String cell : list) {
                    sb.append(cell).append(", ");
                }
                LOG.debug(sb);
                i++;
            }
        }
        assertTrue(resultTable.size()==20 || resultTable.size()==21);
        assertEquals(6, resultTable.get(0).size());
        
        assertEquals("Clients Entwicklungsabteilung", resultTable.get(2).get(1));
        assertEquals("Risikoanalyse", resultTable.get(2).get(2));
        assertEquals("Abhören der elektromagnetischen Abstrahlung von IT-Komponenten", resultTable.get(2).get(3));
        assertEquals("bM 1.99", resultTable.get(2).get(4));
        assertEquals("Verringerung der elektromagnetischen Abstrahlung von IT-Geräten", resultTable.get(2).get(5));
     
    }

    private LinkTableConfiguration cloneConfiguration(ILinkTableConfiguration configuration) {
        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        builder.setColumnPathes(configuration.getColumnPaths())
        .setLinkTypeIds(configuration.getLinkTypeIds());
        if(configuration.getScopeIdArray()!=null) {
            builder.setScopeIds(new HashSet<>(Arrays.asList(configuration.getScopeIdArray())));
        }
        return builder.build();
    }

    protected String getAllObjectsVltFilePath() {
        return getFilePath(VLT_FILENAME);
    }
    
    protected String getRiskAnalysisVltFilePath() {
        return getFilePath(VLT_FILENAME_RISK_ANALYSIS);
    }

    @Override
    protected String getFilePath() {
        return getFilePath(VNA_FILENAME);
    }

    private String getFilePath(String fileName) {
        return this.getClass().getResource(fileName).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
       return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
}
