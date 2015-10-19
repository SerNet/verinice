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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;

@SuppressWarnings("serial")
public class UpdateElementsForGstoolImport<T extends ITypedElement> extends GenericCommand  {

	private List<T> elements;
	private transient IBaseDao<T, Serializable> dao;

	private transient Logger log = Logger.getLogger(UpdateElementsForGstoolImport.class);

	private int flushThreshold = 50;

	public UpdateElementsForGstoolImport(List<T> elements) {
		this.elements = (elements != null) ? elements : new ArrayList<T>(0);
	}

    public void execute() {
		if (elements.size()>0) {
		    int i = 0;
		    int elementCount = 0;
			IBaseDao<T, Serializable> dao = getDao();
			for (T element : elements) {
			    if(element != null){
			        dao.merge(element, true);
			    }
				i++;
				if(i>flushThreshold) {
				    elementCount += i;
				    if(getLog().isDebugEnabled()) {
				        getLog().debug("updated " + String.valueOf(elementCount) + " of " + elements.size() + " in total");
                    }
				    i=0;
				    dao.flush();
				}
			}
		}
	}

    public int getFlushThreshold() {
        return flushThreshold;
    }

    public void setFlushThreshold(int flushThreshold) {
        this.flushThreshold = flushThreshold;
    }

    private IBaseDao<T, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAO(elements.get(0).getTypeId());
        }
        return dao;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.GenericCommand#clear()
     */
    @Override
    public void clear() {
        super.clear();
        this.elements = null;
    }

    private Logger getLog() {
        if(log == null) {
            log = Logger.getLogger(UpdateElementsForGstoolImport.class);
        }
        return log;
    }

}
