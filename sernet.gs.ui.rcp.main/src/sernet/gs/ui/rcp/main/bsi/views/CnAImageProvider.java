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
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bpm.TodoViewItem;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.CnAPlaceholder;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.IDatenschutzElement;

public class CnAImageProvider {

    private static final Map<String, String> IMAGE_NAME_BY_STATE;

    static {
        Map<String, String> m = new HashMap<>();
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
        m.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_NO, ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_YES, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_PARTIALLY, ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
        m.put(Safeguard.PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE, ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_NO, ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_YES, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_PARTIALLY, ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
        m.put(BpRequirement.PROP_IMPLEMENTATION_STATUS_NOT_APPLICABLE, ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
        IMAGE_NAME_BY_STATE = Collections.unmodifiableMap(m);
    }

    public static Image getImage(TodoViewItem elmt) {
        return getImage(elmt.getUmsetzung());
    }

    public static Image getImage(CnATreeElement elmt) {
        if (elmt instanceof MassnahmenUmsetzung) {
            MassnahmenUmsetzung mn = (MassnahmenUmsetzung) elmt;
            mn = (MassnahmenUmsetzung) Retriever.checkRetrieveElement(mn);
            String state = mn.getUmsetzung();
            return getImage(state);
        }
        if (elmt instanceof Safeguard) {
            Safeguard safeguard = (Safeguard) elmt;
            safeguard = (Safeguard) Retriever.checkRetrieveElement(safeguard);
            String state = safeguard.getImplementationStatus();
            return getImage(state);
        }
        if (elmt instanceof BpRequirement) {
            BpRequirement requirement = (BpRequirement) elmt;
            requirement = (BpRequirement) Retriever.checkRetrieveElement(requirement);
            String state = requirement.getImplementationStatus();
            return getImage(state);
        }
        if (elmt instanceof GefaehrdungsUmsetzung) {
            return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
        }

        if (elmt instanceof BausteinUmsetzung) {
            return ImageCache.getInstance().getImage(ImageCache.BAUSTEIN_UMSETZUNG);
        }

        if (elmt instanceof Anwendung || elmt instanceof AnwendungenKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.ANWENDUNG);
        }

        if (elmt instanceof Gebaeude || elmt instanceof GebaeudeKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.GEBAEUDE);
        }

        if (elmt instanceof Person || elmt instanceof PersonenKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.PERSON);
        }

        if (elmt instanceof Client || elmt instanceof ClientsKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.CLIENT);
        }

        if (elmt instanceof SonstIT || elmt instanceof SonstigeITKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.SONSTIT);
        }

        if (elmt instanceof Server || elmt instanceof ServerKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.SERVER);
        }

        if (elmt instanceof TelefonKomponente || elmt instanceof TKKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.TELEFON);
        }

        if (elmt instanceof NetzKomponente || elmt instanceof NKKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.NETWORK);
        }

        if (elmt instanceof Raum || elmt instanceof RaeumeKategorie) {
            return ImageCache.getInstance().getImage(ImageCache.RAUM);
        }

        if (elmt instanceof ITVerbund || elmt instanceof CnAPlaceholder) {
            return ImageCache.getInstance().getImage(ImageCache.EXPLORER);
        }

        if (elmt instanceof IDatenschutzElement) {
            return ImageCache.getInstance().getImage(ImageCache.SHIELD);
        }

        if (elmt instanceof FinishedRiskAnalysis) {
            return ImageCache.getInstance().getImage(ImageCache.RISIKO_MASSNAHMEN_UMSETZUNG);
        }

        if (elmt instanceof ImportBsiGroup) {
            return ImageCache.getInstance().getImage(ImageCache.ISO27K_IMPORT);
        }

        return ImageCache.getInstance().getImage(ImageCache.UNKNOWN);

    }

    private static Image getImage(String state) {
        String imageName = IMAGE_NAME_BY_STATE.get(state);
        if (imageName == null) {
            imageName = ImageCache.MASSNAHMEN_UMSETZUNG_UNBEARBEITET;
        }
        return ImageCache.getInstance().getImage(imageName);
    }

    public static Image getImage(FinishedRiskAnalysis elmt) {
        if (elmt instanceof FinishedRiskAnalysis) {
            return ImageCache.getInstance().getImage(ImageCache.RISIKO_MASSNAHMEN_UMSETZUNG);
        }
        return ImageCache.getInstance().getImage(ImageCache.UNKNOWN);

    }

    public static Image getCustomImage(CnATreeElement element) {
        Image image = null;
        if (element.getIconPath() != null) {
            image = ImageCache.getInstance().getCustomImage(element.getIconPath());
        }
        return image;
    }

}
