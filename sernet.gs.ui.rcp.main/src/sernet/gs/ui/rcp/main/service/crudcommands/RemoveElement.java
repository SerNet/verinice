package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class RemoveElement<T extends CnATreeElement> extends GenericCommand {

	private T element;

	public RemoveElement(T element) {
		this.element = element;
	}
	
	public void execute() {
		
		
			try {
				
				if (element instanceof Person)
					removeConfiguration((Person) element);
				
				IBaseDao dao =  getDaoFactory().getDAOForObject(element);
				element = (T) dao.findById(element.getDbId());

				if (element instanceof ITVerbund) {
					Set<CnATreeElement> personen = ((ITVerbund) element).getCategory(PersonenKategorie.TYPE_ID).getChildren();
					for (CnATreeElement elmt : personen) {
						removeConfiguration((Person)elmt);
					}
				}

				element.remove();
				dao.delete(element);
				element = null;
			} catch (CommandException e) {
				throw new RuntimeCommandException(e);
			}
			
		
// FIXME server: create bulk delete to speed up deletion of objects
//		String query = "delete from CnATreeElement as elmt where elmt.dbId = ?";
//		Integer dbId = element.getDbId();
//		int rows = dao.updateByQuery(
//				query, 
//				new Object[] { dbId } );
//		Logger.getLogger(this.getClass()).debug("Deleted rows: " + rows);
	}

	private void removeConfiguration(Person person) throws CommandException {
		LoadConfiguration command = new LoadConfiguration(person);
		command = getCommandService().executeCommand(command);
		Configuration conf = command.getConfiguration();
		if (conf != null) {
			IBaseDao<Configuration, Serializable> confDAO = getDaoFactory().getDAO(Configuration.class);
			confDAO.delete(conf);
		}
	}

}
