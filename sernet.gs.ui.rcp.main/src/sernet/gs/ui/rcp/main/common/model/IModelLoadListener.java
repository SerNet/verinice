package sernet.gs.ui.rcp.main.common.model;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;

public interface IModelLoadListener {
	void loaded(BSIModel model);
	void closed(BSIModel model);
}
