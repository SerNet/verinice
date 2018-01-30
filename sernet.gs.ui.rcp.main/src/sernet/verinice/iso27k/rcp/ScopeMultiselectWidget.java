package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.MultiselectWidget;
import sernet.verinice.service.commands.crud.LoadCnAElementByType;

/**
 * A widget which provides scopes for multi selection. A scope is either a Organization or an
 * IT network (German: IT-Verbund).
 * 
 * Override this class and method getElementClasses() to change the set of CnATreeElement
 * classes which are provided for selection.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ScopeMultiselectWidget extends MultiselectWidget<CnATreeElement> {
            
    public ScopeMultiselectWidget(Composite composite, ITreeSelection selection, CnATreeElement selectedElement) throws CommandException {     
        super(composite, selection, selectedElement);
        setTitle(Messages.SamtExportDialog_2);
    }

    public ScopeMultiselectWidget(Composite composite) throws CommandException {
       super(composite);
       setTitle(Messages.SamtExportDialog_2);
       
    }
    
    /**
     * Returns a set of CnATreeElement classes which are loaded
     * and provided for selection.
     * 
     * Override this class and this method to load different classes.
     * 
     * @return A set of classes
     */
    protected Set<Class<?>> getElementClasses() {
        Set<Class<?>> elementClasses = new HashSet<>();
        elementClasses.add(Organization.class);
        elementClasses.add(ITVerbund.class);
        elementClasses.add(ItNetwork.class);
        return elementClasses;
    }

    protected void initData() throws CommandException {
        itemList = new ArrayList<CnATreeElement>();
        
        for (Class<?> clazz : getElementClasses()) {
            loadElements((Class<CnATreeElement>) clazz);
        }
         
        itemList = sortItems(itemList);
    }

    
    private void loadElements(Class<CnATreeElement> clazz) throws CommandException {
        LoadCnAElementByType<CnATreeElement> cmdLoadOrganization = new LoadCnAElementByType<>(clazz);
        cmdLoadOrganization = ServiceFactory.lookupCommandService().executeCommand(cmdLoadOrganization);
        itemList.addAll(cmdLoadOrganization.getElements());
    }
    
    protected String getLabel(CnATreeElement elmt) {
        return elmt.getTitle();
    }
    
    protected List<CnATreeElement> sortItems(List<CnATreeElement> list){
        Collections.sort(list, new Comparator<CnATreeElement>() {
            @Override
            public int compare(CnATreeElement o1, CnATreeElement o2) {
                NumericStringComparator comparator = new NumericStringComparator();
                return comparator.compare(o1.getTitle(), o2.getTitle());
            }
        });
        return list;
    }
    
    public boolean isShowOnlySelected() {
        return false;
    }

    public boolean isShowOnlySelectedCheckbox() {
        return false;
    }
    

}
