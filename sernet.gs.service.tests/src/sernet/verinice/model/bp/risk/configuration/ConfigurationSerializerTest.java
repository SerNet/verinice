/*******************************************************************************
 * Copyright (c) 2018 Alexander Ben Nasrallah.
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
 ******************************************************************************/
package sernet.verinice.model.bp.risk.configuration;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationSerializerTest {

    @Test
    public void configurationToString() {
        RiskConfiguration configuration = DefaultRiskConfiguration.getInstance();
        String configString = ConfigurationSerializer.configurationToString(configuration);
        Assert.assertEquals(
                "{\"frequencies\":[{\"id\":\"frequency01\",\"label\":\"selten\",\"description\":\"Ereignis könnte nach heutigem Kenntnisstand höchstens alle fünf Jahre eintreten.\"},{\"id\":\"frequency02\",\"label\":\"mittel\",\"description\":\"Ereignis tritt einmal alle fünf Jahre bis einmal im Jahr ein.\"},{\"id\":\"frequency03\",\"label\":\"häufig\",\"description\":\"Ereignis tritt einmal im Jahr bis einmal pro Monat ein.\"},{\"id\":\"frequency04\",\"label\":\"sehr häufig\",\"description\":\"Ereignis tritt mehrmals im Monat ein.\"}],\"impacts\":[{\"id\":\"impact01\",\"label\":\"vernachlässigbar\",\"description\":\"Die Schadensauswirkungen sind gering und können vernachlässigt werden.\"},{\"id\":\"impact02\",\"label\":\"begrenzt\",\"description\":\"Die Schadensauswirkungen sind begrenzt und überschaubar.\"},{\"id\":\"impact03\",\"label\":\"beträchtlich\",\"description\":\"Die Schadensauswirkungen können beträchtlich sein.\"},{\"id\":\"impact04\",\"label\":\"existenzbedrohend\",\"description\":\"Die Schadensauswirkungen können ein existenziell bedrohliches, katastrophales Ausmaß erreichen.\"}],\"risks\":[{\"id\":\"risk01\",\"label\":\"gering\",\"description\":\"Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten einen ausreichenden Schutz. In der Praxis ist es üblich, geringe Risiken zu akzeptieren und die Gefährdung dennoch zu beobachten.\",\"color\":{\"red\":160,\"green\":207,\"blue\":17}},{\"id\":\"risk02\",\"label\":\"mittel\",\"description\":\"Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen reichen möglicherweise nicht aus.\",\"color\":{\"red\":255,\"green\":255,\"blue\":19}},{\"id\":\"risk03\",\"label\":\"hoch\",\"description\":\"Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung.\",\"color\":{\"red\":255,\"green\":142,\"blue\":67}},{\"id\":\"risk04\",\"label\":\"sehr hoch\",\"description\":\"Die bereits umgesetzten oder zumindest im Sicherheitskonzept vorgesehenen Sicherheitsmaßnahmen bieten keinen ausreichenden Schutz vor der jeweiligen Gefährdung. In der Praxis werden sehr hohe Risiken selten akzeptiert.\",\"color\":{\"red\":255,\"green\":18,\"blue\":18}}],\"configuration\":[[0,0,0,0],[0,0,1,2],[1,1,2,3],[1,2,3,3]]}",
                configString);
    }

    @Test
    public void configurationFromString() {
        String configString = "{\"frequencies\":[{\"id\":\"frequency01\",\"label\":\"!frequency01!\",\"description\":\"!frequency01Description!\"},{\"id\":\"frequency02\",\"label\":\"!frequency02!\",\"description\":\"!frequency02Description!\"},{\"id\":\"frequency03\",\"label\":\"!frequency03!\",\"description\":\"!frequency03Description!\"},{\"id\":\"frequency04\",\"label\":\"!frequency04!\",\"description\":\"!frequency04Description!\"}],\"impacts\":[{\"id\":\"impact01\",\"label\":\"!impact01!\",\"description\":\"!impact01Description!\"},{\"id\":\"impact02\",\"label\":\"!impact02!\",\"description\":\"!impact02Description!\"},{\"id\":\"impact03\",\"label\":\"!impact03!\",\"description\":\"!impact03Description!\"},{\"id\":\"impact04\",\"label\":\"!impact04!\",\"description\":\"!impact04Description!\"}],\"risks\":[{\"id\":\"risk01\",\"label\":\"!risk01!\",\"description\":\"!risk01Description!\",\"color\":{\"red\":160,\"green\":207,\"blue\":17}},{\"id\":\"risk02\",\"label\":\"!risk02!\",\"description\":\"!risk02Description!\",\"color\":{\"red\":255,\"green\":255,\"blue\":19}},{\"id\":\"risk03\",\"label\":\"!risk03!\",\"description\":\"!risk03Description!\",\"color\":{\"red\":255,\"green\":142,\"blue\":67}},{\"id\":\"risk04\",\"label\":\"!risk04!\",\"description\":\"!risk04Description!\",\"color\":{\"red\":255,\"green\":18,\"blue\":18}}],\"configuration\":[[0,0,0,0],[0,0,1,2],[1,1,2,3],[1,2,3,3]]}";
        RiskConfiguration configuration = ConfigurationSerializer
                .configurationFromString(configString);
        Assert.assertEquals("!impact02!", configuration.getImpacts().get(1).getLabel());
    }

    @Test
    public void configurationFromEmtpyString() {
        RiskConfiguration configuration = ConfigurationSerializer.configurationFromString("");
        Assert.assertNull(configuration);
    }
}
