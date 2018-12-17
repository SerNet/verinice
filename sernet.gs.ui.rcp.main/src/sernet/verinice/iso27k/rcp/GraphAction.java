/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.iso27k.rcp;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.GraphCommand;
import sernet.verinice.interfaces.IGraphCommand;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;

/**
 * Class to test client access of GraphService by using {@link GraphCommand}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GraphAction extends Action {
    
    private static final Logger LOG = Logger.getLogger(GraphAction.class);
    
    private static String[] typeIds =  {Organization.TYPE_ID, AssetGroup.TYPE_ID, Asset.TYPE_ID,IncidentScenario.TYPE_ID};
    
    private static String ID = "sernet.verinice.iso27k.rcp.GraphAction";
    
    
    public GraphAction(IWorkbenchWindow window) {
        setText("Graph action");
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ARROW_OUT));
        setEnabled(true);
    }
    
    @Override
    public void run() {
        try {
            IGraphCommand command = new GraphCommand();
            IGraphElementLoader loader =new GraphElementLoader();
            loader.setScopeId(96826);
            loader.setTypeIds(typeIds);
            command.addLoader(loader);
            command.addRelationId(IncidentScenario.REL_INCSCEN_ASSET);
            
            command = ServiceFactory.lookupCommandService().executeCommand(command);          
        
            VeriniceGraph graph = command.getGraph();
            Set<CnATreeElement> elementSet = graph.getElements();
            for (CnATreeElement element : elementSet) {
                LOG.debug("Element: " + element.getTitle());  
                CnATreeElement parent = graph.getParent(element);
                if(parent!=null) {
                    LOG.debug("    P: " + parent.getTitle());
                }
                LOG.debug("  Children:");
                Set<CnATreeElement> children = graph.getChildren(element); 
                for (CnATreeElement child : children) {
                    LOG.debug("   " + child.getTitle());
                }
                LOG.debug("  Links:");
                Set<CnATreeElement> links = graph.getLinkTargets(element);                
                for (CnATreeElement target : links) {
                    LOG.debug("   " + target.getTitle());
                }
            }
            
            
        } catch (Exception e) {
            LOG.error("Error while retrieving configuration", e);
        }
    }
}
