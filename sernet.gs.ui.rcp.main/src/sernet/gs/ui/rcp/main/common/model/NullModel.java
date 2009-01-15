package sernet.gs.ui.rcp.main.common.model;

import java.util.HashSet;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.hui.common.connect.EntityType;

public class NullModel extends BSIModel {
	
		public NullModel() {
			CnATreeElement placeholder = new CnAPlaceholder(this); 
			
			addChild(placeholder);
		}
	
		@Override
		public String getTitel() {
			return "";
		}
		
		@Override
		public boolean canContain(Object obj) {
			return true;
		}
		
}
