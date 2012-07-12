package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Organization;

@SuppressWarnings("serial")
public class LoadReportAllControls extends GenericCommand {

	private int rootElementId;
	
	private Set<Control> result;
	
	private transient Logger logger;
	
	public LoadReportAllControls(Integer root){
		rootElementId = root;
		logger = Logger.getLogger(LoadReportAllControls.class);
	}
	
	private Set<Control> getControlChildren(CnATreeElement elmt){
		Set<Control> controls = new HashSet<Control>();
		for(CnATreeElement e : elmt.getChildren()){
			if(e instanceof Control){
				controls.add((Control)e);
			}
			controls.addAll(getControlChildren(e));
		}
		return controls;
	}
	
	@Override
	public void execute() {
		LoadCnAElementById command = new LoadCnAElementById(Organization.TYPE_ID, rootElementId);
		try {
			command = ServiceFactory.lookupCommandService().executeCommand(command);
			if(command.getFound() == null){
				command = new LoadCnAElementById(ITVerbund.TYPE_ID, rootElementId);
				command = ServiceFactory.lookupCommandService().executeCommand(command);
			}
			if(command.getFound() != null){
				result = getControlChildren(command.getFound());
			}
		} catch (Exception e) {
			logger.error("Error while executing command", e);
		}
	}
	
	public List<Control> getResult(){
		ArrayList<Control> listResult = new ArrayList<Control>();
		for(Control c : result){
			if(!c.isChildrenLoaded()){
				c = (Control)loadChildren(c);
			}
			listResult.add(c);
		}
	    Collections.sort(listResult, new Comparator<CnATreeElement>() {
            @Override
            public int compare(CnATreeElement o1, CnATreeElement o2) {
                NumericStringComparator comparator = new NumericStringComparator();
                return comparator.compare(o1.getTitle(), o2.getTitle());
            }
        });
		return listResult;
	}
	
    private CnATreeElement loadChildren(CnATreeElement el) {
        if (el.isChildrenLoaded()) {
            return el;
        } 

        LoadChildrenForExpansion command;
        command = new LoadChildrenForExpansion(el);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            CnATreeElement newElement = command.getElementWithChildren();
            newElement.setChildrenLoaded(true);
            return newElement;
        } catch (CommandException e) {
            logger.error("error while loading children of CnaTreeElment", e);
        }
        return null;
    }

}
