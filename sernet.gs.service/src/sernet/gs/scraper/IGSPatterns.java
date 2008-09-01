package sernet.gs.scraper;

import java.util.regex.Pattern;

public interface IGSPatterns {

	public abstract String getGefName();

	public abstract String getBausteinPattern();

	public abstract String getMassnahmePattern();

	public abstract String getGefaehrdungPattern();

	public abstract String getTitlePattern();

	public abstract Pattern getStandPat();

	public abstract Pattern getBaustPat();

	public abstract Pattern getSchichtPat();

}