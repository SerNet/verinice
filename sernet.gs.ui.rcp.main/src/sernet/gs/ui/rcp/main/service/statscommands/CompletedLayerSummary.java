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
package sernet.gs.ui.rcp.main.service.statscommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.model.Baustein;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.grundschutzparser.LoadBausteine;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

@SuppressWarnings("serial")
public class CompletedLayerSummary extends MassnahmenSummary {

	private static final Logger log = Logger.getLogger(CompletedLayerSummary.class);
	
	private HibernateCallback hcb = new Callback();
	
	private List<Baustein> bausteine;

	public void execute() {
		try {
			setSummary(getSchichtenSummary());
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Integer> getSchichtenSummary() throws CommandException {
		Map<String, Integer> result = new HashMap<String, Integer>();

		List<Object[]> list = (List<Object[]>) getDaoFactory().getDAO(BSIModel.class).findByCallback(hcb);
		
		for (Object[] l : list) {
			String chapter = (String) l[0];
			Integer count = (Integer) l[1];
			
			Baustein baustein = getBaustein(chapter);
			if (baustein == null) {
				Logger.getLogger(this.getClass()).debug("Kein Baustein gefunden für ID" + baustein.getId());
				continue;
			}
			
			String schicht = Integer.toString(baustein.getSchicht());

			Integer saved = result.get(schicht);
			if (saved == null)
				result.put(schicht, count);
			else {
				result.put(schicht, saved + count);
			}
		}
		return result;
	}

	private static class Callback implements HibernateCallback, Serializable
	{

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
			/*
			 * Selects the chapter property (from a BausteinUmsetzung) and counts
			 * how many MassnahmenUmsetzung instances which are considered completed
			 * belong to the BausteinUmsetzung.
			 * 
			 * The selection goes from the property to its CnATreeElement (BausteinUmsetzung).
			 * From there all CnATreeElements which have that parent are selected.
			 * 
			 * Since we need to find out whether the MassnahmenUmsetzung instance
			 * is considered completed, we need to join over 'entity' and 'properties'
			 * and restrict the results to those where the propertytype is
			 * MassnahmenUmsetzung.P_UMSETZUNG
			 * and the propertyvalue is in MassnahmenUmsetzung.P_UMSETZUNG_JA or
			 * MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH.
			 * 
			 * @see MassnahmenUmsetzung#isCompleted()
			 */
			Query query = session.createSQLQuery(
					"select bup.propertyvalue as pv, count(bup.propertyvalue) as amount "
					+ "from properties bup, propertylist bupl, entity bu, cnatreeelement buc, cnatreeelement muc, entity mu, propertylist mupl, properties mup "
					+ "where bup.propertytype = :buptype "
					+ "and bup.properties_id = bupl.dbid "
					+ "and bupl.typedlist_id = bu.dbid "
					+ "and bu.dbid = buc.entity_id "
					+ "and buc.dbid = muc.parent "
					+ "and muc.entity_id = mu.dbid "
					+ "and mu.dbid = mupl.typedlist_id "
					+ "and mupl.dbid = mup.properties_id "
					+ "and mup.propertytype = :muptype "
					+ "and mup.propertyvalue in (:mupvalues) "
					+ "group by bup.propertyvalue")
					.addScalar("pv", Hibernate.STRING)
					.addScalar("amount", Hibernate.INTEGER)
					.setString("buptype", BausteinUmsetzung.P_NR)
					.setString("muptype", MassnahmenUmsetzung.P_UMSETZUNG)
					.setParameterList("mupvalues", new Object[] { MassnahmenUmsetzung.P_UMSETZUNG_JA, MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH }, Hibernate.STRING);
			
			if (log.isDebugEnabled())
				log.debug("generated query:" + query.getQueryString());
					
			return query.list();
		}
		
	}

	protected Baustein getBaustein(String kapitel) throws CommandException {
		if (bausteine == null) {
			LoadBausteine command = new LoadBausteine();
			command = getCommandService().executeCommand(command);
			this.bausteine = command.getBausteine();
		}
		for (Baustein baustein : bausteine) {
			if (baustein.getId().equals(kapitel))
				return baustein;
		}
		return null;
		
	}
	
}
