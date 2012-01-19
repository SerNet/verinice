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
package sernet.verinice.service.iso27k;

import java.util.Arrays;
import java.util.List;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;

@SuppressWarnings("serial")
public class LoadImportObjectsHolder extends GenericCommand implements INoAccessControl {

	private CnATreeElement holder;
	
	private Class clazz;

	public LoadImportObjectsHolder(Class clazz) {
	    this.clazz = clazz;
	}
	
	public void execute() {
	    String typeId = getTypeId(clazz);
		RetrieveInfo ri = new RetrieveInfo();
		List<CnATreeElement> resultList = getDaoFactory().getDAO(typeId).findAll(ri);
		if(resultList != null) {
			if(resultList.size()>1) {
				throw new RuntimeException("More than one ImportIsoGroup found.");
			} else if(resultList.size()==1) {			
			    holder = resultList.get(0);
			}
		}
	}


	/**
     * @param clazz2
     * @return
     */
    private String getTypeId(Class clazz) {
        String typeId = ImportIsoGroup.TYPE_ID;
        if(isImplementation(clazz,IBSIStrukturElement.class)) {
            typeId = ImportBsiGroup.TYPE_ID;
        }
        return typeId;
    }

    public static boolean isImplementation(Class clazz,Class interfaze) {
        boolean implementz = false;
        Class[] interfaceArray = clazz.getInterfaces();
        for (int i = 0; i < interfaceArray.length; i++) {
            if(interfaceArray[i].equals(interfaze)) {
                implementz=true;
                break;
            }
        }
        return implementz;
    }

    public CnATreeElement getHolder() {
		return holder;
	}
	
	

}
