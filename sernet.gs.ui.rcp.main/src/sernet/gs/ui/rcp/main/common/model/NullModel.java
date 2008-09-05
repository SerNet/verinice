package sernet.gs.ui.rcp.main.common.model;

import java.util.HashSet;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;

public class NullModel extends BSIModel {
	
		private HashSet<CnATreeElement> children;

		public NullModel() {
			ITVerbund nullVerbund = new ITVerbund(this) {
				@Override
				public String getTitel() {
					return "DB-Verbindung: geschlossen";
				}
			};
			addChild(nullVerbund);
		}
	
		@Override
		public String getTitel() {
			return "";
		}
		
}
