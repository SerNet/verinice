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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.faces.bean.ManagedBean;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.web.Messages;

/**
 * Provides several methods which provide data for the charts.
 *
 * The user must have at least read access to the verinice object, otherwise it
 * is not used for the data aggregation.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "controlService")
public class ControlService extends GenericChartService {

    private static final String IMPLEMENTATION_STATUS_UNEDITED = "SingleSelectDummyValue";

    /**
     * Returns aggregate status of all {@link MassnahmenUmsetzung} in verinice.
     *
     * @return The keys of the map is {@link MassnahmenUmsetzung#getUmsetzung()}
     *
     */
    public SortedMap<String, Number> aggregateMassnahmenUmsetzungStatus() {

        IDAOFactory iDaoFactory = getDaoFactory();

        IBaseDao<MassnahmenUmsetzung, Serializable> massnahmenDao = getMassnahmenDao(iDaoFactory);
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();

        @SuppressWarnings("unchecked")
        List<MassnahmenUmsetzung> massnahmen = massnahmenDao.findAll(ri);
        return aggregateMassnahmenUmsetzung(massnahmen);
    }


    /**
     * Returns aggregated status of all {@link MassnahmenUmsetzung} of a it
     * network.
     *
     * @param itNetwork
     *            The IT-Network for which the {@link MassnahmenUmsetzung} are
     *            aggregated.
     * @return If no {@link MassnahmenUmsetzung} is defined for the IT network.
     *         it could be empty.
     * @exception IllegalArgumentException
     *                If no it network is given.
     */
    public SortedMap<String, Number> aggregateMassnahmenUmsetzung(ITVerbund itNetwork) {

        if (itNetwork == null) {
            throw new IllegalArgumentException("param itNetwork may not be null");
        }

        return aggregateMassnahmenUmsetzung(itNetwork.getScopeId());
    }

    /**
     * Returns aggregated status of all {@link MassnahmenUmsetzung} of a it
     * network.
     *
     * @param scopeId
     *            The IT-Network for which the {@link MassnahmenUmsetzung} are
     *            aggregated.
     * @return If no {@link MassnahmenUmsetzung} is defined for the IT network.
     *         it could be empty.
     * @exception IllegalArgumentException
     *                If no it network is given.
     */
    public SortedMap<String, Number> aggregateMassnahmenUmsetzung(Integer scopeId) {

        String hqlQuery = new StringBuilder()
                .append("from CnATreeElement element")
                .append(" left join fetch element.entity entity")
                .append(" left join fetch entity.typedPropertyLists propertyLists")
                .append(" left join fetch propertyLists.properties props")
                .append(" where element.scopeId = ?")
                .append(" and element.objectType = ?").toString();
        Object[] params = new Object[] { scopeId, MassnahmenUmsetzung.HIBERNATE_TYPE_ID };
        IBaseDao<MassnahmenUmsetzung, Serializable> massnahmenDao = getMassnahmenDao(getDaoFactory());
        @SuppressWarnings("unchecked")
        List<MassnahmenUmsetzung> massnahmenUmsetzungen = massnahmenDao.findByQuery(hqlQuery, params);
        return aggregateMassnahmenUmsetzung(massnahmenUmsetzungen);
    }

    /**
     * Aggregate over all {@link BausteinUmsetzung} objects and aggregate the
     * {@link MassnahmenUmsetzung} states.
     *
     * This method normalizes the data in a way, that the number of a specific
     * state is divided through the number of instances of a specific
     * {@link BausteinUmsetzung}. With instances is meant several
     * {@link BausteinUmsetzung} objects with the same chapter value.
     *
     * @return Key is the chapter of a {@link BausteinUmsetzung}. The value is a
     *         map with the key {@link MassnahmenUmsetzung#getUmsetzung()}. This
     *         allows the result to be displayed as a stacked chart.
     */
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUmsNormalized() {

        List<BausteinUmsetzung> baUs = getAllBausteinUmsetzungen();

        Map<String, Map<String, Number>> chapter2MaUs = new HashMap<>();
        Map<String, Integer> chapter2Count = new HashMap<>();

        aggregateMassnahmen(baUs, chapter2MaUs, chapter2Count);

        normalize(chapter2MaUs, chapter2Count);
        return chapter2MaUs;
    }

    private SortedMap<String, Number> aggregateMassnahmenUmsetzung(List<MassnahmenUmsetzung> massnahmen) {

        SortedMap<String, Number> result = new TreeMap<>(new Comparator<String>() {

            @Override
            public int compare(String o1, String o2) {
                return new NumericStringComparator().compare(getLabel(o1), getLabel(o2));
            }

            private String getLabel(String value) {

                if (MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET.equals(value)) {
                    return Messages.getString(IMPLEMENTATION_STATUS_UNEDITED);
                }

                return getObjectService().getLabel(value);
            }
        });

        for (MassnahmenUmsetzung m : massnahmen) {
            Number number = result.get(m.getUmsetzung());
            number = number == null ? 1 : number.intValue() + 1;
            result.put(m.getUmsetzung(), number);
        }

        return result;
    }

    /**
     * Aggregate over all {@link BausteinUmsetzung} objects and aggregate the
     * {@link MassnahmenUmsetzung} states.
     *
     * This method normalizes the data in a way, that the number of a specific
     * state is divided through the number of instances of a specific
     * {@link BausteinUmsetzung}. With instances is meant several
     * {@link BausteinUmsetzung} object with the same chapter value.
     *
     * @return Key is the chapter of a {@link BausteinUmsetzung}. The value is a
     *         map with the key {@link MassnahmenUmsetzung#getUmsetzung()}. This
     *         allows the result to be displayed as a stacked chart.
     */
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUms() {

        List<BausteinUmsetzung> baUs = getAllBausteinUmsetzungen();

        Map<String, Map<String, Number>> chapter2MaUs = new HashMap<>();
        Map<String, Integer> chapter2Count = new HashMap<>();

        aggregateMassnahmen(baUs, chapter2MaUs, chapter2Count);
        return chapter2MaUs;
    }

    /**
     * Aggregate over all {@link BausteinUmsetzung} objects and aggregate the
     * {@link MassnahmenUmsetzung} states.
     *
     * @param itNetwork
     *            Only {@link BausteinUmsetzung} under this it network are taken
     *            into account.
     *
     * @return Key is the chapter of a {@link BausteinUmsetzung}. The value is a
     *         map with the key {@link MassnahmenUmsetzung#getUmsetzung()}. This
     *         allows the result to be displayed as a stacked chart.
     */
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUms(ITVerbund itNetwork) {

        List<BausteinUmsetzung> baUs = filterBausteinUmsetzung(itNetwork, getAllBausteinUmsetzungen());

        Map<String, Map<String, Number>> chapter2MaUs = new HashMap<>();
        Map<String, Integer> chapter2Count = new HashMap<>();

        aggregateMassnahmen(baUs, chapter2MaUs, chapter2Count);
        return chapter2MaUs;
    }

    /**
     * Aggregate over all {@link BausteinUmsetzung} objects and aggregate the
     * {@link MassnahmenUmsetzung} states.
     *
     * This method normalizes the data in a way, that the number of a specific
     * state is divided through the number of instances of a specific
     * {@link BausteinUmsetzung}. With instances is meant several
     * {@link BausteinUmsetzung} object with the same chapter value.
     *
     * @param itNetwork
     *            Only {@link BausteinUmsetzung} under this it network are taken
     *            into account.
     *
     * @return Key is the chapter of a {@link BausteinUmsetzung}. The value is a
     *         map with the key {@link MassnahmenUmsetzung#getUmsetzung()}. This
     *         allows the result to be displayed as a stacked chart.
     */
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUmsNormalized(ITVerbund itNetwork) {

        List<BausteinUmsetzung> baUs = filterBausteinUmsetzung(itNetwork, getAllBausteinUmsetzungen());

        Map<String, Map<String, Number>> chapter2MaUs = new HashMap<>();
        Map<String, Integer> chapter2Count = new HashMap<>();

        aggregateMassnahmen(baUs, chapter2MaUs, chapter2Count);
        normalize(chapter2MaUs, chapter2Count);

        return chapter2MaUs;
    }




    private List<BausteinUmsetzung> filterBausteinUmsetzung(ITVerbund itNetwork, List<BausteinUmsetzung> allBausteinUmsetzungen) {
        List<BausteinUmsetzung> filteredBausteinUmsetzungen = new ArrayList<>();
        for (BausteinUmsetzung b : allBausteinUmsetzungen) {
            if (b.getScopeId() == itNetwork.getScopeId()) {
                filteredBausteinUmsetzungen.add(b);
            }
        }

        return filteredBausteinUmsetzungen;
    }

    @SuppressWarnings("unchecked")
    private List<BausteinUmsetzung> getAllBausteinUmsetzungen() {
        IBaseDao<BausteinUmsetzung, Serializable> dao = getDaoFactory().getDAO(BausteinUmsetzung.class);
        RetrieveInfo retrieveInfo = new RetrieveInfo();
        retrieveInfo.setParent(true);
        retrieveInfo.setProperties(true);
        retrieveInfo.setChildren(true);
        return dao.findAll(retrieveInfo);
    }

    private void aggregateMassnahmen(List<BausteinUmsetzung> baUs, Map<String, Map<String, Number>> chapter2MaUs, Map<String, Integer> chapter2Count) {
        for (BausteinUmsetzung baU : baUs) {

            String chapter = baU.getKapitel();
            List<MassnahmenUmsetzung> maUs = baU.getMassnahmenUmsetzungen();
            Map<String, Number> state2Count = aggregateMassnahmenUmsetzung(maUs);

            if (chapter2Count.containsKey(chapter)) {
                chapter2Count.put(chapter, chapter2Count.get(chapter) + 1);
            } else {
                chapter2Count.put(chapter, 0);
            }

            if (chapter2MaUs.containsKey(chapter)) {
                Map<String, Number> maU2Count = chapter2MaUs.get(chapter);
                for (Entry<String, Number> e : state2Count.entrySet()) {
                    if (maU2Count.containsKey(e.getValue())) {
                        Number oldStateCount = maU2Count.get(e.getKey());
                        Number currentStateCount = e.getValue();
                        Number newStateCount = oldStateCount.intValue() + currentStateCount.intValue();
                        maU2Count.put(e.getKey(), newStateCount);
                    } else {
                        maU2Count.put(e.getKey(), e.getValue());
                    }
                }
            } else {
                chapter2MaUs.put(chapter, state2Count);
            }
        }
    }

    private void normalize(Map<String, Map<String, Number>> chapter2MassnahmenUmsetzungen, Map<String, Integer> chapter2Count) {
        for (Entry<String, Map<String, Number>> e : chapter2MassnahmenUmsetzungen.entrySet()) {
            for (Entry<String, Number> e2 : e.getValue().entrySet()) {
                double newValue = Math.ceil(e2.getValue().doubleValue() / chapter2Count.get(e.getKey()));
                e.getValue().put(e2.getKey(), newValue);
            }
        }
    }

    private IBaseDao<MassnahmenUmsetzung, Serializable> getMassnahmenDao(IDAOFactory iDaoFactory) {
        return (IBaseDao<MassnahmenUmsetzung, Serializable>) iDaoFactory.getDAO(MassnahmenUmsetzung.class);
    }
}
