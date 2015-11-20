package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * this command only exists to create a corrupted database for testing {@link MigrateDbTo1_03D}
 * DO NOT EVER USE THIS CODE IN PRODUCTION, it will
 * @author shagedorn
 *
 */

public class SwapCnaLinkReferences extends GenericCommand {
	
	private transient Logger log = null;
	
	public SwapCnaLinkReferences() {}
	
	@Override
	public void execute() {
		IBaseDao<CnALink, Serializable> linkDao = getDaoFactory().getDAO(CnALink.class);
		
		List<CnALink> linkList = linkDao.findAll();
		StringBuilder sb = new StringBuilder();
		
		for(int index = 0; index < linkList.size(); index++){
			CnALink link = linkList.get(index);
		
			
			if(index % 50 == 0){
				sb.append("Swapping Link #").append(index).append("/").append(linkList.size());
				getLog().error(sb.toString());
				sb.setLength(0);
			}
			
			CnATreeElement source = link.getDependant();
			CnATreeElement target = link.getDependency();

			CreateLink command = new CreateLink(target, source, link.getRelationId());
			try {
				getCommandService().executeCommand(command);
			} catch (CommandException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private Logger getLog(){
		if(log == null){
			log = Logger.getLogger(SwapCnaLinkReferences.class);
		}
		return log;
	}

}
