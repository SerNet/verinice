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
package sernet.gs.ui.rcp.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.NullMonitor;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

public class TestHibernate extends TestCase {
	
	public void testCreate() throws Exception {
		HUITypeFactory htf = HUITypeFactory.createInstance(new URL("/home/akoderman/java/runtime-oc.product/conf/SNCA.xml"));
		CnAElementHome.getInstance().open("/home/akoderman/java/runtime-oc.product/conf",
				new NullMonitor());
		BSIModel model = CnAElementFactory.getInstance().loadOrCreateModel(new NullMonitor());
		List<ITVerbund> itverbund = model.getItverbuende();
		assertTrue("Kein Modell erstellt", itverbund.size() > 0);
		
		GebaeudeKategorie category = (GebaeudeKategorie) itverbund.iterator().next()
			.getCategory(GebaeudeKategorie.TYPE_ID);
		CnATreeElement element = 
			CnAElementFactory.getInstance().saveNew(category, Gebaeude.TYPE_ID, null);
		element.getEntity().setSimpleValue(HUITypeFactory.getInstance()
				.getPropertyType("gebaeude", "gebaeude_name"), "Testgebäude");
		CnAElementHome.getInstance().update(element);

		// load again:
		BSIModel model2 = CnAElementHome.getInstance().loadModel(new NullMonitor());
		List<ITVerbund> itverbund2 = model.getItverbuende();
		assertTrue("Kein Modell erstellt", itverbund.size() > 0);
		
		GebaeudeKategorie category2 = (GebaeudeKategorie) itverbund.iterator().next()
		.getCategory(GebaeudeKategorie.TYPE_ID);
		for (CnATreeElement elmt: category2.getChildren()) {
			String title = elmt.getTitle();
			Logger.getLogger(this.getClass()).debug(title);
			assertTrue(title.length() > 0);
		}
		
		CnAElementHome.getInstance().close();
	}
	
	public void testLoad() throws Exception {
		HUITypeFactory htf = HUITypeFactory.createInstance(new URL("/home/akoderman/java/runtime-oc.product/conf/SNCA.xml"));
		CnAElementHome.getInstance().open("/home/akoderman/java/runtime-oc.product/conf",
				new NullMonitor());
		
		Logger.getLogger(this.getClass()).debug("Loading model START");
		BSIModel model = CnAElementHome.getInstance().loadModel(new NullMonitor());
		Logger.getLogger(this.getClass()).debug("Loading model FINISH");
		assertTrue("Kein Modell erstellt", model != null);
		
		List<ITVerbund> itverbund = model.getItverbuende();
		assertTrue("Kein Modell erstellt", itverbund.size() > 0);
		
		GebaeudeKategorie gebKat = (GebaeudeKategorie) itverbund.iterator().next().getCategory(GebaeudeKategorie.TYPE_ID);
		assertTrue("Kein BSI element erstellt.", gebKat.getChildren().size() >0);

		Gebaeude geb = (Gebaeude) gebKat.getChildren().iterator().next();
		assertTrue("Keine Entity zum Gebäude", geb.getEntity() != null);
		CnAElementHome.getInstance().close();
		
//		Logger.getLogger(this.getClass()).debug("Printing bausteine:------------------------------------------------------------------------------------------------\n\n\n");
//		ArrayList<BausteinUmsetzung> bausteine = model.getBausteine();
//		for (BausteinUmsetzung umsetzung : bausteine) {
//			umsetzung.getChildren().iterator().next().getTitle();
//		}
	}
	
}
