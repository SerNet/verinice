/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.gsimport.TransferData;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider;
import sernet.gs.ui.rcp.main.common.model.CascadingTransaction;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

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
		IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOForObject(
				element);
		dao.reload(element, element.getDbId());
		transferSchutzbedarf();
	}

	public boolean transferSchutzbedarf() {
		if (element.getSchutzbedarfProvider() == null)
			return false;

		ISchutzbedarfProvider zielElmt = element.getSchutzbedarfProvider();

		CascadingTransaction ta;

		ta = new CascadingTransaction();
		zielElmt.setVertraulichkeit(vertraulichkeit, ta);
	
		ta = new CascadingTransaction();
		zielElmt.setVertraulichkeitDescription(vertrBegruendung, ta);

		ta = new CascadingTransaction();
		zielElmt.setVerfuegbarkeit(verfuegbarkeit, ta);

		ta = new CascadingTransaction();
		zielElmt.setVerfuegbarkeitDescription(verfuBegruendung, ta);

		ta = new CascadingTransaction();
		zielElmt.setIntegritaet(integritaet, ta);
		
		ta = new CascadingTransaction();
		zielElmt.setIntegritaetDescription(integBegruendung, ta);

		if (isPersonenbezogen == 1 && element instanceof Anwendung) {
			Anwendung anwendung = (Anwendung) element;
			anwendung.setPersonenbezogen(true);
		}

		return true;
	}

}
