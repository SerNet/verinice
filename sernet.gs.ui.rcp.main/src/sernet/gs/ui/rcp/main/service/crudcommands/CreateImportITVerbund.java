/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class CreateImportITVerbund extends CreateElement
{
	private String sourceId;
	
	public CreateImportITVerbund(CnATreeElement container, Class type, String sourceId )
	{
		super( container, type );
		this.sourceId = sourceId;
	}

	@Override
	public void execute()
	{
		super.execute();
		if ( super.child instanceof ITVerbund )
		{
			ITVerbund verbund = (ITVerbund) super.child;
			verbund.createNewCategories();
			verbund.setSourceId( this.sourceId );
			verbund.setSimpleProperty( ITVerbund.PROP_NAME, this.sourceId + "-Verbund" );
		}
	}

	@Override
	public ITVerbund getNewElement()
	{
		return (ITVerbund) super.getNewElement();
	}

}
