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
package sernet.verinice.service.commands.task;

import java.io.Serializable;

import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IReevaluator;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.common.CnATreeElement;

public class ImportTransferSchutzbedarf extends GenericCommand {

    private static final long serialVersionUID = 20160127105556L;

    private CnATreeElement element;
	private int vertraulichkeit;
	private int verfuegbarkeit;
	private int integritaet;
	private String vertrBegruendung;
	private String verfuBegruendung;
	private String integBegruendung;
	private short isPersonenbezogen;

    private boolean[] kritiaklitaetLevel = null;

    /**
     * default constructor
     */
	public ImportTransferSchutzbedarf(CnATreeElement element,
			int vertraulichkeit, int verfuegbarkeit, int integritaet,
			String vertrBegruendung, String verfuBegruendung,
			String integBegruendung, short isPersonenbezogen) {
		this.element = element;
		this.vertraulichkeit = vertraulichkeit;
		this.verfuegbarkeit = verfuegbarkeit;
		this.integritaet = integritaet;
		this.vertrBegruendung = vertrBegruendung;
		this.verfuBegruendung = verfuBegruendung;
		this.integBegruendung = integBegruendung;
		this.isPersonenbezogen = isPersonenbezogen;
	}

    /**
     * use this constructor if you want to import schutzbedarf-properties for an
     * object of type {@link Netzkomponente} which equals "Netz" in gstools
     * busines logic Schutzbedarfproperties for instances of
     * {@link Netzkomponente} are called "Kritikalität" and are not provided via
     * a {@link IReevaluator} which causes this special handling of
     * that type of objects
     * 
     * @param element
     *            - the element to compute the schutzbedarf for
     * @param kritikalitaetLevel
     *            - properties (set / unset checkboxes) from gstool business
     *            logic
     */
    public ImportTransferSchutzbedarf(NetzKomponente element, boolean[] kritikalitaetLevel) {
        this.element = element;
        this.kritiaklitaetLevel = kritikalitaetLevel;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
		IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOforTypedElement(
				element);
		dao.reload(element, element.getDbId());
        if (NetzKomponente.TYPE_ID.equals(element.getTypeId()) && kritiaklitaetLevel != null) {
		    transferSchutzbedarfNetzkomponente();
		} else {
		    transferSchutzbedarf();
		}
	}

    private boolean transferSchutzbedarf() {
		if (element.getProtectionRequirementsProvider() == null){
			return false;
		}
		IReevaluator zielElmt = element.getProtectionRequirementsProvider();

		zielElmt.setConfidentiality(vertraulichkeit);
	
		zielElmt.setConfidentialityDescription(vertrBegruendung);

		zielElmt.setAvailability(verfuegbarkeit);

		zielElmt.setAvailabilityDescription(verfuBegruendung);

		zielElmt.setIntegrity(integritaet);
		
		zielElmt.setIntegrityDescription(integBegruendung);

		if (isPersonenbezogen == 1 && element instanceof Anwendung) {
			Anwendung anwendung = (Anwendung) element;
			anwendung.setPersonenbezogen(true);
		}
		return true;
	}

    private void transferSchutzbedarfNetzkomponente() {
        PropertyType propertyType = element.getEntityType().getPropertyType(NetzKomponente.PROP_KRITIKALITAET);
        
        if (kritiaklitaetLevel[0]) {
            /*
             * the option {@link NetzKomponente.PROP_KRITIKALITAET_OPTION_0}
             * does not exist in the gstool business logic, so it isn't
             * considered here
             */

            // do nothing
        }
        if (kritiaklitaetLevel[0]) {
            element.getEntity().createNewProperty(propertyType, NetzKomponente.PROP_KRITIKALITAET_OPTION_1);
        }
        if (kritiaklitaetLevel[1]) {
            element.getEntity().createNewProperty(propertyType, NetzKomponente.PROP_KRITIKALITAET_OPTION_2);
        }
        if (kritiaklitaetLevel[2]) {
            element.getEntity().createNewProperty(propertyType, NetzKomponente.PROP_KRITIKALITAET_OPTION_3);
        }
        if (kritiaklitaetLevel[3]) {
            element.getEntity().createNewProperty(propertyType, NetzKomponente.PROP_KRITIKALITAET_OPTION_4);
        }
        if (kritiaklitaetLevel[4]) {
            element.getEntity().createNewProperty(propertyType, NetzKomponente.PROP_KRITIKALITAET_OPTION_5);
        }
    }

}
