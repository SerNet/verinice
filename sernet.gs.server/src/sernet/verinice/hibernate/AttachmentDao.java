/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.IAttachmentDao;
import sernet.verinice.model.bsi.Attachment;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class AttachmentDao extends TreeElementDao<Attachment,Integer> implements IAttachmentDao {

    public AttachmentDao() {
        super(Attachment.class);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAttachmentDao#loadAttachmentList(java.lang.Integer)
     */
    @Override
    public List<Attachment> loadAttachmentList(Integer cnAElementId) {
        DetachedCriteria crit = DetachedCriteria.forClass(Attachment.class);
        if(cnAElementId!=null) {
            crit.add(Restrictions.eq("cnATreeElementId", cnAElementId));
        }
        crit.setFetchMode("entity", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return findByCriteria(crit);
    }
}
