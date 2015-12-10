package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.model.Gefaehrdung;

/**
 * Filter to extract all (Own)Gefaehrdungen matching a given String.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class RiskAnalysisWizardPageSearchFilter extends ViewerFilter {

    private Pattern pattern;

    /**
     * Updates the Pattern.
     * 
     * @param searchString
     *            the String to search for
     */
    void setPattern(String searchString) {
        pattern = Pattern.compile(searchString, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Selects all (Own)Gefaehrdungen matching the Pattern.
     * 
     * @param viewer
     *            the Viewer to operate on
     * @param parentElement
     *            not used
     * @param element
     *            given element
     * @return true if element passes test, false else
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        Gefaehrdung gefaehrdung = (Gefaehrdung) element;
        String gefaehrdungTitle = gefaehrdung.getTitel();
        Matcher matcher = pattern.matcher(gefaehrdungTitle);

        if (matcher.find()) {
            return true;
        }
        return false;
    }
}
