/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class LoadTemplateCandidates extends GenericCommand {

    private static final long serialVersionUID = 1847024646098773061L;

    private String uuid;
    private String typeId;
    private Integer scopeId;
    private Integer groupId;

    private Set<CnATreeElement> templateCandidates = new HashSet<CnATreeElement>();

    /**
     * @param implElement
     */
    public LoadTemplateCandidates(String uuid, String typeId, Integer scopeId, Integer groupId) {
        super();
        this.uuid = uuid;

        if (MassnahmenUmsetzung.TYPE_ID.equals(typeId)) {
            typeId = MassnahmenUmsetzung.HIBERNATE_TYPE_ID;
        }
        if (BausteinUmsetzung.TYPE_ID.equals(typeId)) {
            typeId = BausteinUmsetzung.HIBERNATE_TYPE_ID;
        }

        this.typeId = typeId;
        this.scopeId = scopeId;
        this.groupId = groupId;
    }

    public LoadTemplateCandidates(String uuid, String typeId) {
        this(uuid, typeId, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        CnATreeElement element = dao.findByUuid(this.uuid, ri);

        DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);
        crit.setFetchMode("entity", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        crit.add(Restrictions.eq("objectType", typeId));
        crit.add(Restrictions.eq("templateTypeValue", CnATreeElement.TemplateType.TEMPLATE.name()));
        crit.add(Restrictions.ne("uuid", uuid));
        if (!element.getImplementedTemplateUuids().isEmpty()) {
            crit.add(Restrictions.not(Restrictions.in("uuid", element.getImplementedTemplateUuids())));
        }
        if (scopeId != null) {
            crit.add(Restrictions.eq("scopeId", scopeId));
        }
        if (groupId != null) {
            crit.add(Restrictions.eq("parentId", groupId));
        }
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

        List<CnATreeElement> candidates = dao.findByCriteria(crit);
        templateCandidates.addAll(candidates);
    }

    public Set<CnATreeElement> getTemplateCandidates() {
        return templateCandidates;
    }
}
