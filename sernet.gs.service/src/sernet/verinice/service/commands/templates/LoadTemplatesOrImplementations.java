/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
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
package sernet.verinice.service.commands.templates;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnATreeElement.TemplateType;
import sernet.verinice.service.commands.LoadElementsByUuid;

/**
 * This command loads for given ({@link CnATreeElement}) all modeling templates
 * ({@link TemplateType#TEMPLATE}) applied (implemented) in this element, if the
 * element is an implementation or all implementations
 * ({@link TemplateType#IMPLEMENTATION}) that belong to this modeling template,
 * if the element is a modeling template.
 * 
 * @see CnATreeElement#implementedTemplateUuids
 * @see TemplateType
 * @see sernet.gs.server.DeleteOrphanTemplateRelationsJob
 * 
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class LoadTemplatesOrImplementations extends GenericCommand {

    private static final long serialVersionUID = -8755177313752809710L;

    private transient Logger log = Logger.getLogger(LoadTemplatesOrImplementations.class);

    private final CnATreeElement inputElement;
    private Set<CnATreeElement> elements = new HashSet<CnATreeElement>();

    /**
     * @param implElement
     */
    public LoadTemplatesOrImplementations(CnATreeElement implElement) {
        super();
        this.inputElement = implElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if (inputElement.isImplementation()) {
            loadTemplates();
        } else if (inputElement.isTemplate()) {
            loadImplementations();
        } else {
            elements = Collections.emptySet();
        }
    }

    private void loadTemplates() {
            if (!inputElement.getImplementedTemplateUuids().isEmpty()) {
            Set<CnATreeElement> templates = getElementsWithUuids(inputElement.getImplementedTemplateUuids());

                if (templates == null || templates.isEmpty()) {
                    if (getLog().isInfoEnabled()) {
                        getLog().error("No templates for element id: " + inputElement.getDbId() + " found.");
                    }
                    return;
                }
                elements.addAll(templates);
            }
    }

    @SuppressWarnings("unchecked")
    private void loadImplementations() {
        IBaseDao<? extends CnATreeElement, Serializable> dao = getCnATreeElementDao();

        String hgl = "select distinct ce from CnATreeElement as ce " + //$NON-NLS-1$
                "join ce.implementedTemplateUuids " + //$NON-NLS-1$
                "where ? in elements(ce.implementedTemplateUuids) "; //$NON-NLS-1$

        Object[] params = new Object[] { inputElement.getUuid() };
        List<CnATreeElement> list = dao.findByQuery(hgl, params);

        if (list == null || list.isEmpty()) {
            if (getLog().isInfoEnabled()) {
                getLog().info("No implementation of template with uuid: " + inputElement.getUuid() + " found.");
            }
            return;
        }

        Set<String> implementedElementUuids = new HashSet<String>();
        for (CnATreeElement element : list) {
            implementedElementUuids.add(element.getUuid());
        }

        Set<CnATreeElement> implementations = new HashSet<CnATreeElement>();
        implementations = getElementsWithUuids(implementedElementUuids);
        elements.addAll(implementations);
    }

    /**
     * @param implementedElementUuids
     * @param implementations
     * @return
     */
    private Set<CnATreeElement> getElementsWithUuids(Set<String> implementedElementUuids) {
        Set<CnATreeElement> elements = new HashSet<CnATreeElement>();
        try {
            RetrieveInfo retrieveInfo = RetrieveInfo.getPropertyInstance();
            retrieveInfo.setParent(true);
            retrieveInfo.setParentProperties(true);
            LoadElementsByUuid<CnATreeElement> command = new LoadElementsByUuid<CnATreeElement>(implementedElementUuids, retrieveInfo);
            command = getCommandService().executeCommand(command);
            elements = command.getElements();
        } catch (CommandException e) {
            getLog().error("Error while loading elements.", e);
        }
        return elements;
    }

    private IBaseDao<CnATreeElement, Serializable> getCnATreeElementDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadTemplatesOrImplementations.class);
        }
        return log;
    }

    public Set<CnATreeElement> getElements() {
        return elements;
    }
}
