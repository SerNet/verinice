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

import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RemoveLink;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * ISO27000 Business Impact Inheritence test.
 * See https://wiki.sernet.private/wiki/Verinice/Business_Impact_Inheritence/de
 * for a description of the test cases. 
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class BusinessImpactInheritenceTest extends BeforeEachVNAImportHelper {

    private static final Logger LOG = Logger.getLogger(BusinessImpactInheritenceTest.class);
    
    private static final String VNA_FILENAME = "BusinessImpactInheritenceTest.vna";
    
    private static final String SOURCE_ID = "Unit-Test";
    private static final String EXT_ID_EXTERNE_KOMMUNIKATION = "ENTITY_10473";
    private static final String EXT_ID_INTERNE_KOMMUNIKATION = "ENTITY_10441";
    private static final String EXT_ID_DOKUMENTENMANAGEMENT = "ENTITY_10505";
    private static final String EXT_ID_NETWORK_SWITCH = "ENTITY_10559";
    private static final String EXT_ID_VMWARE_GUEST_1 = "ENTITY_10314";
    private static final String EXT_ID_VMWARE_GUEST_2 = "ENTITY_10376";
    
    @Test
    public void testRemoveElement() throws Exception {
        
        Asset asset = (Asset) loadElement(SOURCE_ID, EXT_ID_VMWARE_GUEST_1);
        checkCIA(asset, 2, 2, 4);
        
        Process process = (Process) loadElement(SOURCE_ID, EXT_ID_DOKUMENTENMANAGEMENT);    
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(process);
        commandService.executeCommand(removeCommand);
        
        asset = (Asset) loadElement(SOURCE_ID, EXT_ID_VMWARE_GUEST_1);
        checkCIA(asset, 2, 2, 2);
    }
    
    @Test
    public void testAddAndRemoveLink() throws Exception {
        
        Asset asset = (Asset) loadElement(SOURCE_ID, EXT_ID_VMWARE_GUEST_2);
        setInheritence(asset, true);
        checkCIA(asset, 2, 2, 4);
        
        // remove link
        removeLinksToProcess(asset);
        asset = (Asset) loadElement(SOURCE_ID, EXT_ID_VMWARE_GUEST_2);
        checkCIA(asset, 0, 0, 0);
        
        // add link
        Process process = (Process) loadElement(SOURCE_ID, EXT_ID_INTERNE_KOMMUNIKATION);
        CreateLink<Process,Asset> command = new CreateLink<Process,Asset>(process, asset, Process.REL_PROCESS_ASSET, "BusinessImpactInheritenceTest");
        command = commandService.executeCommand(command);
        asset = (Asset) loadElement(SOURCE_ID, EXT_ID_VMWARE_GUEST_2);
        checkCIA(asset, 2, 2, 2);
        
        // remove link again to reset database state
        removeLinksToProcess(asset);
        asset = (Asset) loadElement(SOURCE_ID, EXT_ID_VMWARE_GUEST_2);
        checkCIA(asset, 0, 0, 0);
    }
    
    @Test
    public void testChangeBiNumber() throws Exception {
        
        Asset asset = (Asset) loadElement(SOURCE_ID, EXT_ID_NETWORK_SWITCH);
        checkCIA(asset, 2, 2, 4);
        
        Process process = (Process) loadElement(SOURCE_ID, EXT_ID_EXTERNE_KOMMUNIKATION);
        setCIA(process, 3, 2, 4);
        
        asset = (Asset) loadElement(SOURCE_ID, EXT_ID_NETWORK_SWITCH);
        checkCIA(asset, 3, 2, 4);
    }
    
    @Test
    public void testChangeInheritence() throws Exception {
        
        Asset asset = (Asset) loadElement(SOURCE_ID, EXT_ID_VMWARE_GUEST_2);
        checkCIA(asset, 1, 1, 1);
        
        setInheritence(asset, true);
        checkCIA(asset, 2, 2, 4);
        
        setInheritence(asset, false);
        setCIA(asset, 1, 1, 1);
        checkCIA(asset, 1, 1, 1);
    }
    
    private void removeLinksToProcess(Asset asset) throws CommandException {
        Set<CnALink> links = asset.getLinksUp();
        for (CnALink link : links) {
            if(Process.REL_PROCESS_ASSET.equals(link.getRelationId())) {
                RemoveLink removeLink = new RemoveLink(link);
                removeLink = commandService.executeCommand(removeLink);
            }
        }
    }
    
    private void setInheritence(Asset asset, boolean inheritence) throws CommandException {
        int value = (inheritence) ? 1 : 0;
        asset.setNumericProperty(Asset.ASSET_VALUE_METHOD_AVAILABILITY, value);
        asset.setNumericProperty(Asset.ASSET_VALUE_METHOD_CONFIDENTIALITY, value);
        asset.setNumericProperty(Asset.ASSET_VALUE_METHOD_INTEGRITY, value);
        updateElement(asset);
    }

    private void setCIA(Asset asset, int c, int i, int a) throws CommandException {
        asset.setNumericProperty(Asset.ASSET_VALUE_AVAILABILITY, a);
        asset.setNumericProperty(Asset.ASSET_VALUE_CONFIDENTIALITY, c);
        asset.setNumericProperty(Asset.ASSET_VALUE_INTEGRITY, i);
        updateElement(asset);
    }

    private void setCIA(Process process, int c, int i, int a) throws CommandException {
        process.setNumericProperty(Process.PROCESS_VALUE_AVAILABILITY, a);
        process.setNumericProperty(Process.PROCESS_VALUE_CONFIDENTIALITY, c);
        process.setNumericProperty(Process.PROCESS_VALUE_INTEGRITY, i);
        updateElement(process);
    }

    private void checkCIA(Asset element, int c, int i, int a) {
        assertEquals("Availability of element is not " + a, a, element.getNumericProperty(Asset.ASSET_VALUE_AVAILABILITY));
        assertEquals("Confidentiality of element is not " + c, c, element.getNumericProperty(Asset.ASSET_VALUE_CONFIDENTIALITY));
        assertEquals("Integrity of element is not " + i, i, element.getNumericProperty(Asset.ASSET_VALUE_INTEGRITY));
    }
    
    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
       return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
}
