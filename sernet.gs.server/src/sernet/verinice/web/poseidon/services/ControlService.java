/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web.poseidon.services;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.model.IObjectModelService;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "controlService")
public class ControlService extends GenericChartService {

    public Map<String, Integer> aggregateAllISAControlStatusForScope() {

        IDAOFactory iDaoFactory = getDaoFactory();

        IBaseDao<Audit, Serializable> auditDaoFactory = (IBaseDao<Audit, Serializable>) iDaoFactory.getDAO(Audit.class);
        RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance();

        @SuppressWarnings("unchecked")
        List<Audit> audits = auditDaoFactory.findAll(ri);

        Map<String, Integer> result = new HashMap<>();
        for (Audit audit : audits) {
            return getIsaStatusByAudit(audit);

        }

        return result;
    }

    public Map<String, Integer> getIsaStatusByAudit(Audit audit) {

        Map<String, Integer> result = new HashMap<>();
        IObjectModelService objectService = getObjectService();

        String label1 = objectService.getLabel("samt_topic_maturity_null");
        String label2 = objectService.getLabel("samt_topic_maturity_na");
        String label3 = objectService.getLabel("samt_topic_maturity_0");
        String label4 = objectService.getLabel("samt_topic_maturity_1");
        String label5 = objectService.getLabel("samt_topic_maturity_2");
        String label6 = objectService.getLabel("samt_topic_maturity_3");
        String label7 = objectService.getLabel("samt_topic_maturity_4");
        String label8 = objectService.getLabel("samt_topic_maturity_5");

        result.put(label1, 0);
        result.put(label2, 0);
        result.put(label3, 0);
        result.put(label4, 0);
        result.put(label5, 0);
        result.put(label6, 0);
        result.put(label7, 0);
        result.put(label8, 0);

        Set<SamtTopic> samtTopics = getSamtTopics(audit);
        for (SamtTopic samtTopic : samtTopics) {
            switch (samtTopic.getMaturity()) {
            case -2:
                result.put(label1, result.get(label1) + 1);
            case -1:
                result.put(label2, result.get(label2) + 1);
            case 0:
                result.put(label3, result.get(label3) + 1);
            case 1:
                result.put(label4, result.get(label4) + 1);
            case 2:
                result.put(label5, result.get(label5) + 1);
            case 3:
                result.put(label6, result.get(label6) + 1);
            case 4:
                result.put(label7, result.get(label7) + 1);
            case 5:
                result.put(label8, result.get(label8) + 1);
            }
        }

        return result;

    }

    private Set<SamtTopic> getSamtTopics(CnATreeElement cnaTreeElement) {

        Set<SamtTopic> samtTopics = new HashSet<>();

        for (CnATreeElement e : cnaTreeElement.getChildren()) {
            if (e instanceof SamtTopic)
                samtTopics.add((SamtTopic) e);
            samtTopics.addAll(getSamtTopics(e));
        }

        return samtTopics;
    }


    public Map<String, Integer> getAccumulatedControlStatesForScope(String string) {
        return null;
    }
}
