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

import java.util.HashMap;
import java.util.Map;

import javax.faces.bean.ManagedBean;

import sernet.verinice.model.bsi.ITVerbund;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@ManagedBean(name = "controlServiceDummy")
public class ControlServiceDummy extends ControlService {

    @Override
    public Map<String, Number> aggregateMassnahmenUmsetzungStatus() {
        Map<String, Number> states = new HashMap<>();

        states.put("Unbearbeitet", 3);
        states.put("Teilweise", 31);
        states.put("Nein", 55);
        states.put("Ja", 83);
        states.put("Entbehrlich", 12);

        return states;
    }

    @Override
    public Map<String, Number> aggregateMassnahmenUmsetzung(ITVerbund itNetwork) {
        // TODO Auto-generated method stub
        return super.aggregateMassnahmenUmsetzung(itNetwork);
    }

    @Override
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUmsNormalized() {
        // TODO Auto-generated method stub
        return super.groupMassnahmenUmsByBausteinUmsNormalized();
    }

    @Override
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUms() {
        // TODO Auto-generated method stub
        return super.groupMassnahmenUmsByBausteinUms();
    }

    @Override
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUms(ITVerbund itNetwork) {
        // TODO Auto-generated method stub
        return super.groupMassnahmenUmsByBausteinUms(itNetwork);
    }

    @Override
    public Map<String, Map<String, Number>> groupMassnahmenUmsByBausteinUmsNormalized(ITVerbund itNetwork) {
        // TODO Auto-generated method stub
        return super.groupMassnahmenUmsByBausteinUmsNormalized(itNetwork);
    }

    public Map<String, Integer> aggregateMassnahmenUmsetzungStatus(String scope) {

        Map<String, Integer> states = new HashMap<>();

        states.put("Unbearbeitet", 3);
        states.put("Teilweise", 31);
        states.put("Nein", 55);
        states.put("Ja", 83);
        states.put("Entbehrlich", 12);

        return states;
    }
}
