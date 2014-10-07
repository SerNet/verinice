package sernet.verinice.model.report;

import java.io.Serializable;

import sernet.verinice.interfaces.report.IOutputFormat;


public class ODTOutputFormat extends AbstractOutputFormat implements IOutputFormat, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 20141007L;
    
	@Override
	public String getLabel() {
		return "Open Document Text (ODT) ";
	}

	@Override
	public String getId() {
		return "odt";
	}

	@Override
	public String getFileSuffix() {
		return "odt";
	}

    /* (non-Javadoc)
     * @see sernet.verinice.model.report.AbstractOutputFormat#isRenderOutput()
     */
    @Override
    public boolean isRenderOutput() {
        return true;
    }

}
