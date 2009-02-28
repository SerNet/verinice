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
