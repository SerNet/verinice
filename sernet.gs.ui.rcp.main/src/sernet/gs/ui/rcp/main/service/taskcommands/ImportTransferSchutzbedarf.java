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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.ISchutzbedarfProvider;
import sernet.verinice.model.common.CascadingTransaction;
import sernet.verinice.model.common.CnATreeElement;

public class ImportTransferSchutzbedarf extends GenericCommand {

	private CnATreeElement element;
	private int vertraulichkeit;
	private int verfuegbarkeit;
	private int integritaet;
	private String vertrBegruendung;
	private String verfuBegruendung;
	private String integBegruendung;
	private short isPersonenbezogen;

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

	public void execute() {
		IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOforTypedElement(
				element);
		dao.reload(element, element.getDbId());
		transferSchutzbedarf();
	}

	public boolean transferSchutzbedarf() {
		if (element.getSchutzbedarfProvider() == null)
			return false;

		ISchutzbedarfProvider zielElmt = element.getSchutzbedarfProvider();

		zielElmt.setVertraulichkeit(vertraulichkeit);
	
		zielElmt.setVertraulichkeitDescription(vertrBegruendung);

		zielElmt.setVerfuegbarkeit(verfuegbarkeit);

		zielElmt.setVerfuegbarkeitDescription(verfuBegruendung);

		zielElmt.setIntegritaet(integritaet);
		
		zielElmt.setIntegritaetDescription(integBegruendung);

		if (isPersonenbezogen == 1 && element instanceof Anwendung) {
			Anwendung anwendung = (Anwendung) element;
			anwendung.setPersonenbezogen(true);
		}

		return true;
	}

}
