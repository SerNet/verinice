/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class UnresolvedItem implements Serializable {
		private TodoViewItem item;
		private Integer dbId;
		private MassnahmenUmsetzung massnahmenUmsetzung;
		private PropertyList revisionDurchLinks;
		public PropertyList getRevisionDurchLinks() {
			return revisionDurchLinks;
		}

		public void setRevisionDurchLinks(PropertyList revisionDurchLinks) {
			this.revisionDurchLinks = revisionDurchLinks;
		}

		public PropertyList getUmsetzungDurchLinks() {
			return umsetzungDurchLinks;
		}

		public void setUmsetzungDurchLinks(PropertyList umsetzungDurchLinks) {
			this.umsetzungDurchLinks = umsetzungDurchLinks;
		}

		private PropertyList umsetzungDurchLinks;
		
		public TodoViewItem getItem() {
			return item;
		}

		public Integer getDbId() {
			return dbId;
		}

		public MassnahmenUmsetzung getMassnahmenUmsetzung() {
			return massnahmenUmsetzung;
		}

		UnresolvedItem(TodoViewItem item, Integer dbId) {
			this.item = item;
			this.dbId = dbId;
		}
		
		UnresolvedItem(TodoViewItem item, MassnahmenUmsetzung massnahmenUmsetzung) {
			this.item = item;
			this.massnahmenUmsetzung = massnahmenUmsetzung;
			if(massnahmenUmsetzung!=null) {
				this.dbId = massnahmenUmsetzung.getDbId();
			}
		}

		/**
		 * @param item2
		 * @param dbId2
		 * @param properties
		 * @param properties2
		 */
		public UnresolvedItem(TodoViewItem item2, Integer dbId2,
				PropertyList umsetzungDurchLinks, PropertyList revisionDurchLinks) {
			this(item2, dbId2);
			this.umsetzungDurchLinks = umsetzungDurchLinks;
			this.revisionDurchLinks = revisionDurchLinks;
			
		}
		
		
	}
