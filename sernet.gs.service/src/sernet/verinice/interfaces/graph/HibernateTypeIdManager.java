/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.graph;

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.iso27k.IncidentGroup;

/**
 * This class returns a Hibernate type id for a type id.
 * For almost all objects the Hibernate type id is equal to the type id
 * but for some it differs.
 * 
 * See SNCA.xml for the type ids of objects.
 * See Hibernate mapping configuration in *.hbm.xml files
 * for the Hibernate type id of objects.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class HibernateTypeIdManager {

    
    private static final Map<String, String> TYPE_HIBERNATE_TYPE_MAP;
    static{
        TYPE_HIBERNATE_TYPE_MAP = new HashMap<>();
        TYPE_HIBERNATE_TYPE_MAP.put(ITVerbund.TYPE_ID, ITVerbund.TYPE_ID_HIBERNATE);
        
        TYPE_HIBERNATE_TYPE_MAP.put(ServerKategorie.TYPE_ID, ServerKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(GebaeudeKategorie.TYPE_ID, GebaeudeKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(SonstigeITKategorie.TYPE_ID, SonstigeITKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(AnwendungenKategorie.TYPE_ID, AnwendungenKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(ClientsKategorie.TYPE_ID, ClientsKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(NKKategorie.TYPE_ID, NKKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(PersonenKategorie.TYPE_ID, PersonenKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(RaeumeKategorie.TYPE_ID, RaeumeKategorie.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(TKKategorie.TYPE_ID, TKKategorie.TYPE_ID_HIBERNATE);
        
        TYPE_HIBERNATE_TYPE_MAP.put(SonstIT.TYPE_ID, SonstIT.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(TelefonKomponente.TYPE_ID, TelefonKomponente.TYPE_ID_HIBERNATE);
        TYPE_HIBERNATE_TYPE_MAP.put(NetzKomponente.TYPE_ID, NetzKomponente.TYPE_ID_HIBERNATE);
        
        TYPE_HIBERNATE_TYPE_MAP.put(GefaehrdungsUmsetzung.TYPE_ID, GefaehrdungsUmsetzung.HIBERNATE_TYPE_ID);  
        
        // There are 2 different Hibernate ids for MassnahmenUmsetzung.TYPE_ID: 
        // MassnahmenUmsetzung.HIBERNATE_TYPE_ID and
        // RisikoMassnahmenUmsetzung.HIBERNATE_TYPE_ID
        // This ugly exception is handled in GraphElementLoader.setTypeIds(..)
        TYPE_HIBERNATE_TYPE_MAP.put(MassnahmenUmsetzung.TYPE_ID, MassnahmenUmsetzung.HIBERNATE_TYPE_ID);  

        TYPE_HIBERNATE_TYPE_MAP.put(BausteinUmsetzung.TYPE_ID, BausteinUmsetzung.HIBERNATE_TYPE_ID);
        
        TYPE_HIBERNATE_TYPE_MAP.put(FinishedRiskAnalysis.TYPE_ID, FinishedRiskAnalysis.TYPE_ID_HIBERNATE);
        
        TYPE_HIBERNATE_TYPE_MAP.put(IncidentGroup.TYPE_ID, IncidentGroup.TYPE_ID_HIBERNATE);
        
    }
    
    private HibernateTypeIdManager() {
        super();
    }

    /**
     * @param typeId A type id from SNCA.xml
     * @return A Hibernate type id for a type id
     */
    public static final String getHibernateTypeId(String typeId) {
        String hibernateTypeIdFromMap = TYPE_HIBERNATE_TYPE_MAP.get(typeId);     
        if(hibernateTypeIdFromMap!=null) {
            return hibernateTypeIdFromMap;
        } else {
            return typeId;
        }
    }
}

