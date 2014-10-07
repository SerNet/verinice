package sernet.verinice.model.report;

import java.io.Serializable;

import sernet.verinice.interfaces.report.IOutputFormat;

public class ODSOutputFormat extends AbstractOutputFormat implements IOutputFormat, Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 20141007L;

	@Override
	public String getLabel() {
		return "Open Document Spreadsheet (ODS) ";
	}

	@Override
	public String getId() {
		return "ods";
	}

	@Override
	public String getFileSuffix() {
		return "ods";
	}

    /* (non-Javadoc)
     * @see sernet.verinice.model.report.AbstractOutputFormat#isRenderOutput()
     */
    @Override
    public boolean isRenderOutput() {
        return true;
    }

}
