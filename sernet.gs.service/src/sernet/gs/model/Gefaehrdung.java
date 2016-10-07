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
package sernet.gs.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.scraper.GSScraper;

public class Gefaehrdung implements IGSModel {

    private static final long serialVersionUID = 5678950532578545503L;

    private Integer dbId;

    private String id;
    private String titel;
    private String url;
    private int kategorie = 0;
    private String stand;

    private String uuid;
    private String encoding;

    public static final int KAT_UNDEF = 0;
    public static final int KAT_HOEHERE_GEWALT = 1;
    public static final int KAT_ORG_MANGEL = 2;
    public static final int KAT_MENSCH = 3;
    public static final int KAT_TECHNIK = 4;
    public static final int KAT_VORSATZ = 5;
    public static final int KAT_ALLGEMEIN = 6;
    public static final int NUM_KATEGORIES = 7;

    // do not output these values, they are used for string matching:
    public static final String KAT_MATCH_ALLGEMEIN = "Elementare";
    public static final String KAT_MATCH_HOEHERE_GEWALT = "Gewalt";
    public static final String KAT_MATCH_ORG_MANGEL = "Organisatorisch";
    public static final String KAT_MATCH_MENSCH = "Menschliche Fehlhandlungen";
    public static final String KAT_MATCH_TECHNIK = "Technisches Versagen";
    public static final String KAT_MATCH_VORSATZ = "tzliche Handlungen";

    public static final String TYPE_ID = "gefaehrdung";

    private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)");

    private static HashMap<Integer, String> categories = new HashMap<>(NUM_KATEGORIES);

    static {
        categories.put(KAT_UNDEF, Messages.Gefaehrdung_0);
        categories.put(KAT_HOEHERE_GEWALT, Messages.Gefaehrdung_1);
        categories.put(KAT_ORG_MANGEL, Messages.Gefaehrdung_2);
        categories.put(KAT_MENSCH, Messages.Gefaehrdung_3);
        categories.put(KAT_TECHNIK, Messages.Gefaehrdung_4);
        categories.put(KAT_VORSATZ, Messages.Gefaehrdung_5);
        categories.put(KAT_ALLGEMEIN, Messages.Gefaehrdung_6);
    }

    public Gefaehrdung() {
        uuid = UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return id + " " + titel + " [" + getKategorieAsString(GSScraper.CATALOG_LANGUAGE_GERMAN) + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Gefaehrdung) {
            Gefaehrdung gefaehrdung = (Gefaehrdung) obj;
            return this.uuid.equals(gefaehrdung.getUuid());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    public String getId() {
        return id;
    }

    /**
     * Return Kapitel as comparable value, i.e. converts 3.42 to 3042 or 3.221
     * to 3221
     * 
     * 
     * @return
     */
    public int getKapitelValue() {
        final int absValueFactor = 1000;
        int absvalue = 0;
        Matcher m = kapitelPattern.matcher(getId());
        if (m.find()) {
            try {
                int whole = Integer.parseInt(m.group(1));
                int radix = Integer.parseInt(m.group(2));
                absvalue = whole * absValueFactor + radix;
            } catch (NumberFormatException e) {
                Logger.getLogger(this.getClass()).error("Kapitelnummer der Gefaehrdung ist kein Float.", e);
            }
        }
        return absvalue;

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getKategorieAsString(String language) {
        return getCategory(this.kategorie);
    }

    public String getCategory(int category) {
        return categories.get(category);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public int getKategorie() {
        return kategorie;
    }

    public void setKategorie(int kategorie) {
        this.kategorie = kategorie;
    }

    public static Collection<String> getAllCategories() {
        return categories.values();
    }

    public static int kategorieAsInt(String kategorie) {
        if (kategorie.indexOf(KAT_MATCH_HOEHERE_GEWALT) != -1) {
            return KAT_HOEHERE_GEWALT;
        } else if (kategorie.indexOf(KAT_MATCH_MENSCH) != -1) {
            return KAT_MENSCH;
        } else if (kategorie.indexOf(KAT_MATCH_ORG_MANGEL) != -1) {
            return KAT_ORG_MANGEL;
        } else if (kategorie.indexOf(KAT_MATCH_TECHNIK) != -1) {
            return KAT_TECHNIK;
        } else if (kategorie.indexOf(KAT_MATCH_VORSATZ) != -1) {
            return KAT_VORSATZ;
        } else if (kategorie.indexOf(KAT_MATCH_ALLGEMEIN) != -1) {
            return KAT_ALLGEMEIN;
        } else {
            return KAT_UNDEF;
        }
    }

    public void setStand(String stand) {
        this.stand = stand;

    }

    public String getStand() {
        return stand;
    }

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

}
