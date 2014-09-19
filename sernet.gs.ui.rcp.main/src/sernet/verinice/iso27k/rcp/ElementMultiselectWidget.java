package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.MultiselectWidget;

public class ElementMultiselectWidget extends MultiselectWidget<CnATreeElement> {

    public ElementMultiselectWidget(Composite composite, ITreeSelection selection, CnATreeElement selectedElement) throws CommandException {
        super(composite, selection, selectedElement);
        setTitle(Messages.SamtExportDialog_2);
        setShowOnlySelected(false);
        setShowOnlySelectedCheckbox(false);
    }

    public ElementMultiselectWidget(Composite composite) throws CommandException {
       super(composite);
       setTitle(Messages.SamtExportDialog_2);
       setShowOnlySelected(false);
       setShowOnlySelectedCheckbox(false);
    }

    protected void initData() throws CommandException {
        /*
         * Widgets for selection of an IT network or organization:
         */

        LoadCnAElementByType<Organization> cmdLoadOrganization = new LoadCnAElementByType<Organization>(Organization.class);
        cmdLoadOrganization = ServiceFactory.lookupCommandService().executeCommand(cmdLoadOrganization);
        
        
        LoadCnAElementByType<ITVerbund> cmdItVerbund = new LoadCnAElementByType<ITVerbund>(ITVerbund.class);
        cmdItVerbund = ServiceFactory.lookupCommandService().executeCommand(cmdItVerbund);
            
        itemList = new ArrayList<CnATreeElement>();
        itemList.addAll(cmdLoadOrganization.getElements());
        itemList.addAll(cmdItVerbund.getElements());
        itemList = sortItems(itemList);
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
    
    public boolean isShowOnlySelectedCheckbox() {
        return false;
    }
    
    public boolean isShowOnlySelected() {
        return false;
    }

}
