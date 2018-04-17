/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.service.bp.importer.BpImporter;

public class TestAction extends Action {

    public static final String ID = "sernet.gs.ui.rcp.main.testaction"; //$NON-NLS-1$
    
    private static final Logger LOG = Logger.getLogger(TestAction.class);

    public TestAction(IWorkbenchWindow window, String label, String typeID, Integer dbID) {
        setText(label);
        setId(ID);
        
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));
        setEnabled(false);
        CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {
            public void closed(BSIModel model) {
                setEnabled(false);
            }
            public void loaded(BSIModel model) {
                setEnabled(true);
            }
            public void loaded(ISO27KModel model) {
                setEnabled(true);               
            }
            @Override
            public void loaded(BpModel model) {
                setEnabled(true);
            }

            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Activator.inheritVeriniceContextState();
        try {
            
            StringBuilder allsb = new StringBuilder();
            
            
//            LoadReportProcessesWithRisk cmd = new LoadReportProcessesWithRisk(152);
//            cmd = ServiceFactory.lookupCommandService().executeCommand(cmd);
//            List<List<String>> result2 = cmd.getResult();
//            
//            LoadReportAllRisksForScope command = new LoadReportAllRisksForScope(152);
//            command = ServiceFactory.lookupCommandService().executeCommand(command);
//            List<List<String>> result2 = command.getResult();
            
            
//            LoadReportCountRisksBySeverity command = new LoadReportCountRisksBySeverity(152, 'c', 3);
//            command = ServiceFactory.lookupCommandService().executeCommand(command);
//            List<List<Object>> result2 = command.getResult();

            
//            LoadReportProcessesWithRisk command = new LoadReportProcessesWithRisk(152);
//            command = ServiceFactory.lookupCommandService().executeCommand(command);
//            List<List<String>> result2 = command.getResult();
//            
////            
//            for (List<String> row : result2) {
//                for (String col : row) {
//                    allsb.append(col + ":");
//                }
//                allsb.append("\n");
//            }

//            
//                        for (List<Object> row : result2) {
//              for (Object col : row) {
//                  allsb.append(col + ":");
//              }
//              allsb.append("\n");
//          }
//            
//            for (Integer[] row : countC) {
//                for (Integer col : row) {
//                    allsb.append(col + ":");
//                }
//                allsb.append("\n");
//            }
            
            
//            LoadReportElements c2 = new LoadReportElements("riskanalysis", this.dbID);
//            c2 = ServiceFactory.lookupCommandService().executeCommand(c2);
//            List<CnATreeElement> elements = c2.getElements();

//            for (CnATreeElement riskana : elements) {
//                LoadReportRiskAnalysis cmd = new LoadReportRiskAnalysis(riskana.getDbId());
//                cmd = ServiceFactory.lookupCommandService().executeCommand(cmd);
//                cmd.getAllGefaehrdungsUmsetzungen();
//                cmd.getAssociatedGefaehrdungen();
//                List<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen = cmd.getNotOKGefaehrdungsUmsetzungen();
//                cmd.getZielObjekt();
//                
//                for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : notOKGefaehrdungsUmsetzungen) {
//                    LoadReportElementWithChildren cmd2 = new LoadReportElementWithChildren("mnums", gefaehrdungsUmsetzung.getDbId());
//                    cmd2 = ServiceFactory.lookupCommandService().executeCommand(cmd2);
//                    ArrayList<CnATreeElement> result2 = cmd2.getResult();
//                    result2=null;
//                    
//                }
//                
//            }
//            
//            
//            LoadReportElementList cmd = new LoadReportElementList(typeID, dbID);
//            cmd = ServiceFactory.lookupCommandService().executeCommand(cmd);
//            List<List<String>> elements = cmd.getResult();
//            
//            for (List<String> list : elements) {
//                Integer myDbid = Integer.parseInt(list.get(0));
//                LoadReportElementWithLinks command = new LoadReportElementWithLinks(null, myDbid);
//                command = ServiceFactory.lookupCommandService().executeCommand(command);
//                List<List<String>> result = command.getResult();
//                allsb.append(list.get(1)).append("\n");
//                allsb.append(print(result)).append("\n");
//                
//            }
            
//          LoadReportElementWithLinks command = new LoadReportElementWithLinks(null, 152);
//          command = ServiceFactory.lookupCommandService().executeCommand(command);
//          List<List<String>> result = command.getResult();
            
//            LoadReportScenarioDetails command = new LoadReportScenarioDetails(IncidentScenario.TYPE_ID, 1232 );
//            command = ServiceFactory.lookupCommandService().executeCommand(command);
//            List<List<String>> result2 = command.getResult();
//            
//            for (List<String> row : result2) {
//              for (String col : row) {
//                  allsb.append(col + ":");
//              }
//              allsb.append("\n");
//            }
            
            BpImporter importer = new BpImporter("/home/sh/Schreibtisch/issues/vn-1795/XML-2017-08-21/DE");
            importer.run();
            
            if (LOG.isDebugEnabled()) {
                LOG.debug(allsb.toString());
            }
                
        
        } catch (Exception e) {
            ExceptionUtil.log(e, "test failed");
        }
    }

}
