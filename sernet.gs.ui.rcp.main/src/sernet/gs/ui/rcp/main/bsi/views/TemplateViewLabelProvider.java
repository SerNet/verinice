/*******************************************************************************  
 * Copyright (c) 2016 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportParent;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.LoadElementTitles;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de> 
 */
public class TemplateViewLabelProvider extends LabelProvider implements ITableLabelProvider {

    private static final Logger LOG = Logger.getLogger(TemplateViewLabelProvider.class);

    private TemplateView templateView;
    private static HashMap<Integer, String> titleMap = new HashMap<>();

    private static final String TEMPLATE_MASTER = "Master";

    public TemplateViewLabelProvider(TemplateView templateView) {
        this.templateView = templateView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.
     * Object, int)
     */
    @Override
    public Image getColumnImage(Object obj, int index) {
        if (obj instanceof PlaceHolder) {
            return null;
        }
        CnATreeElement element = (CnATreeElement) obj;
        switch (index) {
        case 1:
            return getObjTypeImage(element);
        default:
            return null;
        }
    }

    private Image getObjTypeImage(CnATreeElement element) {
        Image image = CnAImageProvider.getCustomImage(element);
        if (image != null) {
            return image;
        }

        String typeId = element.getTypeId();

        if (typeId.equals(Control.TYPE_ID) || typeId.equals(SamtTopic.TYPE_ID)) {
            String impl = Control.getImplementation(element.getEntity());
            return ImageCache.getInstance().getControlImplementationImage(impl);
        }
        if (element instanceof Group && !(element instanceof ImportIsoGroup)) {
            Group<?> group = (Group<?>) element;
            // TODO - getChildTypes()[0] might be a problem for more than one
            // type
            typeId = group.getChildTypes()[0];
        }
        return ImageCache.getInstance().getObjectTypeImage(typeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.
     * Object, int)
     */
    @Override
    public String getColumnText(Object obj, int index) {
        if (obj instanceof PlaceHolder) {
            if (index != 1) {
                return ""; //$NON-NLS-1$
            }
            PlaceHolder pl = (PlaceHolder) obj;
            return pl.getTitle();
        }

        CnATreeElement element = (CnATreeElement) obj;

        switch (index) {
        case 0:
            return loadParentTitle(element);
        case 1:
            return ""; // image only //$NON-NLS-1$
        case 2:
            return element.getTitle();
        case 3:
            String title = "";
            try {
                if (!titleMap.containsKey(element.getScopeId())) {
                    title = loadElementsTitles(element);
                } else {
                    title = titleMap.get(element.getScopeId());
                }
            } catch (CommandException e) {
                LOG.error("Error while getting element properties", e);
            }
            return title; // ScopeTitle from element dependencies
        case 4:
            return templateView.getInputElement().getEntity().getDbId().equals(element.getEntity().getDbId()) ? TEMPLATE_MASTER : "";
        default:
            return ""; //$NON-NLS-1$
        }
    }

    private static String loadElementsTitles(CnATreeElement element) throws CommandException {
        LoadElementTitles scopeCommand;
        scopeCommand = new LoadElementTitles();
        scopeCommand = ServiceFactory.lookupCommandService().executeCommand(scopeCommand);
        titleMap = scopeCommand.getElements();
        return titleMap.get(element.getScopeId());
    }

    private String loadParentTitle(CnATreeElement element) {
        LoadReportParent scopeCommand = new LoadReportParent(element.getDbId());
        try {
            scopeCommand = ServiceFactory.lookupCommandService().executeCommand(scopeCommand);
        } catch (CommandException e) {
            LOG.error("Error while getting element parent title", e);
        }
        return scopeCommand.getElements().get(0).getTitle();
    }
}
